package com.codereview.controller;

import com.codereview.dto.UserResponse;
import com.codereview.model.User;
import com.codereview.service.AnalyticsService;
import com.codereview.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController â€” profile, leaderboard, stats.
 * Skills: RESTful API, Spring Boot, Full-Stack Web Development
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and leaderboard endpoints")
public class UserController {

    private final UserService userService;
    private final AnalyticsService analyticsService;

    // â”€â”€ GET /api/users/profile â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/profile")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.toResponse(currentUser));
    }

    // â”€â”€ PUT /api/users/profile â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PutMapping("/profile")
    @Operation(summary = "Update current user's full name or email")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                userService.updateProfile(
                        currentUser.getId(),
                        body.get("fullName"),
                        body.get("email")));
    }

    // â”€â”€ POST /api/users/change-password â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/change-password")
    @Operation(summary = "Change current user's password")
    public ResponseEntity<String> changePassword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {

        userService.changePassword(
                currentUser.getId(),
                body.get("currentPassword"),
                body.get("newPassword"));
        return ResponseEntity.ok("Password changed successfully");
    }

    // â”€â”€ GET /api/users/leaderboard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/leaderboard")
    @Operation(summary = "Get top users by points")
    public ResponseEntity<List<UserResponse>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }

    // â”€â”€ GET /api/users/search â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/search")
    @Operation(summary = "Search users by username or full name")
    public ResponseEntity<List<UserResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchUsers(q));
    }

    // â”€â”€ GET /api/users/{id} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/{id}")
    @Operation(summary = "Get a user's public profile")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // â”€â”€ GET /api/users/{id}/stats â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get analytics stats for a user")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getUserAnalytics(id));
    }

    // â”€â”€ DELETE /api/users/{id} (Admin only) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user (admin only)")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
