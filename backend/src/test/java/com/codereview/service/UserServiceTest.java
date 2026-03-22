package com.codereview.service;

import com.codereview.dto.*;
import com.codereview.exception.ResourceNotFoundException;
import com.codereview.model.User;
import com.codereview.repository.CodeReviewRepository;
import com.codereview.repository.CommentRepository;
import com.codereview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for UserService
 * Skills: Testing, JUnit 5, Mockito
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CodeReviewRepository reviewRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GamificationService gamificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(User.Role.USER);
        testUser.setActive(true);
        testUser.setPoints(0);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        UserResponse expectedResponse = UserResponse.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(com.codereview.exception.UserAlreadyExistsException.class, () -> {
            userService.register(request);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });
    }

    @Test
    void testAddPoints_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.addPoints(1L, 100);

        // Assert
        assertEquals(100, testUser.getPoints());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testUpdateProfile_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = userService.updateProfile(1L, "Updated Name", "updated@example.com");

        // Assert
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }
}
