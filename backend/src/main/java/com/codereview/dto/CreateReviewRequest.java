package com.codereview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * CreateReviewRequest DTO
 * Skills: RESTful API, API, Data Persistence
 */
@Data
public class CreateReviewRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be 5â€“200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotBlank(message = "Code content is required")
    @Size(min = 1, max = 100_000, message = "Code content must not be empty (max 100 000 chars)")
    private String codeContent;

    @NotBlank(message = "Programming language is required")
    @Size(max = 50, message = "Language must be at most 50 characters")
    private String language;

    @Size(max = 500, message = "Repository URL must be at most 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Repository URL must be a valid HTTP/HTTPS URL or empty")
    private String repositoryUrl;
}
