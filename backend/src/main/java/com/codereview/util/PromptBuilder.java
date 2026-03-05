package com.codereview.util;

import com.codereview.model.CodeReview;

/**
 * PromptBuilder — Centralised AI prompt templates for the APIService.
 * Skills: Server Side, RESTful API
 */
public final class PromptBuilder {

    private PromptBuilder() {
    }

    // ── Code Review Analysis Prompt ───────────────────────────────────────

    public static String buildCodeReviewPrompt(CodeReview review) {
        return String.format("""
                You are an expert code reviewer specialising in %s.

                Analyse the following code and return a JSON array of issues.
                Each object must have these fields:
                  severity   : "CRITICAL" | "WARNING" | "INFO"
                  category   : "SECURITY" | "PERFORMANCE" | "STYLE" | "BUGS" | "BEST_PRACTICE"
                  suggestion : concise description + recommended fix
                  codeSnippet: the problematic lines (or null)
                  lineStart  : integer line number (or null)
                  lineEnd    : integer line number (or null)

                Return ONLY the JSON array — no markdown, no extra text.

                Code:
                ```%s
                %s
                ```
                """,
                review.getLanguage(),
                review.getLanguage(),
                review.getCodeContent());
    }

    // ── Conflict Resolution Prompt ────────────────────────────────────────

    public static String buildConflictPrompt(int lineNumber,
            String[] reviewerComments) {
        StringBuilder sb = new StringBuilder();
        sb.append("Multiple reviewers left conflicting comments on line ")
                .append(lineNumber).append(":\n\n");

        for (int i = 0; i < reviewerComments.length; i++) {
            sb.append("Reviewer ").append(i + 1).append(": ")
                    .append(reviewerComments[i]).append("\n\n");
        }
        sb.append("Provide a single, balanced resolution addressing all concerns. " +
                "Be concise (max 3 sentences).");
        return sb.toString();
    }

    // ── Summary Prompt ────────────────────────────────────────────────────

    public static String buildSummaryPrompt(String codeContent, String language) {
        return String.format("""
                Provide a brief 2-sentence summary of what the following %s code does.
                Focus on its purpose and key functionality.

                Code:
                ```%s
                %s
                ```
                """,
                language, language, codeContent);
    }

    // ── Quality Score Explanation ─────────────────────────────────────────

    public static String buildScoreExplanationPrompt(double score,
            int critical,
            int warning,
            int info) {
        return String.format(
                "This code received a quality score of %.1f/10. " +
                        "There are %d critical issues, %d warnings, and %d informational items. " +
                        "Write a 2-sentence explanation of the score for the developer.",
                score, critical, warning, info);
    }
}
