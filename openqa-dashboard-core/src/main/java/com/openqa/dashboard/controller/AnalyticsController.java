package com.openqa.dashboard.controller;

import com.openqa.dashboard.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> getTrends(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getTrends(days));
    }

    @GetMapping("/device-stats")
    public ResponseEntity<Map<String, Object>> getDeviceStats() {
        return ResponseEntity.ok(analyticsService.getDeviceStats());
    }
}
