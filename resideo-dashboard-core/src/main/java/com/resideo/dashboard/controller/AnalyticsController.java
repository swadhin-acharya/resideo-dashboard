package com.resideo.dashboard.controller;

import com.resideo.dashboard.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
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

    @GetMapping("/flaky-tests")
    public ResponseEntity<List<Map<String, Object>>> getFlakyTests() {
        return ResponseEntity.ok(analyticsService.getFlakyTests());
    }

    @GetMapping("/most-failed/features")
    public ResponseEntity<List<Map<String, Object>>> getMostFailedFeatures() {
        return ResponseEntity.ok(analyticsService.getMostFailedFeatures());
    }

    @GetMapping("/most-failed/scenarios")
    public ResponseEntity<List<Map<String, Object>>> getMostFailedScenarios() {
        return ResponseEntity.ok(analyticsService.getMostFailedScenarios());
    }

    @GetMapping("/by-device")
    public ResponseEntity<Map<String, Object>> getDeviceStats() {
        return ResponseEntity.ok(analyticsService.getDeviceStats());
    }

    @GetMapping("/by-firmware")
    public ResponseEntity<Map<String, Object>> getFirmwareStats() {
        return ResponseEntity.ok(analyticsService.getFirmwareStats());
    }

    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> compare(
            @RequestParam UUID left,
            @RequestParam UUID right) {
        return ResponseEntity.ok(analyticsService.compareExecutions(left, right));
    }
}
