package com.codereview.controller;

import com.codereview.dto.CommentRequest;
import com.codereview.dto.CommentResponse;
import com.codereview.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CommentController â€” comment CRUD and resolve/unresolve.
 * Skills: RESTful API, Spring Boot
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Review comment management")
public class CommentController {

    private final CommentService commentService;

    // â”€â”€ POST /api/reviews/{reviewId}/comments â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/api/reviews/{reviewId}/comments")
    @Operation(summary = "Add a comment to a review")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long reviewId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        request.setReviewId(reviewId); // ensure path param wins
        CommentResponse response = commentService.addComment(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // â”€â”€ GET /api/reviews/{reviewId}/comments/line/{lineNumber} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/api/reviews/{reviewId}/comments/line/{lineNumber}")
    @Operation(summary = "Get comments for a specific line")
    public ResponseEntity<List<CommentResponse>> getByLine(
            @PathVariable Long reviewId,
            @PathVariable Integer lineNumber) {
        return ResponseEntity.ok(commentService.getCommentsByLine(reviewId, lineNumber));
    }

    // â”€â”€ GET /api/reviews/{reviewId}/comments/unresolved â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/api/reviews/{reviewId}/comments/unresolved")
    @Operation(summary = "Get unresolved comments for a review")
    public ResponseEntity<List<CommentResponse>> getUnresolved(@PathVariable Long reviewId) {
        return ResponseEntity.ok(commentService.getUnresolvedComments(reviewId));
    }

    // â”€â”€ PUT /api/comments/{id} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PutMapping("/api/comments/{id}")
    @Operation(summary = "Update a comment (author only)")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(
                commentService.updateComment(id, body.get("commentText"), currentUser.getUsername()));
    }

    // â”€â”€ DELETE /api/comments/{id} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @DeleteMapping("/api/comments/{id}")
    @Operation(summary = "Delete a comment (author only)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        commentService.deleteComment(id, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    // â”€â”€ PATCH /api/comments/{id}/resolve â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PatchMapping("/api/comments/{id}/resolve")
    @Operation(summary = "Mark a comment as resolved")
    public ResponseEntity<CommentResponse> resolveComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(commentService.resolveComment(id, currentUser.getUsername()));
    }

    // â”€â”€ PATCH /api/comments/{id}/unresolve â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PatchMapping("/api/comments/{id}/unresolve")
    @Operation(summary = "Mark a comment as unresolved")
    public ResponseEntity<CommentResponse> unresolveComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(commentService.unresolveComment(id, currentUser.getUsername()));
    }
}
