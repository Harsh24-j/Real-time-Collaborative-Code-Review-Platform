package com.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ErrorResponse DTO â€” standardised error envelope
 * Skills: API, Secure Coding, Back-End Web Development
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP status code (e.g. 400, 401, 403, 404, 500) */
    private int status;

    /**
     * Short machine-readable error code (e.g. "VALIDATION_FAILED", "UNAUTHORIZED")
     */
    private String error;

    /** Human-readable description of what went wrong */
    private String message;

    /** Request path that triggered the error */
    private String path;

    /** Server timestamp of the error */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
