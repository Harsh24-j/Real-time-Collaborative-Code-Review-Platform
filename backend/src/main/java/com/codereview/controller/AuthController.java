package com.codereview.controller;

import com.codereview.dto.*;
import com.codereview.model.User;
import com.codereview.service.UserService;
import com.codereview.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — registration, login, and profile retrieval.
 * Skills: Secure Coding, RESTful API, Spring Boot, Back-End Web Development
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and current-user endpoints")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    // ── POST /api/auth/register ───────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = (User) auth.getPrincipal();
        String token = jwtUtil.generateToken(user);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .points(user.getPoints())
                .build();

        return ResponseEntity.ok(response);
    }

    // ── POST /api/auth/logout ─────────────────────────────────────────────
    // JWT is stateless — logout is handled client-side by discarding the token.
    // This endpoint exists for API completeness and future token-blacklist support.

    @PostMapping("/logout")
    @Operation(summary = "Logout (client should discard the JWT)")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    // ── GET /api/auth/me ──────────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user profile")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.toResponse(user));
    }
}
