package com.codereview.dto;

import com.codereview.model.CodeReview;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/**
 * UpdateReviewRequest DTO â€” partial update (all fields optional)
 * Skills: RESTful API, API
 */
@Data
public class UpdateReviewRequest {

    /** Allowed status transitions validated in the service layer */
    private CodeReview.Status status;

    @DecimalMin(value = "0.0", message = "Quality score must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Quality score must be at most 10.0")
    private Double qualityScore;

    private String title;
    private String description;
}
