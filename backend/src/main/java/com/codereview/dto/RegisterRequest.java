package com.codereview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * RegisterRequest DTO
 * Skills: Secure Coding, API
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3â€“50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, digits, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8â€“100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be 2â€“100 characters")
    private String fullName;
}
