package com.codereview.service;

import com.codereview.model.AISuggestion;
import com.codereview.model.CodeReview;
import com.codereview.repository.AISuggestionRepository;
import com.codereview.repository.CodeReviewRepository;
import com.codereview.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AIService — OpenAI GPT-4 integration for automated code analysis.
 * Skills: Server Side, RESTful API, Spring Boot
 *
 * Uses WebClient (WebFlux) for non-blocking HTTP calls to the OpenAI API.
 * Analysis runs @Async so it never blocks the HTTP request thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final WebClient.Builder webClientBuilder;
    private final AISuggestionRepository aiSuggestionRepository;
    private final CodeReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final CommentService commentService;

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${app.openai.model:gpt-4}")
    private String model;

    @Value("${app.openai.max-tokens:1500}")
    private int maxTokens;

    @Value("${app.openai.temperature:0.3}")
    private double temperature;

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Asynchronously analyse a code review.
     * Deletes previous AI suggestions, calls GPT-4, persists results,
     * and updates the quality score on the review.
     */
    @Async
    @Transactional
    public CompletableFuture<List<AISuggestion>> analyzeReview(Long reviewId) {
        CodeReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        log.info("Starting AI analysis for review {} ({})", reviewId, review.getLanguage());

        // Clear stale suggestions before re-analysis
        aiSuggestionRepository.deleteByReviewId(reviewId);

        String prompt = buildPrompt(review);
        String rawResponse = callOpenAI(prompt);

        List<AISuggestion> suggestions = parseResponse(rawResponse, review);
        aiSuggestionRepository.saveAll(suggestions);

        // Compute and persist quality score
        double score = computeQualityScore(suggestions);
        reviewService.updateQualityScore(reviewId, score);

        // Post AI suggestions as comments (line-specific where available)
        suggestions.stream()
                .filter(s -> s.getSeverity() == AISuggestion.Severity.CRITICAL
                        || s.getSeverity() == AISuggestion.Severity.WARNING)
                .forEach(s -> commentService.addAiComment(
                        reviewId, s.getLineStart(),
                        "[" + s.getSeverity() + " – " + s.getCategory() + "] " + s.getSuggestion()));

        log.info("AI analysis complete: reviewId={}, suggestions={}, score={}",
                reviewId, suggestions.size(), score);
        return CompletableFuture.completedFuture(suggestions);
    }

    // ── Prompt Engineering ─────────────────────────────────────────────────

    private String buildPrompt(CodeReview review) {
        return String.format("""
                You are an expert code reviewer. Analyse the following %s code and provide structured feedback.

                For each issue found, respond with a JSON array of objects in this exact format:
                [
                  {
                    "severity": "CRITICAL|WARNING|INFO",
                    "category": "SECURITY|PERFORMANCE|STYLE|BUGS|BEST_PRACTICE",
                    "suggestion": "Clear description of the issue and how to fix it",
                    "codeSnippet": "The problematic code excerpt (max 3 lines)",
                    "lineStart": <line number or null>,
                    "lineEnd": <line number or null>
                  }
                ]

                Focus on:
                - Security vulnerabilities (SQL injection, XSS, hardcoded secrets)
                - Performance issues (N+1 queries, unnecessary loops, memory leaks)
                - Code style and readability
                - Potential bugs and edge cases
                - Best practice violations

                Code to review:
                ```%s
                %s
                ```

                Return only the JSON array. No additional text.
                """,
                review.getLanguage(),
                review.getLanguage(),
                review.getCodeContent());
    }

    // ── OpenAI API Call ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callOpenAI(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key not configured — returning mock response");
            return getMockResponse();
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "temperature", temperature,
                    "messages", List.of(
                            Map.of("role", "system",
                                    "content", "You are an expert code reviewer. Return only valid JSON."),
                            Map.of("role", "user", "content", prompt)));

            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.error("OpenAI API call failed: {}", e.getMessage());
                        return Mono.just(Map.of());
                    })
                    .block();

            if (response == null || !response.containsKey("choices")) {
                return getMockResponse();
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Error calling OpenAI: {}", e.getMessage());
            return getMockResponse();
        }
    }

    // ── Response Parsing ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<AISuggestion> parseResponse(String raw, CodeReview review) {
        List<AISuggestion> suggestions = new ArrayList<>();
        try {
            // Strip markdown fences if present
            String json = raw.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)```\\s*$", "")
                    .trim();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> items = mapper.readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<>() {
                    });

            for (Map<String, Object> item : items) {
                try {
                    AISuggestion s = AISuggestion.builder()
                            .review(review)
                            .severity(AISuggestion.Severity.valueOf((String) item.get("severity")))
                            .category(AISuggestion.Category.valueOf((String) item.get("category")))
                            .suggestion((String) item.get("suggestion"))
                            .codeSnippet((String) item.getOrDefault("codeSnippet", null))
                            .lineStart(item.get("lineStart") instanceof Integer i ? i : null)
                            .lineEnd(item.get("lineEnd") instanceof Integer i ? i : null)
                            .build();
                    suggestions.add(s);
                } catch (Exception e) {
                    log.warn("Skipping malformed AI suggestion item: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
        }
        return suggestions;
    }

    // ── Quality Score Calculation ──────────────────────────────────────────

    /**
     * Scoring formula:
     * Start at 10.0
     * -2.0 per CRITICAL issue
     * -0.5 per WARNING issue
     * -0.1 per INFO issue
     * Floor at 0.0
     */
    private double computeQualityScore(List<AISuggestion> suggestions) {
        double score = 10.0;
        for (AISuggestion s : suggestions) {
            score -= switch (s.getSeverity()) {
                case CRITICAL -> 2.0;
                case WARNING -> 0.5;
                case INFO -> 0.1;
            };
        }
        return Math.max(0.0, Math.round(score * 100.0) / 100.0);
    }

    // ── Mock Response (no API key) ─────────────────────────────────────────

    private String getMockResponse() {
        return """
                [
                  {
                    "severity": "WARNING",
                    "category": "BEST_PRACTICE",
                    "suggestion": "Consider adding input validation to prevent potential security issues.",
                    "codeSnippet": null,
                    "lineStart": null,
                    "lineEnd": null
                  },
                  {
                    "severity": "INFO",
                    "category": "STYLE",
                    "suggestion": "Add Javadoc comments to public methods for better documentation.",
                    "codeSnippet": null,
                    "lineStart": null,
                    "lineEnd": null
                  }
                ]
                """;
    }
}
