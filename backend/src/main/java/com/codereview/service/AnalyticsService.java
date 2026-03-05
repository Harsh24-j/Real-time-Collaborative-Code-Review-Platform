package com.codereview.service;

import com.codereview.model.CodeReview;
import com.codereview.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AnalyticsService — dashboard stats, review metrics, user analytics.
 * Skills: Server Side, Spring Boot, Data Persistence
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final CodeReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AISuggestionRepository aiSuggestionRepository;
    private final ReviewActivityRepository activityRepository;

    // ── Dashboard summary ─────────────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalReviews", reviewRepository.count());
        stats.put("totalUsers", userRepository.countByActiveTrue());
        stats.put("totalComments", commentRepository.count());
        stats.put("openReviews", reviewRepository.countByStatus(CodeReview.Status.OPEN));
        stats.put("inReview", reviewRepository.countByStatus(CodeReview.Status.IN_REVIEW));
        stats.put("approvedReviews", reviewRepository.countByStatus(CodeReview.Status.APPROVED));
        stats.put("rejectedReviews", reviewRepository.countByStatus(CodeReview.Status.REJECTED));
        stats.put("closedReviews", reviewRepository.countByStatus(CodeReview.Status.CLOSED));

        // Reviews in the last 24 h
        long recentReviews = reviewRepository
                .findRecentReviews(LocalDateTime.now().minusHours(24)).size();
        stats.put("reviewsLast24h", recentReviews);

        // AI suggestion breakdown
        stats.put("criticalIssues",
                aiSuggestionRepository.countByCategory(com.codereview.model.AISuggestion.Category.SECURITY)
                        + aiSuggestionRepository.countByCategory(com.codereview.model.AISuggestion.Category.BUGS));
        stats.put("performanceIssues",
                aiSuggestionRepository.countByCategory(com.codereview.model.AISuggestion.Category.PERFORMANCE));

        return stats;
    }

    // ── Review metrics ────────────────────────────────────────────────────

    public Map<String, Object> getReviewMetrics(Long reviewId) {
        Map<String, Object> metrics = new LinkedHashMap<>();

        metrics.put("totalComments", commentRepository.countByReviewId(reviewId));
        metrics.put("unresolvedComments", commentRepository.countByReviewIdAndIsResolvedFalse(reviewId));

        // AI severity distribution
        Map<String, Long> severity = new LinkedHashMap<>();
        for (com.codereview.model.AISuggestion.Severity s : com.codereview.model.AISuggestion.Severity.values()) {
            severity.put(s.name(), aiSuggestionRepository.countByReviewIdAndSeverity(reviewId, s));
        }
        metrics.put("aiSeverityDistribution", severity);

        // Category distribution
        List<Object[]> catDist = aiSuggestionRepository.getCategoryDistribution(reviewId);
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (Object[] row : catDist) {
            categoryMap.put(row[0].toString(), (Long) row[1]);
        }
        metrics.put("aiCategoryDistribution", categoryMap);

        // Activity timeline
        List<Object[]> activityCounts = activityRepository.countByActivityType(reviewId);
        Map<String, Long> activityMap = new LinkedHashMap<>();
        for (Object[] row : activityCounts) {
            activityMap.put((String) row[0], (Long) row[1]);
        }
        metrics.put("activityCounts", activityMap);

        return metrics;
    }

    // ── User analytics ────────────────────────────────────────────────────

    public Map<String, Object> getUserAnalytics(Long userId) {
        Map<String, Object> analytics = new LinkedHashMap<>();

        analytics.put("reviewsCreated", reviewRepository.countByCreatorId(userId));
        analytics.put("commentsWritten", commentRepository.countByUserId(userId));
        analytics.put("approvedReviews",
                reviewRepository.findByCreatorId(userId).stream()
                        .filter(r -> r.getStatus() == CodeReview.Status.APPROVED).count());

        // Activity breakdown
        List<Object[]> activitySummary = activityRepository.getUserActivitySummary(userId);
        Map<String, Long> activityMap = new LinkedHashMap<>();
        for (Object[] row : activitySummary) {
            activityMap.put((String) row[0], (Long) row[1]);
        }
        analytics.put("activitySummary", activityMap);

        return analytics;
    }

    // ── Trend analysis ────────────────────────────────────────────────────

    public Map<String, Object> getTrends(int days) {
        Map<String, Object> trends = new LinkedHashMap<>();

        // Reviews created in the last N days
        List<CodeReview> recentReviews = reviewRepository.findRecentReviews(LocalDateTime.now().minusDays(days));
        trends.put("reviewsCreated", recentReviews.size());

        // Language distribution
        Map<String, Long> languages = new LinkedHashMap<>();
        recentReviews.forEach(r -> {
            String lang = r.getLanguage() != null ? r.getLanguage() : "unknown";
            languages.merge(lang, 1L, Long::sum);
        });
        trends.put("languageDistribution", languages);

        // Status distribution
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (CodeReview.Status s : CodeReview.Status.values()) {
            statusMap.put(s.name(), reviewRepository.countByStatus(s));
        }
        trends.put("statusDistribution", statusMap);

        // Recent activity
        List<Object> recentActivity = new ArrayList<>(
                activityRepository.findRecentActivities(LocalDateTime.now().minusDays(days)));
        trends.put("recentActivityCount", recentActivity.size());

        return trends;
    }
}
