package com.codereview.service;

import com.codereview.dto.*;
import com.codereview.exception.ResourceNotFoundException;
import com.codereview.exception.UserAlreadyExistsException;
import com.codereview.model.User;
import com.codereview.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService — user lifecycle, authentication helpers, points management.
 * Implements UserDetailsService so Spring Security can load users by username.
 * Skills: Spring Boot, Secure Coding, Back-End Web Development
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CodeReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final GamificationService gamificationService;

    // ── Spring Security ────────────────────────────────────────────────────

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    // ── Registration ───────────────────────────────────────────────────────

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .points(0)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());
        return toResponse(saved);
    }

    // ── Profile ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return toResponse(user);
    }

    public UserResponse updateProfile(Long userId, String fullName, String email) {
        User user = findOrThrow(userId);

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("Email already in use: " + email);
            }
            user.setEmail(email);
        }
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }

        return toResponse(userRepository.save(user));
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findOrThrow(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    // ── Points & Gamification ─────────────────────────────────────────────

    public void addPoints(Long userId, int points) {
        User user = findOrThrow(userId);
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
        gamificationService.checkAndAwardBadges(user);
        log.debug("Added {} points to user {}. Total: {}", points, userId, user.getPoints());
    }

    // ── Leaderboard ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getLeaderboard() {
        return userRepository.findTopUsersByPoints()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String term) {
        return userRepository.searchUsers(term)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    public void deactivateUser(Long userId) {
        User user = findOrThrow(userId);
        user.setActive(false);
        userRepository.save(user);
        log.warn("User deactivated: {}", user.getUsername());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserResponse toResponse(User user) {
        long reviewsCreated = reviewRepository.countByCreatorId(user.getId());
        long commentsWritten = commentRepository.countByUserId(user.getId());
        long reviewsApproved = reviewRepository.countByCreatorId(user.getId()); // refined by status in analytics

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .active(user.getActive())
                .points(user.getPoints())
                .badges(user.getBadges())
                .reviewsCreated(reviewsCreated)
                .commentsWritten(commentsWritten)
                .reviewsApproved(reviewsApproved)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
