package com.codereview.repository;

import com.codereview.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment Repository
 * Skills: Data Persistence, Spring Data JPA
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Derived finders
    List<Comment> findByReviewId(Long reviewId);

    List<Comment> findByUserId(Long userId);

    List<Comment> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    List<Comment> findByReviewIdAndIsResolvedFalse(Long reviewId);

    List<Comment> findByIsAiGeneratedTrue();

    List<Comment> findByIsVoiceGeneratedTrue();

    List<Comment> findByReviewIdAndLineNumber(Long reviewId, Integer lineNumber);

    // Count helpers
    long countByReviewId(Long reviewId);

    long countByReviewIdAndIsResolvedFalse(Long reviewId);

    long countByUserId(Long userId);

    // JOIN FETCH user to avoid N+1 when rendering comment threads
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.review.id = :reviewId " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findByReviewIdWithUser(@Param("reviewId") Long reviewId);

    // Recent comments made by a specific user
    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByUser(@Param("userId") Long userId);
}
