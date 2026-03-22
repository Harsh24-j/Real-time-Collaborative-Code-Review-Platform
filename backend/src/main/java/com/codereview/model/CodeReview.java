package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeReview Entity - Represents a code review request
 * Skills: J2EE, Data Persistence, Model View Controller
 */
@Entity
@Table(name = "code_reviews", indexes = {
        @Index(name = "idx_creator", columnList = "creator_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "code_content", columnDefinition = "TEXT", nullable = false)
    private String codeContent;

    @Column(length = 50)
    private String language; // java, python, javascript, etc.

    @Column(name = "repository_url")
    private String repositoryUrl;

    // Many-to-One: Many reviews belong to one creator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(name = "quality_score")
    private Double qualityScore; // AI-generated score 0.00 to 10.00

    // One-to-Many: One review has multiple comments
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // One-to-Many: One review has multiple AI suggestions
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AISuggestion> aiSuggestions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        OPEN, // Just created
        IN_REVIEW, // Being reviewed
        APPROVED, // Code approved
        REJECTED, // Code needs changes
        CLOSED // Review completed
    }
}
