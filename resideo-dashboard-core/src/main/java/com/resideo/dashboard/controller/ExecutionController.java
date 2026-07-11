package com.resideo.dashboard.controller;

import com.resideo.dashboard.config.ResideoProperties;
import com.resideo.dashboard.model.dto.ExecutionRequest;
import com.resideo.dashboard.model.dto.ExecutionResponse;
import com.resideo.dashboard.model.dto.ExecutionSummary;
import com.resideo.dashboard.model.dto.PagedResponse;
import com.resideo.dashboard.security.CurrentUser;
import com.resideo.dashboard.service.ExecutionRunnerService;
import com.resideo.dashboard.service.ExecutionService;
import com.resideo.dashboard.websocket.LiveExecutionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/executions")
public class ExecutionController {

    private final ExecutionService executionService;
    private final ExecutionRunnerService executionRunner;
    private final ResideoProperties properties;
    private final LiveExecutionHandler wsHandler;

    public ExecutionController(ExecutionService executionService,
                                ExecutionRunnerService executionRunner,
                                ResideoProperties properties,
                                LiveExecutionHandler wsHandler) {
        this.executionService = executionService;
        this.executionRunner = executionRunner;
        this.properties = properties;
        this.wsHandler = wsHandler;
    }

    @PostMapping
    public ResponseEntity<ExecutionResponse> create(@RequestBody ExecutionRequest request,
                                                     @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr,
                                                     @CurrentUser UUID userId) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        ExecutionResponse response = executionService.create(request, projectId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/trigger")
    public ResponseEntity<ExecutionResponse> trigger(@RequestBody ExecutionRequest request,
                                                      @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr,
                                                      @CurrentUser UUID userId) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        request.setSource("DASHBOARD");
        ExecutionResponse response = executionRunner.createAndRun(request, projectId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ExecutionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String firmware,
            @RequestParam(required = false) String appVersion,
            @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr,
            @CurrentUser UUID userId) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        return ResponseEntity.ok(executionService.list(page, size, status, platform, environment, firmware, appVersion, projectId, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionResponse> getById(@PathVariable UUID id,
                                                      @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        return ResponseEntity.ok(executionService.getById(id, projectId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ExecutionResponse> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        ExecutionResponse response = executionService.updateStatus(id, newStatus);
        if (!"RUNNING".equals(newStatus) && !"PENDING".equals(newStatus)) {
            wsHandler.broadcast(id, "EXECUTION_" + newStatus, Map.of("status", newStatus));
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<ExecutionResponse> updateName(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(executionService.updateName(id, body.get("name")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        executionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        executionRunner.cancelExecution(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<ExecutionSummary> getSummary(
            @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        return ResponseEntity.ok(executionService.getSummary(projectId));
    }

    @GetMapping("/running")
    public ResponseEntity<List<ExecutionResponse>> getRunning(
            @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        return ResponseEntity.ok(executionService.getRunningExecutions(projectId));
    }

    @GetMapping("/feature-files")
    public ResponseEntity<List<Map<String, String>>> listFeatureFiles() {
        return ResponseEntity.ok(executionRunner.listFeatureFiles(properties.getWorkspace()));
    }

    @PostMapping("/{id}/logs")
    public ResponseEntity<Void> addLog(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String level = body.getOrDefault("level", "INFO");
        String message = body.get("message");
        executionService.appendLog(id, level, message);
        wsHandler.broadcast(id, "EXECUTION_LOG", Map.of("level", level, "message", message));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/features")
    public ResponseEntity<Map<String, Object>> addFeature(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var feature = executionService.addFeature(id,
                (String) body.get("featureName"),
                (String) body.get("uri"),
                (String) body.getOrDefault("status", "RUNNING"),
                body.get("durationMs") != null ? ((Number) body.get("durationMs")).longValue() : null);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", feature.getId().toString(),
                "featureName", feature.getFeatureName(),
                "status", feature.getStatus()
        ));
    }

    @PatchMapping("/{id}/features/{featureId}/complete")
    public ResponseEntity<Void> completeFeature(@PathVariable UUID id, @PathVariable UUID featureId) {
        executionService.completeFeature(featureId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<Map<String, Object>>> getLogs(@PathVariable UUID id) {
        return ResponseEntity.ok(executionService.getLogs(id));
    }

    @GetMapping("/{id}/scenarios/{scenarioId}/steps")
    public ResponseEntity<List<Map<String, Object>>> getScenarioSteps(@PathVariable UUID id, @PathVariable UUID scenarioId) {
        return ResponseEntity.ok(executionService.getStepsByScenario(scenarioId));
    }

    @PostMapping("/{id}/scenarios")
    public ResponseEntity<Map<String, Object>> addScenario(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String featureIdStr = (String) body.get("featureId");
        UUID featureId = featureIdStr != null ? UUID.fromString(featureIdStr) : null;
        String scenarioName = (String) body.get("scenarioName");
        String status = (String) body.getOrDefault("status", "UNKNOWN");
        var scenario = executionService.addScenario(id, featureId,
                scenarioName,
                (String) body.get("tags"),
                status,
                body.get("durationMs") != null ? ((Number) body.get("durationMs")).longValue() : null,
                (String) body.get("failureReason"),
                (String) body.get("deviceName"));
        wsHandler.broadcastScenarioComplete(id, "Cucumber", scenarioName, status);
        wsHandler.broadcastSummaryUpdate(id,
                scenario.getStatus().equals("PASSED") ? 1 : 0,
                scenario.getStatus().equals("FAILED") ? 1 : 0,
                scenario.getStatus().equals("SKIPPED") ? 1 : 0);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", scenario.getId().toString(),
                "scenarioName", scenario.getScenarioName(),
                "status", scenario.getStatus()
        ));
    }
}
