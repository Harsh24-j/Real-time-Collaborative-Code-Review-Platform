package com.codereview.controller;

import com.codereview.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AnalyticsController â€” dashboard stats, review metrics, trends.
 * Skills: RESTful API, Spring Boot, Server Side
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard statistics and metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // â”€â”€ GET /api/analytics/dashboard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform-wide dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }

    // â”€â”€ GET /api/analytics/reviews/{id} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/reviews/{id}")
    @Operation(summary = "Get detailed metrics for a specific review")
    public ResponseEntity<Map<String, Object>> getReviewMetrics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getReviewMetrics(id));
    }

    // â”€â”€ GET /api/analytics/trends â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/trends")
    @Operation(summary = "Get trend data for the last N days")
    public ResponseEntity<Map<String, Object>> getTrends(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(analyticsService.getTrends(days));
    }

    // â”€â”€ GET /api/analytics/users/{id} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/users/{id}")
    @Operation(summary = "Get analytics for a specific user")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getUserAnalytics(id));
    }
}
