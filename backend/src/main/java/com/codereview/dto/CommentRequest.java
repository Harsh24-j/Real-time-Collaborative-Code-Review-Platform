package com.codereview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * CommentRequest DTO
 * Skills: RESTful API, API
 */
@Data
public class CommentRequest {

    @NotNull(message = "Review ID is required")
    private Long reviewId;

    @Min(value = 1, message = "Line number must be at least 1")
    private Integer lineNumber; // optional â€” null means a general review comment

    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 5000, message = "Comment must be 1â€“5000 characters")
    private String commentText;
}
