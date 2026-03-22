package com.codereview.dto;

import com.codereview.model.CodeReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ReviewResponse DTO â€” full review detail returned by the API
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

    // â”€â”€ Creator summary â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private Long creatorId;
    private String creatorUsername;
    private String creatorFullName;

    // â”€â”€ Aggregated counts (avoids pulling full lists) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private Long commentCount;
    private Long aiSuggestionCount;
    private Long unresolvedCommentCount;

    // â”€â”€ AI analysis summary â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private Long criticalIssues;
    private Long warningIssues;
    private Long infoIssues;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
