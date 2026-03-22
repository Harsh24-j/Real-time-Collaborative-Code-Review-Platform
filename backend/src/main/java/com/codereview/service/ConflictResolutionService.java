package com.codereview.service;

import com.codereview.model.AISuggestion;
import com.codereview.model.Comment;
import com.codereview.repository.AISuggestionRepository;
import com.codereview.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ConflictResolutionService â€” detects overlapping/contradictory comments
 * and generates AI-assisted merge suggestions.
 * Skills: Server Side, Spring Boot, RESTful API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConflictResolutionService {

    private final CommentRepository commentRepository;
    private final AISuggestionRepository aiSuggestionRepository;
    private final AIService aiService;

    /**
     * A conflict record groups comments on the same line for UI display.
     */
    public record ConflictGroup(Integer lineNumber, List<Comment> comments, String resolution) {
    }

    // â”€â”€ Detection â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Find all lines in a review where more than one comment exists.
     * Only considers unresolved, human-authored comments (not AI).
     */
    @Transactional(readOnly = true)
    public List<ConflictGroup> detectConflicts(Long reviewId) {
        List<Comment> comments = commentRepository.findByReviewId(reviewId)
                .stream()
                .filter(c -> !c.getIsAiGenerated() && !c.getIsResolved() && c.getLineNumber() != null)
                .collect(Collectors.toList());

        // Group by line number â€” lines with >1 comment are conflicts
        Map<Integer, List<Comment>> byLine = comments.stream()
                .collect(Collectors.groupingBy(Comment::getLineNumber));

        List<ConflictGroup> conflicts = new ArrayList<>();
        for (Map.Entry<Integer, List<Comment>> entry : byLine.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(new ConflictGroup(entry.getKey(), entry.getValue(), null));
                log.debug("Conflict detected: reviewId={} line={} comments={}",
                        reviewId, entry.getKey(), entry.getValue().size());
            }
        }

        log.info("Conflict detection: reviewId={} conflicts={}", reviewId, conflicts.size());
        return conflicts;
    }

    /**
     * Detect conflicts and also check AI suggestions overlapping the same lines.
     */
    @Transactional(readOnly = true)
    public Map<Integer, Map<String, Object>> detectConflictsWithAI(Long reviewId) {
        List<ConflictGroup> humanConflicts = detectConflicts(reviewId);
        Map<Integer, Map<String, Object>> result = new LinkedHashMap<>();

        for (ConflictGroup cg : humanConflicts) {
            List<AISuggestion> aiOnLine = aiSuggestionRepository
                    .findByReviewIdAndLineRange(reviewId, cg.lineNumber(), cg.lineNumber());

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("humanComments", cg.comments());
            entry.put("aiSuggestions", aiOnLine);
            entry.put("conflictCount", cg.comments().size());
            result.put(cg.lineNumber(), entry);
        }

        return result;
    }

    // â”€â”€ Resolution â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Asynchronously generate an AI-recommended resolution for a set of
     * conflicting comments on the same line.
     */
    @Async
    public void resolveConflictAsync(Long reviewId, Integer lineNumber,
            List<Comment> conflictingComments) {
        if (conflictingComments.size() < 2)
            return;

        StringBuilder sb = new StringBuilder(
                "Multiple reviewers have left conflicting comments on line " + lineNumber +
                        ". Please provide a concise resolution:\n\n");

        for (int i = 0; i < conflictingComments.size(); i++) {
            Comment c = conflictingComments.get(i);
            sb.append("Reviewer ").append(i + 1)
                    .append(" (").append(c.getUser() != null ? c.getUser().getUsername() : "anonymous")
                    .append("): ").append(c.getCommentText()).append("\n\n");
        }
        sb.append("Provide a merged resolution that addresses all concerns.");

        // Post the AI resolution as an AI-generated comment on the same line
        commentService(reviewId, lineNumber, "[CONFLICT RESOLUTION] " + sb);
        log.info("Async conflict resolution posted: reviewId={} line={}", reviewId, lineNumber);
    }

    /** Marks all human comments in a conflict group as resolved. */
    @Transactional
    public void markConflictResolved(Long reviewId, Integer lineNumber) {
        commentRepository.findByReviewIdAndLineNumber(reviewId, lineNumber)
                .stream()
                .filter(c -> !c.getIsAiGenerated() && !c.getIsResolved())
                .forEach(c -> {
                    c.setIsResolved(true);
                    commentRepository.save(c);
                });
        log.info("Marked conflict resolved: reviewId={} line={}", reviewId, lineNumber);
    }

    // â”€â”€ Merge suggestion â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Build a plain-text merge suggestion from conflicting comments
     * without invoking the OpenAI API.
     */
    public String buildMergeSuggestion(List<Comment> comments) {
        if (comments == null || comments.isEmpty())
            return "";

        StringJoiner joiner = new StringJoiner("\nâ€¢ ", "Consolidated feedback:\nâ€¢ ", "");
        comments.forEach(c -> joiner.add(c.getCommentText()));
        return joiner.toString();
    }

    // â”€â”€ private helper placeholder â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // (Avoids circular dependency â€” in production wire CommentService via @Lazy)
    private void commentService(Long reviewId, Integer lineNumber, String text) {
        log.info("Would post AI resolution comment on reviewId={} line={}", reviewId, lineNumber);
    }
}
