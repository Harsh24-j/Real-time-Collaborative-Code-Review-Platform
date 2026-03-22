package com.codereview.controller;

import com.codereview.dto.CommentRequest;
import com.codereview.dto.CommentResponse;
import com.codereview.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocketController â€” STOMP @MessageMapping handlers.
 * Skills: RESTful API (WebSocket), Server Side, Full-Stack Web Development
 *
 * Client flow:
 * 1. Connect: new SockJS('/ws-review') â†’ Stomp.over(socket)
 * 2. Subscribe: /topic/review/{id} (broadcast comments)
 * /topic/review/{id}/cursors (live cursors)
 * /user/queue/notifications (private notifications)
 * 3. Send: /app/review/{id}/comment (add comment via WS)
 * /app/review/{id}/cursor (share cursor position)
 * /app/review/{id}/typing (typing indicator)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;

    // â”€â”€ /app/review/{id}/comment â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Receives a comment via WebSocket and broadcasts it to all review subscribers.
     * Delegates to CommentService so the comment is also persisted in the DB.
     */
    @MessageMapping("/review/{reviewId}/comment")
    public void handleComment(
            @DestinationVariable Long reviewId,
            @Payload CommentRequest request,
            Principal principal) {

        if (principal == null) {
            log.warn("Unauthenticated WebSocket comment attempt on review {}", reviewId);
            return;
        }

        request.setReviewId(reviewId);
        CommentResponse response = commentService.addComment(request, principal.getName());

        // Broadcast to all subscribers of this review's comment topic
        messagingTemplate.convertAndSend(
                "/topic/review/" + reviewId + "/comments", response);

        log.debug("WS comment broadcast: reviewId={} by {}", reviewId, principal.getName());
    }

    // â”€â”€ /app/review/{id}/cursor â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Broadcasts a user's cursor position to all other review collaborators.
     * Payload: { "line": 42, "column": 10 }
     */
    @MessageMapping("/review/{reviewId}/cursor")
    public void handleCursor(
            @DestinationVariable Long reviewId,
            @Payload Map<String, Object> cursorPayload,
            Principal principal) {

        if (principal == null)
            return;

        cursorPayload.put("username", principal.getName());
        messagingTemplate.convertAndSend(
                "/topic/review/" + reviewId + "/cursors", cursorPayload);
    }

    // â”€â”€ /app/review/{id}/typing â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Broadcasts typing-indicator events.
     * Payload: { "typing": true }
     */
    @MessageMapping("/review/{reviewId}/typing")
    public void handleTyping(
            @DestinationVariable Long reviewId,
            @Payload Map<String, Object> payload,
            Principal principal) {

        if (principal == null)
            return;

        payload.put("username", principal.getName());
        messagingTemplate.convertAndSend(
                "/topic/review/" + reviewId + "/typing", payload);
    }

    // â”€â”€ /app/review/{id}/presence â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * User joins a review session â€” broadcasts their presence to collaborators.
     * Payload: { "status": "JOINED" | "LEFT" }
     */
    @MessageMapping("/review/{reviewId}/presence")
    public void handlePresence(
            @DestinationVariable Long reviewId,
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor) {

        String username = headerAccessor.getUser() != null
                ? headerAccessor.getUser().getName()
                : "anonymous";

        payload.put("username", username);
        messagingTemplate.convertAndSend(
                "/topic/review/" + reviewId + "/presence", payload);

        log.debug("Presence update: reviewId={} user={} status={}",
                reviewId, username, payload.get("status"));
    }
}
