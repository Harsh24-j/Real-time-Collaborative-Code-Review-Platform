package com.codereview.repository;

import com.codereview.model.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Learning Resource Repository
 * Skills: Data Persistence, Spring Data JPA
 */
@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {

    List<LearningResource> findByIssueType(String issueType);

    List<LearningResource> findByResourceType(String resourceType);

    List<LearningResource> findByDifficultyLevel(String difficultyLevel);

    List<LearningResource> findByIssueTypeAndDifficultyLevel(
            String issueType, String difficultyLevel);

    // Title search (case-insensitive)
    @Query("SELECT lr FROM LearningResource lr WHERE " +
            "LOWER(lr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<LearningResource> searchByTitle(@Param("searchTerm") String searchTerm);

    // Random selection per issue type — exposed to AI service for varied
    // suggestions
    @Query(value = "SELECT * FROM learning_resources WHERE issue_type = :issueType " +
            "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<LearningResource> findRandomByIssueType(
            @Param("issueType") String issueType,
            @Param("limit") int limit);
}
