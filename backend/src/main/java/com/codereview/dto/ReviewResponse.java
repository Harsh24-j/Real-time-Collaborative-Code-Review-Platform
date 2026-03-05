package com.codereview.dto;

import com.codereview.model.CodeReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ReviewResponse DTO — full review detail returned by the API
 * Skills: RESTful API, Model View Controller, API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private String title;
    private String description;
    private String codeContent;
    private String language;
    private String repositoryUrl;
    private CodeReview.Status status;
    private Double qualityScore;

    // ── Creator summary ───────────────────────────────────────────────────
    private Long creatorId;
    private String creatorUsername;
    private String creatorFullName;

    // ── Aggregated counts (avoids pulling full lists) ─────────────────────
    private Long commentCount;
    private Long aiSuggestionCount;
    private Long unresolvedCommentCount;

    // ── AI analysis summary ───────────────────────────────────────────────
    private Long criticalIssues;
    private Long warningIssues;
    private Long infoIssues;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
