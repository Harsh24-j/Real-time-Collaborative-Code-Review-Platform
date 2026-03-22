package com.codereview.service;

import com.codereview.model.Badge;
import com.codereview.model.User;
import com.codereview.repository.BadgeRepository;
import com.codereview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private GamificationService gamificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .points(0)
                .badges(new HashSet<>())
                .build();
    }

    @Test
    void onReviewCreated_AddsPoints() {
        gamificationService.onReviewCreated(testUser);

        assertEquals(10, testUser.getPoints());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void onCommentAdded_AddsPoints() {
        gamificationService.onCommentAdded(testUser);

        assertEquals(5, testUser.getPoints());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void awardBadges_Success() {
        testUser.setPoints(100);
        Badge badge = Badge.builder().id(1L).name("Review Guru").pointsRequired(50).build();
        
        when(badgeRepository.findByPointsRequiredLessThanEqual(100)).thenReturn(List.of(badge));
        
        gamificationService.checkAndAwardBadges(testUser);

        assertTrue(testUser.getBadges().contains(badge));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void leaderboard_ReturnsTopUsers() {
        User u1 = User.builder().points(100).build();
        User u2 = User.builder().points(50).build();
        
        when(userRepository.findTopUsersByPoints()).thenReturn(List.of(u1, u2));
        
        List<User> top = gamificationService.getTopUsers(5);
        
        assertEquals(2, top.size());
        assertEquals(100, top.get(0).getPoints());
    }
}
