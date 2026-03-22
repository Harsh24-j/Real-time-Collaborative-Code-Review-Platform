package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Comment Entity - User comments on code reviews
 * Skills: Data Persistence, J2EE
 */
@Entity
@Table(name = "review_comments", indexes = {
        @Index(name = "idx_review_id", columnList = "review_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_review_line", columnList = "review_id,line_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private CodeReview review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "line_number")
    private Integer lineNumber; // Which line of code was commented on

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String commentText;

    @Column(name = "is_ai_generated", columnDefinition = "boolean default false")
    private Boolean isAiGenerated = false;

    @Column(name = "is_resolved", columnDefinition = "boolean default false")
    private Boolean isResolved = false;

    @Column(name = "is_voice_generated", columnDefinition = "boolean default false")
    private Boolean isVoiceGenerated = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
