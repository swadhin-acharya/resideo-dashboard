package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.entity.Execution;
import com.openqa.dashboard.model.entity.ExecutionFeature;
import com.openqa.dashboard.model.entity.ExecutionScenario;
import com.openqa.dashboard.model.entity.ExecutionStep;
import com.openqa.dashboard.repository.ExecutionFeatureRepository;
import com.openqa.dashboard.repository.ExecutionRepository;
import com.openqa.dashboard.repository.ExecutionScenarioRepository;
import com.openqa.dashboard.repository.ExecutionStepRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scenarios")
public class ScenarioController {

    private final ExecutionStepRepository stepRepository;
    private final ExecutionScenarioRepository scenarioRepository;
    private final ExecutionRepository executionRepository;
    private final ExecutionFeatureRepository featureRepository;

    public ScenarioController(ExecutionStepRepository stepRepository,
                              ExecutionScenarioRepository scenarioRepository,
                              ExecutionRepository executionRepository,
                              ExecutionFeatureRepository featureRepository) {
        this.stepRepository = stepRepository;
        this.scenarioRepository = scenarioRepository;
        this.executionRepository = executionRepository;
        this.featureRepository = featureRepository;
    }

    @PostMapping("/{scenarioId}/steps")
    public ResponseEntity<Void> addStep(@PathVariable UUID scenarioId, @RequestBody Map<String, Object> body) {
        ExecutionStep step = new ExecutionStep();
        step.setScenarioId(scenarioId);
        step.setStepName((String) body.get("stepName"));
        step.setStatus((String) body.getOrDefault("status", "UNKNOWN"));
        step.setDurationMs(body.get("durationMs") != null ? ((Number) body.get("durationMs")).longValue() : null);
        step.setLogText((String) body.get("logText"));
        stepRepository.save(step);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{scenarioId}/status")
    public ResponseEntity<Void> updateScenarioStatus(@PathVariable UUID scenarioId, @RequestBody Map<String, String> body) {
        scenarioRepository.findById(scenarioId).ifPresent(scenario -> {
            String oldStatus = scenario.getStatus();
            String newStatus = body.get("status");
            scenario.setStatus(newStatus);
            if (body.containsKey("durationMs")) {
                scenario.setDurationMs(Long.parseLong(body.get("durationMs")));
            }
            if (body.containsKey("failureReason")) {
                scenario.setFailureReason(body.get("failureReason"));
            }
            scenarioRepository.save(scenario);

            // Update counts when transitioning from RUNNING to a final state
            if ("RUNNING".equals(oldStatus) && !"RUNNING".equals(newStatus)) {
                executionRepository.findById(scenario.getExecutionId()).ifPresent(exec -> {
                    if ("PASSED".equals(newStatus)) exec.setPassCount(exec.getPassCount() + 1);
                    else if ("FAILED".equals(newStatus)) exec.setFailCount(exec.getFailCount() + 1);
                    else if ("SKIPPED".equals(newStatus)) exec.setSkipCount(exec.getSkipCount() + 1);
                    executionRepository.save(exec);
                });
                if (scenario.getFeatureId() != null) {
                    featureRepository.findById(scenario.getFeatureId()).ifPresent(feature -> {
                        if ("PASSED".equals(newStatus)) feature.setPassCount(feature.getPassCount() + 1);
                        else if ("FAILED".equals(newStatus)) feature.setFailCount(feature.getFailCount() + 1);
                        else if ("SKIPPED".equals(newStatus)) feature.setSkipCount(feature.getSkipCount() + 1);
                        featureRepository.save(feature);
                    });
                }
            }
        });
        return ResponseEntity.ok().build();
    }
}
