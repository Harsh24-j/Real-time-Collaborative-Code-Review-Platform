package com.codereview.repository;

import com.codereview.model.ReviewActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Review Activity Repository - Activity tracking for analytics
 * Skills: Data Persistence, Spring Data JPA
 */
@Repository
public interface ReviewActivityRepository extends JpaRepository<ReviewActivity, Long> {

    // Derived finders
    List<ReviewActivity> findByReviewIdOrderByCreatedAtDesc(Long reviewId);

    List<ReviewActivity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReviewActivity> findByActivityType(String activityType);

    // All activities newer than a given timestamp (dashboard feed)
    @Query("SELECT a FROM ReviewActivity a WHERE a.createdAt >= :since " +
            "ORDER BY a.createdAt DESC")
    List<ReviewActivity> findRecentActivities(@Param("since") LocalDateTime since);

    // Activities on a specific review within a date range
    @Query("SELECT a FROM ReviewActivity a WHERE a.review.id = :reviewId AND " +
            "a.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY a.createdAt DESC")
    List<ReviewActivity> findByReviewIdAndDateRange(
            @Param("reviewId") Long reviewId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Activity-type counts per review â€” returns Object[]{String activityType, Long
    // count}
    @Query("SELECT a.activityType, COUNT(a) FROM ReviewActivity a " +
            "WHERE a.review.id = :reviewId GROUP BY a.activityType")
    List<Object[]> countByActivityType(@Param("reviewId") Long reviewId);

    // Per-user activity summary â€” returns Object[]{String activityType, Long count}
    @Query("SELECT a.activityType, COUNT(a) FROM ReviewActivity a " +
            "WHERE a.user.id = :userId GROUP BY a.activityType")
    List<Object[]> getUserActivitySummary(@Param("userId") Long userId);
}
