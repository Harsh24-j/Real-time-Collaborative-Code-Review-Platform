package com.codereview.dto;

/**
 * AiSuggestionDto — typed record used as BeanOutputConverter target for Spring
 * AI.
 *
 * Spring AI's BeanOutputConverter serialises the schema of this record into the
 * prompt
 * and deserialises the model's JSON response back into List<AiSuggestionDto>
 * automatically.
 *
 * Skills: Server Side, Spring Boot, RESTful API
 */
public record AiSuggestionDto(

        /**
         * Issue severity: CRITICAL | WARNING | INFO
         */
        String severity,

        /**
         * Issue category: SECURITY | PERFORMANCE | STYLE | BUGS | BEST_PRACTICE
         */
        String category,

        /**
         * Clear description of the issue and recommended fix.
         */
        String suggestion,

        /**
         * Problematic code excerpt (max ~3 lines), or null if not line-specific.
         */
        String codeSnippet,

        /**
         * First line of the issue in the submitted code (1-based), or null.
         */
        Integer lineStart,

        /**
         * Last line of the issue in the submitted code (1-based), or null.
         */
        Integer lineEnd,

        /**
         * The exact replacement code snippet that fixes the issue, or null.
         */
        String fixedCodeSnippet) {
}
