package com.resideo.dashboard.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resideo.dashboard.config.AgentProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "resideo.agent", name = "enabled", havingValue = "true")
public class ExecutionAgent {

    private static final Logger log = LoggerFactory.getLogger(ExecutionAgent.class);

    private final AgentProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

    public ExecutionAgent(AgentProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @PostConstruct
    public void start() {
        log.info("ExecutionAgent started - polling {} every {}ms", properties.getApiUrl(), properties.getPollIntervalMs());
        scheduler.scheduleWithFixedDelay(this::poll, 0, properties.getPollIntervalMs(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdown();
        runningProcesses.values().forEach(p -> p.destroyForcibly());
    }

    private void poll() {
        try {
            String json = apiGet("/api/v1/executions/agent/pending");
            if (json == null || json.isBlank()) return;

            List<Map<String, Object>> executions = objectMapper.readValue(json, new TypeReference<>() {});
            for (Map<String, Object> exec : executions) {
                String id = (String) exec.get("id");
                if (id == null || runningProcesses.containsKey(id)) continue;

                String mavenCmd = (String) exec.get("mavenCommand");
                if (mavenCmd == null || mavenCmd.isBlank()) {
                    log.warn("Execution {} has no mavenCommand, skipping", id);
                    continue;
                }

                log.info("Picked up execution {}: {}", id, exec.get("name"));
                scheduler.submit(() -> runExecution(id, mavenCmd));
                break;
            }
        } catch (Throwable e) {
            log.debug("Poll failed: {}", e.getMessage(), e);
        }
    }

    private void runExecution(String execId, String mavenCmd) {
        try {
            apiPatch("/api/v1/executions/" + execId + "/status", Map.of("status", "RUNNING"));

            String wsPath = properties.getWorkspacePath();
            Path execDir = Paths.get(wsPath).toAbsolutePath().normalize();
            Path cucumberJson = execDir.resolve("target/cucumber.json");

            log.info("Running: {} in {}", mavenCmd, execDir);

            ProcessBuilder pb = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("cmd.exe", "/c", mavenCmd);
            } else {
                pb.command("sh", "-c", mavenCmd);
            }
            pb.directory(execDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            runningProcesses.put(execId, process);

            // Drain stdout to a thread-local buffer (discarded) so the process doesn't block
            Thread drainer = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while (reader.readLine() != null) { /* discard */ }
                } catch (IOException ignored) {}
            }, "stdout-drainer-" + execId.substring(0, 8));
            drainer.setDaemon(true);
            drainer.start();

            int exitCode = process.waitFor();
            drainer.join(5000);
            runningProcesses.remove(execId);
            log.info("Execution {} exited with code {}", execId, exitCode);

            if (Files.exists(cucumberJson)) {
                log.info("Parsing results for execution {}", execId);
                parseAndPushResults(execId, cucumberJson.toFile());
                log.info("Results pushed for execution {}", execId);
                apiPatch("/api/v1/executions/" + execId + "/status", Map.of("status", "PASSED"));
                log.info("Execution {} status set to PASSED", execId);
            } else {
                log.warn("cucumber.json not found for execution {}", execId);
                apiPatch("/api/v1/executions/" + execId + "/status", Map.of("status", "FAILED"));
            }

        } catch (Throwable e) {
            log.error("Execution {} failed: {} - {}", execId, e.getClass().getSimpleName(), e.getMessage(), e);
            try {
                apiPatch("/api/v1/executions/" + execId + "/status", Map.of("status", "FAILED"));
            } catch (Throwable ignored) {}
            runningProcesses.remove(execId);
        }
    }

    private void parseAndPushResults(String execId, File cucumberJson) throws Exception {
        JsonNode root = objectMapper.readTree(cucumberJson);
        log.info("Parsing {} features for execution {}", root.size(), execId);

        for (int fi = 0; fi < root.size(); fi++) {
            JsonNode featureNode = root.get(fi);

            String featureName = featureNode.has("name") ? featureNode.get("name").asText() : "Unknown";
            String featureUri = featureNode.has("uri") ? featureNode.get("uri").asText() : null;

            Map<String, Object> featureBody = new HashMap<>();
            featureBody.put("featureName", featureName);
            featureBody.put("uri", featureUri);
            featureBody.put("status", "RUNNING");
            featureBody.put("durationMs", 0);

            Map<String, Object> featureResp = null;
            try {
                log.debug("Creating feature: POST /api/v1/executions/{}/features body={}", execId, featureBody);
                featureResp = apiPost("/api/v1/executions/" + execId + "/features", featureBody);
                log.debug("Feature response: {}", featureResp);
            } catch (Throwable e) {
                log.warn("Failed to create feature {}: {} - {}", featureName, e.getClass().getSimpleName(), e.getMessage(), e);
            }
            String featureId = featureResp != null ? (String) featureResp.get("id") : null;
            log.debug("Feature {}: id={}", featureName, featureId);

            JsonNode elements = featureNode.get("elements");
            if (elements == null || !elements.isArray()) continue;

            for (int ei = 0; ei < elements.size(); ei++) {
                JsonNode element = elements.get(ei);
                String type = element.has("type") ? element.get("type").asText() : "";
                String keyword = element.has("keyword") ? element.get("keyword").asText() : "";
                if (!"scenario".equals(type) && !"Scenario".equals(keyword)) continue;

                String scenarioName = element.has("name") ? element.get("name").asText() : "Unknown";
                String tags = "";
                JsonNode tagNode = element.get("tags");
                if (tagNode != null && tagNode.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode t : tagNode) {
                        if (sb.length() > 0) sb.append(",");
                        JsonNode nameNode = t.get("name");
                        if (nameNode != null) sb.append(nameNode.asText());
                    }
                    tags = sb.toString();
                }

                String scenarioStatus = "PASSED";
                String failureReason = null;
                long scenarioDuration = 0;

                JsonNode steps = element.get("steps");
                List<Map<String, Object>> stepPayloads = new ArrayList<>();
                if (steps != null && steps.isArray()) {
                    for (JsonNode stepNode : steps) {
                        String stepName = stepNode.has("name") ? stepNode.get("name").asText() : "Unknown";
                        String stepStatus = "PASSED";
                        long stepDuration = 0;
                        String stepLog = null;

                        JsonNode result = stepNode.get("result");
                        if (result != null) {
                            stepStatus = result.has("status") ? result.get("status").asText().toUpperCase() : "PASSED";
                            if (result.has("duration")) {
                                stepDuration = result.get("duration").asLong() / 1_000_000;
                            }
                            if (result.has("error_message")) {
                                stepLog = result.get("error_message").asText();
                            }
                        }

                        if ("FAILED".equals(stepStatus)) scenarioStatus = "FAILED";
                        scenarioDuration += stepDuration;

                        Map<String, Object> sp = new HashMap<>();
                        sp.put("stepName", stepName);
                        sp.put("status", stepStatus);
                        sp.put("durationMs", stepDuration);
                        sp.put("logText", stepLog != null ? stepLog : "");
                        stepPayloads.add(sp);
                    }
                }

                if ("FAILED".equals(scenarioStatus) && steps != null) {
                    for (JsonNode stepNode : steps) {
                        JsonNode result = stepNode.get("result");
                        if (result != null && result.has("status") && "failed".equals(result.get("status").asText()) && result.has("error_message")) {
                            String msg = result.get("error_message").asText();
                            failureReason = msg.length() > 1000 ? msg.substring(0, 1000) : msg;
                            break;
                        }
                    }
                }

                Map<String, Object> scenarioBody = new HashMap<>();
                scenarioBody.put("featureId", featureId);
                scenarioBody.put("scenarioName", scenarioName);
                scenarioBody.put("tags", tags);
                scenarioBody.put("status", scenarioStatus);
                scenarioBody.put("durationMs", scenarioDuration);
                if (failureReason != null) scenarioBody.put("failureReason", failureReason);

                Map<String, Object> scenarioResp = null;
                try {
                    scenarioResp = apiPost("/api/v1/executions/" + execId + "/scenarios", scenarioBody);
                } catch (Exception e) {
                    log.warn("Failed to create scenario {}: {}", scenarioName, e.getMessage());
                }
                String scenarioId = scenarioResp != null ? (String) scenarioResp.get("id") : null;

                for (Map<String, Object> stepPayload : stepPayloads) {
                    if (scenarioId != null) {
                        try {
                            apiPost("/api/v1/executions/" + execId + "/scenarios/" + scenarioId + "/steps", stepPayload);
                        } catch (Exception e) {
                            log.warn("Failed to create step: {}", e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private String apiGet(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getApiUrl() + path))
                .header("Authorization", "Bearer " + properties.getApiToken())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return resp.statusCode() == 200 ? resp.body() : null;
    }

    private Map<String, Object> apiPost(String path, Map<String, Object> body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getApiUrl() + path))
                .header("Authorization", "Bearer " + properties.getApiToken())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300 && !resp.body().isBlank()) {
            return objectMapper.readValue(resp.body(), new TypeReference<>() {});
        }
        return null;
    }

    private void apiPatch(String path, Map<String, Object> body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getApiUrl() + path))
                .header("Authorization", "Bearer " + properties.getApiToken())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }
}
