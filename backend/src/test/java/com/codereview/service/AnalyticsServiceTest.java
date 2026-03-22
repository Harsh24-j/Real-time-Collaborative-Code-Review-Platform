package com.codereview.service;

import com.codereview.repository.CodeReviewRepository;
import com.codereview.repository.CommentRepository;
import com.codereview.repository.UserRepository;
import com.codereview.repository.AISuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private CodeReviewRepository reviewRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AISuggestionRepository aiSuggestionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getStats_Success() {
        when(reviewRepository.count()).thenReturn(10L);
        when(userRepository.countByActiveTrue()).thenReturn(5L);
        when(commentRepository.count()).thenReturn(25L);
        when(reviewRepository.countByStatus(any())).thenReturn(2L);
        when(reviewRepository.findRecentReviews(any())).thenReturn(List.of());

        Map<String, Object> stats = analyticsService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(10L, stats.get("totalReviews"));
        assertEquals(5L, stats.get("totalUsers"));
        assertEquals(25L, stats.get("totalComments"));
    }
}
