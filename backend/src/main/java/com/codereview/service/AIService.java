package com.codereview.service;

import com.codereview.dto.AiSuggestionDto;
import com.codereview.model.AISuggestion;
import com.codereview.model.CodeReview;
import com.codereview.repository.AISuggestionRepository;
import com.codereview.repository.CodeReviewRepository;
import com.codereview.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AIService — Spring AI-powered code analysis using GPT-4o.
 * Skills: Server Side, RESTful API, Spring Boot
 *
 * Uses Spring AI's {@link ChatClient} (auto-configured from spring.ai.openai.*)
 * and {@link BeanOutputConverter} for type-safe, structured JSON output.
 *
 * Analysis runs {@code @Async} so HTTP request threads are never blocked.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    /** Spring AI auto-configured ChatClient (wired from spring.ai.openai.*). */
    private final ChatClient chatClient;
    private final AISuggestionRepository aiSuggestionRepository;
    private final CodeReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final CommentService commentService;

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Asynchronously analyse a code review using Spring AI + GPT-4o.
     * Deletes previous AI suggestions, calls the model, persists results,
     * and updates the quality score on the review.
     */
    @Async
    @Transactional
    public CompletableFuture<List<AISuggestion>> analyzeReview(Long reviewId) {
        CodeReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        log.info("[Spring AI] Starting analysis for review {} ({})",
                reviewId, review.getLanguage());

        // Clear stale suggestions before re-analysis
        aiSuggestionRepository.deleteByReviewId(reviewId);

        List<AISuggestion> suggestions;
        try {
            suggestions = callSpringAI(review);
        } catch (Exception e) {
            log.error("[Spring AI] Analysis failed, falling back to mock: {}", e.getMessage());
            suggestions = getMockSuggestions(review);
        }

        aiSuggestionRepository.saveAll(suggestions);

        // Compute and persist quality score
        double score = computeQualityScore(suggestions);
        reviewService.updateQualityScore(reviewId, score);

        // Post CRITICAL and WARNING suggestions as inline comments
        suggestions.stream()
                .filter(s -> s.getSeverity() == AISuggestion.Severity.CRITICAL
                        || s.getSeverity() == AISuggestion.Severity.WARNING)
                .forEach(s -> commentService.addAiComment(
                        reviewId, s.getLineStart(),
                        "[" + s.getSeverity() + " – " + s.getCategory() + "] " + s.getSuggestion()));

        log.info("[Spring AI] Analysis complete: reviewId={}, suggestions={}, score={}",
                reviewId, suggestions.size(), score);

        return CompletableFuture.completedFuture(suggestions);
    }

    // ── Spring AI call ─────────────────────────────────────────────────────

    /**
     * Calls GPT-4o via Spring AI ChatClient with a {@link BeanOutputConverter}.
     *
     * {@code BeanOutputConverter} automatically:
     * - Appends a JSON schema instruction to the prompt
     * - Deserialises the model response into {@code List<AiSuggestionDto>}
     */
    private List<AISuggestion> callSpringAI(CodeReview review) {
        // Type-safe converter: GPT JSON array → List<AiSuggestionDto>
        BeanOutputConverter<List<AiSuggestionDto>> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<AiSuggestionDto>>() {
                });

        String promptText = """
                You are an expert code reviewer. Analyse the following {language} code and provide structured feedback.

                Focus on:
                - Security vulnerabilities (SQL injection, XSS, hardcoded secrets)
                - Performance issues (N+1 queries, unnecessary loops, memory leaks)
                - Code style and readability
                - Potential bugs and edge cases
                - Best practice violations

                For each issue found, respond with a JSON array where each object has:
                - severity: CRITICAL | WARNING | INFO
                - category: SECURITY | PERFORMANCE | STYLE | BUGS | BEST_PRACTICE
                - suggestion: clear description and how to fix it
                - codeSnippet: the problematic code excerpt (max 3 lines), or null
                - fixedCodeSnippet: the complete corrected replacement for the problematic code excerpt, or null if no code change is required. Make sure this is a drop-in replacement.
                - lineStart: first affected line number (1-based), or null
                - lineEnd: last affected line number (1-based), or null

                {format}

                Code to review:
                ```{language}
                {code}
                ```
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        var prompt = template.create(Map.of(
                "language", review.getLanguage(),
                "code", truncateCode(review.getCodeContent()),
                "format", converter.getFormat()));

        String rawResponse = chatClient
                .prompt(prompt)
                .call()
                .content();
        log.debug("[Spring AI] Raw response length: {} chars", rawResponse == null ? 0 : rawResponse.length());

        if (rawResponse == null || rawResponse.isBlank()) {
            log.warn("[Spring AI] Received empty response from model");
            return new ArrayList<>();
        }

        List<AiSuggestionDto> dtos = converter.convert(rawResponse);
        return mapToEntities(dtos, review);
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private List<AISuggestion> mapToEntities(List<AiSuggestionDto> dtos, CodeReview review) {
        List<AISuggestion> entities = new ArrayList<>();
        if (dtos == null)
            return entities;

        for (AiSuggestionDto dto : dtos) {
            try {
                AISuggestion entity = AISuggestion.builder()
                        .review(review)
                        .severity(parseSeverity(dto.severity()))
                        .category(parseCategory(dto.category()))
                        .suggestion(dto.suggestion())
                        .codeSnippet(dto.codeSnippet())
                        .fixedCodeSnippet(dto.fixedCodeSnippet())
                        .lineStart(dto.lineStart())
                        .lineEnd(dto.lineEnd())
                        .build();
                entities.add(entity);
            } catch (Exception e) {
                log.warn("[Spring AI] Skipping malformed suggestion DTO: {}", e.getMessage());
            }
        }
        return entities;
    }

    private AISuggestion.Severity parseSeverity(String severity) {
        if (severity == null) return AISuggestion.Severity.INFO;
        try {
            return AISuggestion.Severity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AISuggestion.Severity.INFO;
        }
    }

    private AISuggestion.Category parseCategory(String category) {
        if (category == null) return AISuggestion.Category.BEST_PRACTICE;
        try {
            return AISuggestion.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AISuggestion.Category.BEST_PRACTICE;
        }
    }

    // ── Quality Score ──────────────────────────────────────────────────────

    /**
     * Scoring formula: start at 10.0, deduct per issue.
     * CRITICAL: -2.0 | WARNING: -0.5 | INFO: -0.1 | Floor at 0.0.
     * Returns a 0.0–10.0 score.
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

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Truncate very large code submissions to stay within model context limits. */
    private String truncateCode(String code) {
        if (code == null)
            return "";
        // GPT-4o context: 128k tokens; 100k chars ≈ ~25k tokens — safe limit
        int limit = 100_000;
        return code.length() > limit ? code.substring(0, limit) + "\n... [truncated]" : code;
    }

    /** Fallback suggestions when the API is unavailable or the key is blank. */
    private List<AISuggestion> getMockSuggestions(CodeReview review) {
        return List.of(
                AISuggestion.builder()
                        .review(review)
                        .severity(AISuggestion.Severity.WARNING)
                        .category(AISuggestion.Category.BEST_PRACTICE)
                        .suggestion("Consider adding input validation to prevent potential security issues.")
                        .build(),
                AISuggestion.builder()
                        .review(review)
                        .severity(AISuggestion.Severity.INFO)
                        .category(AISuggestion.Category.STYLE)
                        .suggestion("Add Javadoc comments to public methods for better documentation.")
                        .build());
    }

    /** Returns all suggestions for a review, ordered by highest severity first. */
    public List<AISuggestion> getSuggestionsByReviewId(Long reviewId) {
        return aiSuggestionRepository.findByReviewIdOrderBySeverity(reviewId);
    }
}
