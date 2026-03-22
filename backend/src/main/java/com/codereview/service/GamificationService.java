package com.codereview.service;

import com.codereview.model.Badge;
import com.codereview.model.User;
import com.codereview.repository.BadgeRepository;
import com.codereview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * GamificationService â€” points, badge awards, and leaderboard logic.
 * Skills: Full-Stack Web Development, Server Side, Spring Boot
 *
 * Points table:
 * REVIEW_CREATED â†’ 10 pts
 * COMMENT_ADDED â†’ 5 pts
 * REVIEW_APPROVED â†’ 20 pts
 * REVIEW_RESOLVED â†’ 15 pts
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GamificationService {

    private static final int PTS_REVIEW_CREATED = 10;
    private static final int PTS_COMMENT_ADDED = 5;
    private static final int PTS_REVIEW_APPROVED = 20;
    private static final int PTS_REVIEW_RESOLVED = 15;

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;

    // â”€â”€ Event hooks (called by other services) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void onReviewCreated(User user) {
        addPoints(user, PTS_REVIEW_CREATED, "REVIEW_CREATED");
    }

    public void onCommentAdded(User user) {
        addPoints(user, PTS_COMMENT_ADDED, "COMMENT_ADDED");
    }

    public void onReviewApproved(User user) {
        addPoints(user, PTS_REVIEW_APPROVED, "REVIEW_APPROVED");
    }

    public void onReviewResolved(User user) {
        addPoints(user, PTS_REVIEW_RESOLVED, "REVIEW_RESOLVED");
    }

    // â”€â”€ Badge evaluation â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Check whether the user has crossed any badge threshold since the last award.
     * Called after every points addition.
     */
    public void checkAndAwardBadges(User user) {
        List<Badge> eligible = badgeRepository.findByPointsRequiredLessThanEqual(user.getPoints());
        boolean awarded = false;

        for (Badge badge : eligible) {
            if (!user.getBadges().contains(badge)) {
                user.getBadges().add(badge);
                awarded = true;
                log.info("Badge '{}' awarded to user {}", badge.getName(), user.getUsername());
            }
        }

        if (awarded) {
            userRepository.save(user);
        }
    }

    // â”€â”€ Leaderboard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public List<User> getTopUsers(int limit) {
        return userRepository.findTopUsersByPoints()
                .stream()
                .limit(limit)
                .toList();
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void addPoints(User user, int pts, String reason) {
        user.setPoints(user.getPoints() + pts);
        userRepository.save(user);
        log.debug("Awarded {} pts ({}) to user {}. Total: {}", pts, reason,
                user.getUsername(), user.getPoints());
        checkAndAwardBadges(user);
    }
}
