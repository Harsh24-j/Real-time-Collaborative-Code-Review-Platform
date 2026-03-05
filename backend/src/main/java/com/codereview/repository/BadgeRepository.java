package com.codereview.repository;

import com.codereview.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Badge Repository - Gamification
 * Skills: Data Persistence, Spring Data JPA
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByName(String name);

    List<Badge> findByType(Badge.BadgeType type);

    // Find all badges a user is eligible for based on their current points
    List<Badge> findByPointsRequiredLessThanEqual(Integer points);

    // Ordered list for the badge showcase UI
    List<Badge> findAllByOrderByPointsRequiredAsc();

    boolean existsByName(String name);
}
