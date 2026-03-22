package com.codereview.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Badge Entity - Gamification badges for the leaderboard system
 * Skills: Data Persistence
 */
@Entity
@Table(name = "badges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "points_required")
    private Integer pointsRequired;

    @Enumerated(EnumType.STRING)
    private BadgeType type;

    public enum BadgeType {
        BRONZE, SILVER, GOLD, PLATINUM
    }
}
