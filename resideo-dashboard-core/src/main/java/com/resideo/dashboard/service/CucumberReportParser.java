package com.resideo.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resideo.dashboard.model.entity.Execution;
import com.resideo.dashboard.model.entity.ExecutionFeature;
import com.resideo.dashboard.model.entity.ExecutionScenario;
import com.resideo.dashboard.model.entity.ExecutionStep;
import com.resideo.dashboard.model.enums.ExecutionStatus;
import com.resideo.dashboard.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class CucumberReportParser {

    private static final Logger log = LoggerFactory.getLogger(CucumberReportParser.class);

    private final ExecutionRepository executionRepository;
    private final ExecutionFeatureRepository featureRepository;
    private final ExecutionScenarioRepository scenarioRepository;
    private final ExecutionStepRepository stepRepository;
    private final ObjectMapper objectMapper;

    public CucumberReportParser(ExecutionRepository executionRepository,
                                ExecutionFeatureRepository featureRepository,
                                ExecutionScenarioRepository scenarioRepository,
                                ExecutionStepRepository stepRepository,
                                ObjectMapper objectMapper) {
        this.executionRepository = executionRepository;
        this.featureRepository = featureRepository;
        this.scenarioRepository = scenarioRepository;
        this.stepRepository = stepRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void parse(UUID executionId, File cucumberJson) {
        try {
            log.info("Parsing cucumber.json for execution {}: {}", executionId, cucumberJson.getAbsolutePath());
            JsonNode root = objectMapper.readTree(cucumberJson);

            int totalPassed = 0, totalFailed = 0, totalSkipped = 0;
            List<ExecutionFeature> features = new ArrayList<>();

            for (JsonNode featureNode : root) {
                ExecutionFeature feature = parseFeature(executionId, featureNode);
                features.add(feature);
            }
            featureRepository.saveAll(features);

            for (int i = 0; i < root.size(); i++) {
                JsonNode featureNode = root.get(i);
                ExecutionFeature feature = features.get(i);
                List<ExecutionScenario> scenarios = parseScenarios(executionId, feature.getId(), featureNode);
                for (ExecutionScenario s : scenarios) {
                    switch (s.getStatus() != null ? s.getStatus() : "") {
                        case "PASSED" -> totalPassed++;
                        case "FAILED" -> totalFailed++;
                        case "SKIPPED" -> totalSkipped++;
                    }
                }
            }

            Execution execution = executionRepository.findById(executionId).orElse(null);
            if (execution != null) {
                execution.setPassCount(totalPassed);
                execution.setFailCount(totalFailed);
                execution.setSkipCount(totalSkipped);
                execution.setTotalCount(totalPassed + totalFailed + totalSkipped);
                if (execution.getStatus() == ExecutionStatus.RUNNING) {
                    execution.setStatus(totalFailed > 0 ? ExecutionStatus.FAILED : ExecutionStatus.PASSED);
                }
                if (execution.getEndTime() == null) {
                    execution.setEndTime(Instant.now());
                }
                if (execution.getStartTime() != null && execution.getDurationMs() == null) {
                    execution.setDurationMs(Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());
                }
                executionRepository.save(execution);
            }

            log.info("Parsed {} features for execution {}: {} passed, {} failed, {} skipped",
                    features.size(), executionId, totalPassed, totalFailed, totalSkipped);

        } catch (Exception e) {
            log.error("Failed to parse cucumber.json for execution " + executionId, e);
        }
    }

    private ExecutionFeature parseFeature(UUID executionId, JsonNode featureNode) {
        ExecutionFeature feature = new ExecutionFeature();
        feature.setExecutionId(executionId);
        JsonNode name = featureNode.get("name");
        feature.setFeatureName(name != null ? name.asText() : "Unknown");
        JsonNode uri = featureNode.get("uri");
        feature.setUri(uri != null ? uri.asText() : null);

        JsonNode elements = featureNode.get("elements");
        if (elements != null && elements.isArray()) {
            int passed = 0, failed = 0, skipped = 0;
            long totalDuration = 0;
            for (JsonNode element : elements) {
                String status = getScenarioStatus(element);
                switch (status) {
                    case "PASSED" -> passed++;
                    case "FAILED" -> failed++;
                    case "SKIPPED" -> skipped++;
                }
                totalDuration += getScenarioDuration(element);
            }
            feature.setPassCount(passed);
            feature.setFailCount(failed);
            feature.setSkipCount(skipped);
            feature.setDurationMs(totalDuration);
            feature.setStatus(failed > 0 ? "FAILED" : "PASSED");
        }
        return feature;
    }

    private List<ExecutionScenario> parseScenarios(UUID executionId, UUID featureId, JsonNode featureNode) {
        List<ExecutionScenario> scenarios = new ArrayList<>();
        JsonNode elements = featureNode.get("elements");
        if (elements == null) return scenarios;

        for (JsonNode element : elements) {
            if (!"scenario".equals(element.get("type").asText()) && !"Scenario".equals(element.get("keyword").asText())) {
                continue;
            }
            ExecutionScenario scenario = new ExecutionScenario();
            scenario.setExecutionId(executionId);
            scenario.setFeatureId(featureId);
            scenario.setScenarioName(element.get("name").asText());

            JsonNode tags = element.get("tags");
            if (tags != null && tags.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode tag : tags) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(tag.get("name").asText());
                }
                scenario.setTags(sb.toString());
            }

            scenario.setStatus(getScenarioStatus(element));
            scenario.setDurationMs(getScenarioDuration(element));
            scenario.setFailureReason(getFailureReason(element));

            scenario = scenarioRepository.save(scenario);

            parseSteps(scenario.getId(), element);

            scenarios.add(scenario);
        }
        return scenarios;
    }

    private void parseSteps(UUID scenarioId, JsonNode element) {
        JsonNode steps = element.get("steps");
        if (steps == null) return;
        List<ExecutionStep> execSteps = new ArrayList<>();
        for (JsonNode stepNode : steps) {
            ExecutionStep step = new ExecutionStep();
            step.setScenarioId(scenarioId);
            step.setStepName(stepNode.get("name").asText());
            JsonNode result = stepNode.get("result");
            if (result != null) {
                step.setStatus(result.get("status").asText().toUpperCase());
                JsonNode duration = result.get("duration");
                if (duration != null) {
                    step.setDurationMs(duration.asLong() / 1_000_000);
                }
                JsonNode error = result.get("error_message");
                if (error != null) {
                    step.setLogText(error.asText());
                }
            }
            // screenshot from output/embeddings
            JsonNode embeddings = stepNode.get("embeddings");
            if (embeddings != null && embeddings.isArray() && embeddings.size() > 0) {
                step.setScreenshotPath("screenshot_" + stepNode.hashCode() + ".png");
            }
            execSteps.add(step);
        }
        stepRepository.saveAll(execSteps);
    }

    private String getScenarioStatus(JsonNode element) {
        JsonNode steps = element.get("steps");
        if (steps == null) return "UNKNOWN";
        boolean hasFailed = false;
        for (JsonNode step : steps) {
            JsonNode result = step.get("result");
            if (result != null) {
                String status = result.get("status").asText();
                if ("failed".equals(status)) hasFailed = true;
            }
        }
        return hasFailed ? "FAILED" : "PASSED";
    }

    private long getScenarioDuration(JsonNode element) {
        long total = 0;
        JsonNode steps = element.get("steps");
        if (steps == null) return 0;
        for (JsonNode step : steps) {
            JsonNode result = step.get("result");
            if (result != null && result.has("duration")) {
                total += result.get("duration").asLong();
            }
        }
        return total / 1_000_000; // nanos to ms
    }

    private String getFailureReason(JsonNode element) {
        JsonNode steps = element.get("steps");
        if (steps == null) return null;
        for (JsonNode step : steps) {
            JsonNode result = step.get("result");
            if (result != null && "failed".equals(result.get("status").asText()) && result.has("error_message")) {
                String msg = result.get("error_message").asText();
                return msg.length() > 1000 ? msg.substring(0, 1000) : msg;
            }
        }
        return null;
    }
}
