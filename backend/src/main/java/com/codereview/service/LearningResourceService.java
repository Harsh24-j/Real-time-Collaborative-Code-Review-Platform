package com.codereview.service;

import com.codereview.model.AISuggestion;
import com.codereview.model.LearningResource;
import com.codereview.repository.AISuggestionRepository;
import com.codereview.repository.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LearningResourceService â€” maps AI-detected categories to curated resources.
 * Skills: Server Side, Spring Boot, Data Persistence
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearningResourceService {

    private final LearningResourceRepository resourceRepository;
    private final AISuggestionRepository aiSuggestionRepository;

    /** Per-category randomised recommendations for a review's AI suggestions. */
    public Map<String, List<LearningResource>> getRecommendationsForReview(
            Long reviewId, int limitPerCategory) {

        Set<AISuggestion.Category> categories = aiSuggestionRepository
                .findByReviewId(reviewId).stream()
                .map(AISuggestion::getCategory)
                .collect(Collectors.toSet());

        Map<String, List<LearningResource>> result = new LinkedHashMap<>();
        for (AISuggestion.Category cat : categories) {
            List<LearningResource> resources = resourceRepository.findRandomByIssueType(cat.name(), limitPerCategory);
            if (!resources.isEmpty())
                result.put(cat.name(), resources);
        }
        return result;
    }

    public List<LearningResource> getByIssueType(String issueType) {
        return resourceRepository.findByIssueType(issueType);
    }

    public List<LearningResource> getByIssueTypeAndDifficulty(String issueType, String difficulty) {
        return resourceRepository.findByIssueTypeAndDifficultyLevel(issueType, difficulty);
    }

    public List<LearningResource> search(String term) {
        return resourceRepository.searchByTitle(term);
    }

    public List<LearningResource> getAllResources() {
        return resourceRepository.findAll();
    }

    @Transactional
    public LearningResource createResource(LearningResource resource) {
        return resourceRepository.save(resource);
    }

    @Transactional
    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }

    public String getCategoryDescription(AISuggestion.Category category) {
        return switch (category) {
            case SECURITY -> "Security vulnerability â€” injection, secrets, auth";
            case PERFORMANCE -> "Performance issue â€” complexity, queries, caching";
            case STYLE -> "Code style â€” naming, formatting, readability";
            case BUGS -> "Potential bug â€” nulls, edge cases, logic errors";
            case BEST_PRACTICE -> "Best practice â€” SOLID, DRY, design patterns";
        };
    }
}
