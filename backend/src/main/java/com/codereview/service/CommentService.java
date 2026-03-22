package com.codereview.service;

import com.codereview.dto.CommentRequest;
import com.codereview.dto.CommentResponse;
import com.codereview.exception.ResourceNotFoundException;
import com.codereview.exception.UnauthorizedException;
import com.codereview.model.*;
import com.codereview.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CommentService â€” comment CRUD, resolve/unresolve, real-time broadcast.
 * Skills: Spring Boot, RESTful API, Server Side
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CodeReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewActivityRepository activityRepository;
    private final GamificationService gamificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // â”€â”€ Create â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public CommentResponse addComment(CommentRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        CodeReview review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", request.getReviewId()));

        Comment comment = Comment.builder()
                .review(review)
                .user(user)
                .lineNumber(request.getLineNumber())
                .commentText(request.getCommentText())
                .isAiGenerated(false)
                .isVoiceGenerated(false)
                .isResolved(false)
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Comment added: reviewId={} by user={}", review.getId(), username);

        // Activity log + gamification
        activityRepository.save(ReviewActivity.builder()
                .review(review).user(user)
                .activityType("COMMENT_ADDED")
                .activityDetail("{\"commentId\":" + saved.getId() + "}")
                .build());
        gamificationService.onCommentAdded(user);

        // Broadcast via WebSocket
        CommentResponse response = toResponse(saved);
        messagingTemplate.convertAndSend("/topic/review/" + review.getId() + "/comments", response);

        return response;
    }

    /** Called by AIService â€” stores AI-generated suggestion as a comment. */
    public CommentResponse addAiComment(Long reviewId, Integer lineNumber, String text) {
        CodeReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Comment comment = Comment.builder()
                .review(review)
                .user(null) // system/AI â€” no user
                .lineNumber(lineNumber)
                .commentText(text)
                .isAiGenerated(true)
                .isResolved(false)
                .build();

        Comment saved = commentRepository.save(comment);
        CommentResponse response = toResponse(saved);
        messagingTemplate.convertAndSend("/topic/review/" + reviewId + "/comments", response);
        return response;
    }

    // â”€â”€ Read â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByReview(Long reviewId) {
        return commentRepository.findByReviewIdWithUser(reviewId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getUnresolvedComments(Long reviewId) {
        return commentRepository.findByReviewIdAndIsResolvedFalse(reviewId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByLine(Long reviewId, Integer lineNumber) {
        return commentRepository.findByReviewIdAndLineNumber(reviewId, lineNumber)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // â”€â”€ Update â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public CommentResponse updateComment(Long commentId, String newText, String username) {
        Comment comment = findOrThrow(commentId);
        assertIsAuthor(comment, username);
        comment.setCommentText(newText);
        return toResponse(commentRepository.save(comment));
    }

    public CommentResponse resolveComment(Long commentId, String username) {
        Comment comment = findOrThrow(commentId);
        comment.setIsResolved(true);
        Comment saved = commentRepository.save(comment);

        messagingTemplate.convertAndSend(
                "/topic/review/" + comment.getReview().getId() + "/comments",
                toResponse(saved));

        log.info("Comment {} resolved by {}", commentId, username);
        return toResponse(saved);
    }

    public CommentResponse unresolveComment(Long commentId, String username) {
        Comment comment = findOrThrow(commentId);
        comment.setIsResolved(false);
        return toResponse(commentRepository.save(comment));
    }

    // â”€â”€ Delete â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void deleteComment(Long commentId, String username) {
        Comment comment = findOrThrow(commentId);
        assertIsAuthor(comment, username);
        commentRepository.delete(comment);
        log.info("Comment {} deleted by {}", commentId, username);
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private Comment findOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }

    private void assertIsAuthor(Comment comment, String username) {
        if (comment.getUser() == null || !comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("You are not the author of this comment");
        }
    }

    public CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .reviewId(c.getReview().getId())
                .lineNumber(c.getLineNumber())
                .commentText(c.getCommentText())
                .isAiGenerated(c.getIsAiGenerated())
                .isVoiceGenerated(c.getIsVoiceGenerated())
                .isResolved(c.getIsResolved())
                .userId(c.getUser() != null ? c.getUser().getId() : null)
                .username(c.getUser() != null ? c.getUser().getUsername() : "AI Assistant")
                .userFullName(c.getUser() != null ? c.getUser().getFullName() : "AI Assistant")
                .createdAt(c.getCreatedAt())
                .build();
    }
}
