package com.codereview.repository;

import com.codereview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository - Database access for User entity
 * Skills: Data Persistence, Spring Data JPA
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username (for authentication)
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if username exists (for registration validation)
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find all active users
    List<User> findByActiveTrue();

    // Find users by role
    List<User> findByRole(User.Role role);

    // Count active users
    long countByActiveTrue();

    // Get leaderboard — top users by points
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.points DESC")
    List<User> findTopUsersByPoints();

    // Get users with minimum points threshold
    @Query("SELECT u FROM User u WHERE u.active = true AND u.points >= :minPoints " +
            "ORDER BY u.points DESC")
    List<User> findUsersWithMinimumPoints(@Param("minPoints") Integer minPoints);

    // Search users by username or full name (case-insensitive)
    @Query("SELECT u FROM User u WHERE u.active = true AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.fullName)  LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}
