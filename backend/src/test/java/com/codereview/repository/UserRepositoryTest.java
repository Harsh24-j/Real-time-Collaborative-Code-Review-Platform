package com.codereview.repository;

import com.codereview.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository Tests - Database Layer
 * Skills: Testing, JPA, Data Persistence
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuserrepo");
        testUser.setEmail("testrepo@example.com");
        testUser.setPassword("password");
        testUser.setFullName("Test User Repo");
        testUser.setRole(User.Role.USER);
        testUser.setActive(true);
        testUser.setPoints(100);
    }

    @Test
    void testSaveUser() {
        // Act
        User saved = userRepository.save(testUser);
        
        // Assert
        assertNotNull(saved.getId());
        assertEquals("testuserrepo", saved.getUsername());
    }

    @Test
    void testFindByUsername() {
        // Arrange
        entityManager.persist(testUser);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByUsername("testuserrepo");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testrepo@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail() {
        // Arrange
        entityManager.persist(testUser);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByEmail("testrepo@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuserrepo", found.get().getUsername());
    }

    @Test
    void testExistsByUsername() {
        // Arrange
        entityManager.persist(testUser);
        entityManager.flush();

        // Act
        boolean exists = userRepository.existsByUsername("testuserrepo");

        // Assert
        assertTrue(exists);
    }

    @Test
    void testFindTopUsersByPoints() {
        // Arrange
        User user1 = createUser("user1", "user1@test.com", 100);
        User user2 = createUser("user2", "user2@test.com", 200);
        User user3 = createUser("user3", "user3@test.com", 150);
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        // Act
        List<User> topUsers = userRepository.findTopUsersByPoints();

        // Assert
        assertFalse(topUsers.isEmpty());
        assertEquals("user2", topUsers.get(0).getUsername()); // Highest points first
        assertEquals(200, topUsers.get(0).getPoints());
    }

    @Test
    void testCountByActiveTrue() {
        // Arrange
        User activeUser = createUser("active", "active@test.com", 50);
        User inactiveUser = createUser("inactive", "inactive@test.com", 50);
        inactiveUser.setActive(false);
        
        entityManager.persist(activeUser);
        entityManager.persist(inactiveUser);
        entityManager.flush();

        // Act
        long count = userRepository.countByActiveTrue();

        // Assert
        assertTrue(count >= 1);
    }

    private User createUser(String username, String email, int points) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setFullName(username);
        user.setRole(User.Role.USER);
        user.setActive(true);
        user.setPoints(points);
        return user;
    }
}
