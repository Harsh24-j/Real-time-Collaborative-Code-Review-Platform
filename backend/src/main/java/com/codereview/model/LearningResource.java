package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * LearningResource â€” curated learning materials (docs, tutorials, articles)
 * surfaced alongside AI code-review suggestions to help developers improve.
 *
 * Skills: Data Persistence, J2EE, Spring Boot
 */
@Entity
@Table(name = "learning_resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short descriptive title shown in the UI. */
    @Column(nullable = false, length = 255)
    private String title;

    /** Brief description of what the resource covers. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Direct URL to the resource (article, doc page, video, etc.). */
    @Column(nullable = false, length = 1000)
    private String url;

    /**
     * The code-quality issue type this resource addresses.
     * e.g. "SQL_INJECTION", "NULL_POINTER", "CODE_SMELL"
     */
    @Column(name = "issue_type", length = 100)
    private String issueType;

    /**
     * Format of the resource: ARTICLE, VIDEO, DOCUMENTATION, TUTORIAL, COURSE.
     */
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    /**
     * Difficulty level: BEGINNER, INTERMEDIATE, ADVANCED.
     */
    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    /** Estimated read/watch time in minutes (0 = unknown). */
    @Column(name = "estimated_minutes")
    @Builder.Default
    private Integer estimatedMinutes = 0;

    /** Provider / author of the resource (e.g. "MDN", "Baeldung"). */
    @Column(length = 100)
    private String provider;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
