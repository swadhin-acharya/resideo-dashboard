package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.dto.*;
import com.openqa.dashboard.model.entity.ExecutionFeature;
import com.openqa.dashboard.model.entity.ExecutionLog;
import com.openqa.dashboard.service.ExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping
    public List<ExecutionSummary> getAll() {
        return executionService.getAll();
    }

    @GetMapping("/{id}")
    public ExecutionResponse getById(@PathVariable String id) {
        return executionService.getById(id);
    }

    @PostMapping
    public ExecutionSummary create(@RequestBody ExecutionRequest request) {
        return executionService.create(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        executionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/features")
    public ExecutionFeature addFeature(@PathVariable String id, @RequestBody FeatureRequest request) {
        request.setExecutionId(id);
        return executionService.addFeature(request);
    }

    @PostMapping("/{id}/scenarios")
    public ExecutionResponse.ScenarioInfo addScenario(@PathVariable String id, @RequestBody ScenarioRequest request) {
        request.setExecutionId(id);
        return executionService.addScenario(request);
    }

    @PostMapping("/{id}/steps")
    public void addStep(@PathVariable String id, @RequestBody StepRequest request) {
        executionService.addStep(request);
    }

    @PostMapping("/{id}/logs")
    public ExecutionLog addLog(@PathVariable String id, @RequestBody LogRequest request) {
        request.setExecutionId(id);
        return executionService.addLog(request);
    }

    @PatchMapping("/{id}/status")
    public void updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        executionService.updateStatus(id, body.get("status"));
    }

    @PostMapping("/run")
    public ResponseEntity<ExecutionSummary> run(@RequestBody ExecutionRequest request) {
        ExecutionSummary created = executionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/finish")
    public void finish(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", executionService.determineFinalStatus(id));
        executionService.updateStatus(id, status);
    }
}
