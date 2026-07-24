package com.openqa.dashboard.service;

import com.openqa.dashboard.model.dto.FeatureRequest;
import com.openqa.dashboard.model.dto.ScenarioRequest;
import com.openqa.dashboard.model.dto.StepRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class CucumberReportParser {

    private static final Logger log = LoggerFactory.getLogger(CucumberReportParser.class);

    private final ExecutionService executionService;

    public CucumberReportParser(ExecutionService executionService) {
        this.executionService = executionService;
    }

    public void parse(String executionId, File jsonFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonFile);

            if (!root.isArray()) {
                log.warn("Expected JSON array at root: {}", jsonFile);
                return;
            }

            for (JsonNode featureNode : root) {
                String featureName = featureNode.has("name") ? featureNode.get("name").asText("Unknown") : "Unknown";
                String uri = featureNode.has("uri") ? featureNode.get("uri").asText("") : "";

                FeatureRequest featureReq = new FeatureRequest();
                featureReq.setExecutionId(executionId);
                featureReq.setFeatureName(featureName);
                featureReq.setUri(uri);
                featureReq.setStatus("RUNNING");
                var feature = executionService.addFeature(featureReq);
                String featureId = feature.getId();
                long featureDuration = 0;

                JsonNode elements = featureNode.get("elements");
                if (elements != null) {
                    for (JsonNode element : elements) {
                        String type = element.has("type") ? element.get("type").asText("") : "";
                        if (!"scenario".equals(type) && !"Scenario".equals(type)) continue;

                        String scenarioName = element.has("name") ? element.get("name").asText("Unknown") : "Unknown";
                        String tags = extractTags(element);
                        String status = "UNKNOWN";
                        long scenarioDuration = 0;

                        JsonNode steps = element.get("steps");
                        if (steps != null) {
                            for (JsonNode stepNode : steps) {
                                String keyword = stepNode.has("keyword") ? stepNode.get("keyword").asText("") : "";
                                String stepName = stepNode.has("name") ? stepNode.get("name").asText("") : "";
                                String stepStatus = "UNKNOWN";
                                Long stepDuration = 0L;
                                if (stepNode.has("result")) {
                                    stepStatus = stepNode.get("result").has("status")
                                            ? stepNode.get("result").get("status").asText("UNKNOWN") : "UNKNOWN";
                                    stepDuration = stepNode.get("result").has("duration")
                                            ? stepNode.get("result").get("duration").asLong() / 1000000 : 0L;
                                }

                                StepRequest stepReq = new StepRequest();
                                stepReq.setScenarioId(null); // Will need to set after scenario creation
                                stepReq.setKeyword(keyword);
                                stepReq.setStepName(stepName);
                                stepReq.setStatus(stepStatus.toUpperCase());
                                stepReq.setDurationMs(stepDuration);
                                scenarioDuration += stepDuration;

                                if ("FAILED".equalsIgnoreCase(stepStatus)) status = "FAILED";
                                else if ("PASSED".equalsIgnoreCase(stepStatus) && !"FAILED".equals(status)) status = "PASSED";
                                else if ("SKIPPED".equalsIgnoreCase(stepStatus) && !"FAILED".equals(status) && !"PASSED".equals(status)) status = "SKIPPED";
                            }
                        }

                        ScenarioRequest scenarioReq = new ScenarioRequest();
                        scenarioReq.setExecutionId(executionId);
                        scenarioReq.setFeatureId(featureId);
                        scenarioReq.setScenarioName(scenarioName);
                        scenarioReq.setTags(tags);
                        scenarioReq.setStatus(status);
                        scenarioReq.setDurationMs(scenarioDuration);
                        var scenario = executionService.addScenario(scenarioReq);
                        featureDuration += scenarioDuration;

                        // Add steps with correct scenario ID
                        if (steps != null) {
                            for (JsonNode stepNode : steps) {
                                String keyword = stepNode.has("keyword") ? stepNode.get("keyword").asText("") : "";
                                String stepName = stepNode.has("name") ? stepNode.get("name").asText("") : "";
                                String stepStatus = "UNKNOWN";
                                Long stepDuration = 0L;
                                if (stepNode.has("result")) {
                                    stepStatus = stepNode.get("result").has("status")
                                            ? stepNode.get("result").get("status").asText("UNKNOWN") : "UNKNOWN";
                                    stepDuration = stepNode.get("result").has("duration")
                                            ? stepNode.get("result").get("duration").asLong() / 1000000 : 0L;
                                }
                                StepRequest stepReq = new StepRequest();
                                stepReq.setScenarioId(scenario.getId());
                                stepReq.setKeyword(keyword);
                                stepReq.setStepName(stepName);
                                stepReq.setStatus(stepStatus.toUpperCase());
                                stepReq.setDurationMs(stepDuration);
                                executionService.addStep(stepReq);
                            }
                        }
                    }
                }

                String featureStatus = "PASSED";
                if (featureNode.has("status")) {
                    featureStatus = featureNode.get("status").asText("PASSED");
                }
                feature.setStatus(featureStatus);
                feature.setDurationMs(featureDuration);
            }

            log.info("Parsed cucumber.json for execution {}: {}", executionId, jsonFile.getName());
        } catch (Exception e) {
            log.error("Failed to parse cucumber.json: {}", jsonFile, e);
        }
    }

    private String extractTags(JsonNode element) {
        StringBuilder sb = new StringBuilder();
        JsonNode tags = element.get("tags");
        if (tags != null) {
            for (JsonNode tag : tags) {
                if (tag.has("name")) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(tag.get("name").asText());
                }
            }
        }
        return sb.toString();
    }
}
