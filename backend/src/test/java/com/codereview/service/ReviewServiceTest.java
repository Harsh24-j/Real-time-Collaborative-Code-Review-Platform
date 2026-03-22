package com.codereview.service;

import com.codereview.dto.*;
import com.codereview.exception.ResourceNotFoundException;
import com.codereview.model.CodeReview;
import com.codereview.model.User;
import com.codereview.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private CodeReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AISuggestionRepository aiSuggestionRepository;
    
    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private ReviewActivityRepository activityRepository;
    
    @Mock
    private GamificationService gamificationService;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ReviewService reviewService;

    private CodeReview testReview;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testReview = new CodeReview();
        testReview.setId(1L);
        testReview.setTitle("Test Review");
        testReview.setDescription("Test Description");
        testReview.setCodeContent("System.out.println('Hello');");
        testReview.setLanguage("java");
        testReview.setCreator(testUser);
        testReview.setStatus(CodeReview.Status.OPEN);
    }

    @Test
    void testCreateReview_Success() {
        // Arrange
        CreateReviewRequest request = new CreateReviewRequest();
        request.setTitle("Test Review");
        request.setCodeContent("System.out.println('Hello');");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(CodeReview.class))).thenReturn(testReview);

        // Act
        ReviewResponse result = reviewService.createReview(request, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test Review", result.getTitle());
        verify(reviewRepository, times(1)).save(any(CodeReview.class));
    }

    @Test
    void testGetAllReviews_Success() {
        // Arrange
        Page<CodeReview> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(testReview));
        when(reviewRepository.findAll(any(org.springframework.data.domain.PageRequest.class))).thenReturn(page);

        // Act
        Page<ReviewResponse> result = reviewService.getAllReviews(0, 10, "createdAt");

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("Test Review", result.getContent().get(0).getTitle());
        verify(reviewRepository, times(1)).findAll(any(org.springframework.data.domain.PageRequest.class));
    }

    @Test
    void testGetReviewById_Success() {
        // Arrange
        when(reviewRepository.findByIdWithCreator(1L)).thenReturn(Optional.of(testReview));

        // Act
        ReviewResponse result = reviewService.getReviewById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Review", result.getTitle());
    }

    @Test
    void testGetReviewById_NotFound() {
        // Arrange
        when(reviewRepository.findByIdWithCreator(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getReviewById(999L);
        });
    }

    @Test
    void testUpdateReview_Success() {
        // Arrange
        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setStatus(CodeReview.Status.APPROVED);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        lenient().when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(CodeReview.class))).thenReturn(testReview);

        // Act
        reviewService.updateReview(1L, request, "testuser");

        // Assert
        assertEquals(CodeReview.Status.APPROVED, testReview.getStatus());
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    void testDeleteReview_Success() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        doNothing().when(reviewRepository).delete(any(CodeReview.class));

        // Act
        reviewService.deleteReview(1L, "testuser");

        // Assert
        verify(reviewRepository, times(1)).delete(testReview);
    }
}
