package com.codereview.dto;

import com.codereview.model.Badge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * UserResponse DTO — public profile + statistics
 * Skills: API, Full-Stack Web Development
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Boolean active;

    // ── Gamification ──────────────────────────────────────────────────────
    private Integer points;
    private Set<Badge> badges;

    // ── Statistics ────────────────────────────────────────────────────────
    private Long reviewsCreated;
    private Long commentsWritten;
    private Long reviewsApproved;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
