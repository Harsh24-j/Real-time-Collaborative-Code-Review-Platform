package com.codereview.repository;

import com.codereview.model.AISuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI Suggestion Repository
 * Skills: Data Persistence, Spring Data JPA
 */
@Repository
public interface AISuggestionRepository extends JpaRepository<AISuggestion, Long> {

    // Derived finders
    List<AISuggestion> findByReviewId(Long reviewId);

    List<AISuggestion> findBySeverity(AISuggestion.Severity severity);

    List<AISuggestion> findByCategory(AISuggestion.Category category);

    List<AISuggestion> findByReviewIdAndSeverity(Long reviewId, AISuggestion.Severity severity);

    // Count helpers
    long countByReviewIdAndSeverity(Long reviewId, AISuggestion.Severity severity);

    long countByCategory(AISuggestion.Category category);

    // Ordered by severity priority: CRITICAL → WARNING → INFO
    @Query("SELECT s FROM AISuggestion s WHERE s.review.id = :reviewId " +
            "ORDER BY CASE s.severity " +
            "WHEN 'CRITICAL' THEN 1 " +
            "WHEN 'WARNING'  THEN 2 " +
            "WHEN 'INFO'     THEN 3 END, s.createdAt ASC")
    List<AISuggestion> findByReviewIdOrderBySeverity(@Param("reviewId") Long reviewId);

    // Suggestions overlapping a given line range
    @Query("SELECT s FROM AISuggestion s WHERE s.review.id = :reviewId AND " +
            "s.lineStart >= :startLine AND s.lineEnd <= :endLine")
    List<AISuggestion> findByReviewIdAndLineRange(
            @Param("reviewId") Long reviewId,
            @Param("startLine") Integer startLine,
            @Param("endLine") Integer endLine);

    // Category distribution for pie/bar charts
    @Query("SELECT s.category, COUNT(s) FROM AISuggestion s " +
            "WHERE s.review.id = :reviewId GROUP BY s.category")
    List<Object[]> getCategoryDistribution(@Param("reviewId") Long reviewId);

    // Bulk delete when re-running AI analysis
    void deleteByReviewId(Long reviewId);
}
