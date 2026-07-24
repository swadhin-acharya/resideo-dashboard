package com.openqa.dashboard.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class DashboardReporter {

    private final String baseUrl;
    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DashboardReporter(String baseUrl, String apiToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public DashboardReporter(String baseUrl) {
        this(baseUrl, null);
    }

    public Execution createExecution(CreateExecutionRequest request) throws Exception {
        return post("/api/v1/executions", request, Execution.class);
    }

    public Execution createExecution(CreateExecutionRequest request, String projectId) throws Exception {
        return post("/api/v1/executions", request, Execution.class,
                Map.of("X-Project-Id", projectId));
    }

    public void updateStatus(String executionId, String status) throws Exception {
        patch("/api/v1/executions/" + executionId + "/status", Map.of("status", status));
    }

    public void addLog(String executionId, String level, String message) throws Exception {
        post("/api/v1/executions/" + executionId + "/logs", new LogEntry(level, message), Void.class);
    }

    public Feature addFeature(String executionId, FeatureRequest request) throws Exception {
        return post("/api/v1/executions/" + executionId + "/features", request, Feature.class);
    }

    public Scenario addScenario(String executionId, ScenarioRequest request) throws Exception {
        return post("/api/v1/executions/" + executionId + "/scenarios", request, Scenario.class);
    }

    public void addStep(String scenarioId, StepRequest request) throws Exception {
        post("/api/v1/scenarios/" + scenarioId + "/steps", request, Void.class);
    }

    public void completeFeature(String executionId, String featureId) throws Exception {
        patch("/api/v1/executions/" + executionId + "/features/" + featureId + "/complete", Map.of());
    }

    public void completeFeature(String executionId, String featureId, String projectId) throws Exception {
        patch("/api/v1/executions/" + executionId + "/features/" + featureId + "/complete", Map.of(),
                Map.of("X-Project-Id", projectId));
    }

    public void updateScenarioStatus(String scenarioId, String status, Long durationMs, String failureReason) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        if (durationMs != null) body.put("durationMs", String.valueOf(durationMs));
        if (failureReason != null) body.put("failureReason", failureReason);
        patch("/api/v1/scenarios/" + scenarioId + "/status", body);
    }

    private <T> T post(String path, Object body, Class<T> responseType) throws Exception {
        return post(path, body, responseType, Map.of());
    }

    private <T> T post(String path, Object body, Class<T> responseType, Map<String, String> headers) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        addAuth(builder);
        headers.forEach(builder::header);
        HttpResponse<String> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("POST " + path + " failed: " + resp.statusCode() + " " + resp.body());
        }
        if (responseType == Void.class || responseType == null) return null;
        return objectMapper.readValue(resp.body(), responseType);
    }

    private void patch(String path, Object body) throws Exception {
        patch(path, body, Map.of());
    }

    private void patch(String path, Object body, Map<String, String> headers) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json));
        addAuth(builder);
        headers.forEach(builder::header);
        HttpResponse<String> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("PATCH " + path + " failed: " + resp.statusCode() + " " + resp.body());
        }
    }

    private void addAuth(HttpRequest.Builder builder) {
        if (apiToken != null && !apiToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + apiToken);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Execution {
        public String id;
        public String name;
        public String status;
        public String projectId;
        public String createdAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        public String id;
        public String featureName;
        public String status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scenario {
        public String id;
        public String scenarioName;
        public String status;
    }

    public static class CreateExecutionRequest {
        public String name;
        public String executionType;
        public String platform;
        public String environment;
        public String firmwareVersion;
        public String appVersion;
        public String buildNumber;
        public String branch;
        public String commitHash;
        public String triggeredBy;
        public String cucumberTags;
        public String source = "CLIENT";
        public String machineName;
        public String visibility;
        public Map<String, String> additionalConfig;
    }

    public static class FeatureRequest {
        public String featureName;
        public String uri;
        public String status;
        public Long durationMs;
    }

    public static class ScenarioRequest {
        public String featureId;
        public String scenarioName;
        public String tags;
        public String status;
        public Long durationMs;
        public String failureReason;
        public String deviceName;
    }

    public static class StepRequest {
        public String stepName;
        public String status;
        public Long durationMs;
        public String logText;
    }

    public static class LogEntry {
        public String level;
        public String message;
        public LogEntry() {}
        public LogEntry(String level, String message) {
            this.level = level;
            this.message = message;
        }
    }
}
