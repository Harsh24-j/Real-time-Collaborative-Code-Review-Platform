package com.codereview.repository;

import com.codereview.model.CodeReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CodeReview Repository
 * Skills: Data Persistence, Spring Data JPA
 */
@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    // Derived finders
    List<CodeReview> findByCreatorId(Long creatorId);

    List<CodeReview> findByStatus(CodeReview.Status status);

    List<CodeReview> findByLanguage(String language);

    // Paginated variants
    Page<CodeReview> findByCreatorId(Long creatorId, Pageable pageable);

    Page<CodeReview> findByStatus(CodeReview.Status status, Pageable pageable);

    // Count helpers
    long countByStatus(CodeReview.Status status);

    long countByCreatorId(Long creatorId);

    // JOIN FETCH to avoid N+1 on single-review load
    @Query("SELECT r FROM CodeReview r JOIN FETCH r.creator WHERE r.id = :id")
    Optional<CodeReview> findByIdWithCreator(@Param("id") Long id);

    // JOIN FETCH for list views
    @Query("SELECT DISTINCT r FROM CodeReview r JOIN FETCH r.creator")
    List<CodeReview> findAllWithCreator();

    // Reviews with comment counts â€” returns Object[]{CodeReview, Long}
    @Query("SELECT r, COUNT(c) FROM CodeReview r LEFT JOIN r.comments c " +
            "GROUP BY r ORDER BY r.createdAt DESC")
    List<Object[]> findAllWithCommentCount();

    // Recent reviews since a given timestamp
    @Query("SELECT r FROM CodeReview r WHERE r.createdAt >= :since " +
            "ORDER BY r.createdAt DESC")
    List<CodeReview> findRecentReviews(@Param("since") LocalDateTime since);

    // Reviews within a quality-score band
    @Query("SELECT r FROM CodeReview r WHERE r.qualityScore BETWEEN :minScore AND :maxScore")
    List<CodeReview> findByQualityScoreRange(
            @Param("minScore") Double minScore,
            @Param("maxScore") Double maxScore);

    // Full-text search on title + description
    @Query("SELECT r FROM CodeReview r WHERE " +
            "LOWER(r.title)       LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CodeReview> searchReviews(@Param("searchTerm") String searchTerm);

    // Top-quality reviews (pageable so caller can limit to top N)
    @Query("SELECT r FROM CodeReview r WHERE r.qualityScore IS NOT NULL " +
            "ORDER BY r.qualityScore DESC")
    List<CodeReview> findTopQualityReviews(Pageable pageable);
}
