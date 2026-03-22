package com.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse DTO - Returned after successful login or registration
 * Skills: Secure Coding, API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Bearer JWT token â€” include as Authorization header on subsequent requests */
    private String token;

    private String tokenType = "Bearer";

    /** Token lifetime in milliseconds (matches app.jwt.expiration) */
    private Long expiresIn;

    // â”€â”€ Embedded user summary â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Integer points;
}
