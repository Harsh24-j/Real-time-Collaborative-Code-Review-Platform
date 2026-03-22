package com.codereview.service;

import com.codereview.dto.*;
import com.codereview.exception.ResourceNotFoundException;
import com.codereview.exception.UnauthorizedException;
import com.codereview.model.*;
import com.codereview.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewService â€” CRUD, status transitions, quality scoring, search.
 * Skills: Spring Boot, RESTful API, Data Persistence, Server Side
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final CodeReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AISuggestionRepository aiSuggestionRepository;
    private final CommentRepository commentRepository;
    private final ReviewActivityRepository activityRepository;
    private final GamificationService gamificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // â”€â”€ Create â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public ReviewResponse createReview(CreateReviewRequest request, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        CodeReview review = CodeReview.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .codeContent(request.getCodeContent())
                .language(request.getLanguage())
                .repositoryUrl(request.getRepositoryUrl())
                .creator(creator)
                .status(CodeReview.Status.OPEN)
                .build();

        CodeReview saved = reviewRepository.save(review);
        logActivity(saved, creator, "REVIEW_CREATED", null);
        gamificationService.onReviewCreated(creator);

        log.info("Review created: id={} by user={}", saved.getId(), username);
        return toResponse(saved);
    }

    // â”€â”€ Read â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        CodeReview review = reviewRepository.findByIdWithCreator(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(int page, int size, String sortBy) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return reviewRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByStatus(CodeReview.Status status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUser(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByCreatorId(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> searchReviews(String term) {
        return reviewRepository.searchReviews(term)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getRecentReviews(int hours) {
        return reviewRepository.findRecentReviews(LocalDateTime.now().minusHours(hours))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getTopQualityReviews(int limit) {
        return reviewRepository.findTopQualityReviews(PageRequest.of(0, limit))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // â”€â”€ Update â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public ReviewResponse updateReview(Long id, UpdateReviewRequest request, String username) {
        CodeReview review = findOrThrow(id);
        assertIsCreator(review, username);

        if (request.getTitle() != null)
            review.setTitle(request.getTitle());
        if (request.getDescription() != null)
            review.setDescription(request.getDescription());
        if (request.getQualityScore() != null)
            review.setQualityScore(request.getQualityScore());

        if (request.getStatus() != null) {
            transitionStatus(review, request.getStatus(), username);
        }

        CodeReview saved = reviewRepository.save(review);
        broadcastUpdate(saved);
        return toResponse(saved);
    }

    /** Updates the quality score â€” typically called by AIService after analysis. */
    public void updateQualityScore(Long reviewId, Double score) {
        CodeReview review = findOrThrow(reviewId);
        review.setQualityScore(score);
        reviewRepository.save(review);
        broadcastUpdate(review);
    }

    // â”€â”€ Delete â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void deleteReview(Long id, String username) {
        CodeReview review = findOrThrow(id);
        assertIsCreator(review, username);
        reviewRepository.delete(review);
        log.info("Review {} deleted by {}", id, username);
    }

    // â”€â”€ Status transitions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void transitionStatus(CodeReview review, CodeReview.Status newStatus, String byUsername) {
        CodeReview.Status old = review.getStatus();
        review.setStatus(newStatus);

        User actor = userRepository.findByUsername(byUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", byUsername));

        logActivity(review, actor, "STATUS_CHANGED",
                String.format("{\"from\":\"%s\",\"to\":\"%s\"}", old, newStatus));

        if (newStatus == CodeReview.Status.APPROVED) {
            gamificationService.onReviewApproved(review.getCreator());
        }
        log.info("Review {} status: {} â†’ {}", review.getId(), old, newStatus);
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private CodeReview findOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
    }

    private void assertIsCreator(CodeReview review, String username) {
        if (!review.getCreator().getUsername().equals(username)) {
            throw new UnauthorizedException("You are not the creator of this review");
        }
    }

    private void logActivity(CodeReview review, User user, String type, String detail) {
        activityRepository.save(ReviewActivity.builder()
                .review(review)
                .user(user)
                .activityType(type)
                .activityDetail(detail)
                .build());
    }

    /** Broadcast review update to all WebSocket subscribers of this review. */
    private void broadcastUpdate(CodeReview review) {
        messagingTemplate.convertAndSend(
                "/topic/review/" + review.getId(),
                toResponse(review));
    }

    public ReviewResponse toResponse(CodeReview r) {
        long commentCount = commentRepository.countByReviewId(r.getId());
        long unresolved = commentRepository.countByReviewIdAndIsResolvedFalse(r.getId());
        long aiCount = aiSuggestionRepository.countByReviewIdAndSeverity(r.getId(), AISuggestion.Severity.CRITICAL)
                + aiSuggestionRepository.countByReviewIdAndSeverity(r.getId(), AISuggestion.Severity.WARNING)
                + aiSuggestionRepository.countByReviewIdAndSeverity(r.getId(), AISuggestion.Severity.INFO);
        long critical = aiSuggestionRepository.countByReviewIdAndSeverity(r.getId(), AISuggestion.Severity.CRITICAL);
        long warnings = aiSuggestionRepository.countByReviewIdAndSeverity(r.getId(), AISuggestion.Severity.WARNING);
        long info = aiSuggestionRepository.countByReviewIdAndSeverity(r.getId(), AISuggestion.Severity.INFO);

        return ReviewResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .codeContent(r.getCodeContent())
                .language(r.getLanguage())
                .repositoryUrl(r.getRepositoryUrl())
                .status(r.getStatus())
                .qualityScore(r.getQualityScore())
                .creatorId(r.getCreator().getId())
                .creatorUsername(r.getCreator().getUsername())
                .creatorFullName(r.getCreator().getFullName())
                .commentCount(commentCount)
                .unresolvedCommentCount(unresolved)
                .aiSuggestionCount(aiCount)
                .criticalIssues(critical)
                .warningIssues(warnings)
                .infoIssues(info)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
