package com.codereview.controller;

import com.codereview.dto.*;
import com.codereview.model.CodeReview;
import com.codereview.service.AIService;
import com.codereview.service.CommentService;
import com.codereview.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ReviewController — full CRUD, status management, search, AI analysis trigger.
 * Skills: RESTful API, Spring Boot, Model View Controller
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Code review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;
    private final CommentService commentService;
    private final AIService aiService;

    // ── GET /api/reviews ──────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all reviews with pagination and optional search")
    public ResponseEntity<?> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CodeReview.Status status) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(reviewService.searchReviews(search));
        }
        if (status != null) {
            return ResponseEntity.ok(reviewService.getReviewsByStatus(status, page, size));
        }
        Page<ReviewResponse> result = reviewService.getAllReviews(page, size, sortBy);
        return ResponseEntity.ok(result);
    }

    // ── GET /api/reviews/{id} ─────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a review by ID")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    // ── POST /api/reviews ─────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new code review")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        ReviewResponse review = reviewService.createReview(request, currentUser.getUsername());

        // Trigger async AI analysis immediately after creation
        aiService.analyzeReview(review.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    // ── PUT /api/reviews/{id} ─────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update a review (creator only)")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(
                reviewService.updateReview(id, request, currentUser.getUsername()));
    }

    // ── DELETE /api/reviews/{id} ──────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review (creator only)")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        reviewService.deleteReview(id, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── PATCH /api/reviews/{id}/status ────────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update review status")
    public ResponseEntity<ReviewResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails currentUser) {

        CodeReview.Status newStatus = CodeReview.Status.valueOf(body.get("status").toUpperCase());
        UpdateReviewRequest req = new UpdateReviewRequest();
        req.setStatus(newStatus);
        return ResponseEntity.ok(reviewService.updateReview(id, req, currentUser.getUsername()));
    }

    // ── GET /api/reviews/{id}/comments ────────────────────────────────────

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get all comments for a review")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentsByReview(id));
    }

    // ── POST /api/reviews/{id}/analyze ───────────────────────────────────

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger AI re-analysis of a review")
    public ResponseEntity<String> triggerAnalysis(@PathVariable Long id) {
        aiService.analyzeReview(id);
        return ResponseEntity.accepted().body("AI analysis started asynchronously");
    }

    // ── GET /api/reviews/recent ───────────────────────────────────────────

    @GetMapping("/recent")
    @Operation(summary = "Get reviews created in the last N hours")
    public ResponseEntity<List<ReviewResponse>> getRecent(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(reviewService.getRecentReviews(hours));
    }
}
