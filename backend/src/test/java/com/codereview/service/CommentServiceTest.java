package com.codereview.service;

import com.codereview.dto.CommentRequest;
import com.codereview.dto.CommentResponse;
import com.codereview.model.CodeReview;
import com.codereview.model.Comment;
import com.codereview.model.User;
import com.codereview.repository.CodeReviewRepository;
import com.codereview.repository.CommentRepository;
import com.codereview.repository.UserRepository;
import com.codereview.repository.ReviewActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CodeReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewActivityRepository activityRepository;
    @Mock
    private GamificationService gamificationService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private CodeReview testReview;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();
        testReview = CodeReview.builder().id(1L).title("Test Review").build();
        commentRequest = new CommentRequest();
        commentRequest.setReviewId(1L);
        commentRequest.setLineNumber(10);
        commentRequest.setCommentText("Test comment");
    }

    @Test
    void addComment_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        
        Comment savedComment = Comment.builder()
                .id(1L)
                .commentText("Test comment")
                .user(testUser)
                .review(testReview)
                .lineNumber(10)
                .build();
        
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse result = commentService.addComment(commentRequest, "testuser");

        assertNotNull(result);
        assertEquals("Test comment", result.getCommentText());
        verify(commentRepository).save(any(Comment.class));
        verify(messagingTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void resolveComment_Success() {
        Comment comment = Comment.builder()
                .id(1L)
                .isResolved(false)
                .review(testReview)
                .build();
        
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

        CommentResponse result = commentService.resolveComment(1L, "testuser");

        assertTrue(result.getIsResolved());
        verify(commentRepository).save(comment);
    }
}
