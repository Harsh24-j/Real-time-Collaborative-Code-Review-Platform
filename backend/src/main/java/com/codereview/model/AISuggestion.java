package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * AISuggestion Entity - AI-generated code improvement suggestions
 * Skills: Data Persistence, Server Side
 */
@Entity
@Table(name = "ai_suggestions", indexes = {
        @Index(name = "idx_ai_review_id", columnList = "review_id"),
        @Index(name = "idx_severity", columnList = "severity")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AISuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private CodeReview review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String suggestion;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet; // The problematic code excerpt

    @Column(name = "fixed_code_snippet", columnDefinition = "TEXT")
    private String fixedCodeSnippet; // The AI-suggested corrected code

    @Column(name = "line_start")
    private Integer lineStart;

    @Column(name = "line_end")
    private Integer lineEnd;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Severity {
        CRITICAL, // Must fix immediately
        WARNING, // Should fix
        INFO // Nice to have
    }

    public enum Category {
        SECURITY, // Security vulnerabilities
        PERFORMANCE, // Performance issues
        STYLE, // Code style issues
        BUGS, // Potential bugs
        BEST_PRACTICE // Best practice violations
    }
}
