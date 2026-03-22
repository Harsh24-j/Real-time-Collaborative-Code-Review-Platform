package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ReviewActivity Entity â€” audit log for analytics and activity feeds
 * Skills: Data Persistence, J2EE
 *
 * Tracks every significant action on a review (comment added, status changed,
 * AI analysis run, etc.) for the dashboard activity feed and metrics.
 */
@Entity
@Table(name = "review_activities", indexes = {
        @Index(name = "idx_activity_review", columnList = "review_id"),
        @Index(name = "idx_activity_user", columnList = "user_id"),
        @Index(name = "idx_activity_type", columnList = "activity_type"),
        @Index(name = "idx_activity_time", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ReviewActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private CodeReview review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Free-text activity type label.
     * Examples: COMMENT_ADDED, STATUS_CHANGED, AI_ANALYSIS_COMPLETE,
     * REVIEW_APPROVED, REVIEW_REJECTED, USER_JOINED
     */
    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    /** Optional JSON or plain-text detail payload for the activity */
    @Column(name = "activity_detail", columnDefinition = "TEXT")
    private String activityDetail;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
