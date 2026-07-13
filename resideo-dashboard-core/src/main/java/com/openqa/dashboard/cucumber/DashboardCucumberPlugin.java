package com.openqa.dashboard.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Optional Cucumber plugin that posts results to the Resideo Dashboard API in real time.
 *
 * Activate in cucumber.properties:
 *   cucumber.plugin=com.openqa.dashboard.cucumber.DashboardCucumberPlugin
 *   openqa.dashboard.url=http://localhost:8080
 *   openqa.dashboard.api-key=optional-secret
 */
public class DashboardCucumberPlugin implements ConcurrentEventListener {

    private static final String DASHBOARD_URL = System.getProperty("openqa.dashboard.url", "http://localhost:8080");
    private static final String API_KEY = System.getProperty("openqa.dashboard.api-key", "");
    private static final String EXECUTION_ID = System.getProperty("openqa.execution.id", "");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final TestRun testRun = new TestRun();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::onTestRunStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
    }

    private void onTestRunStarted(TestRunStarted event) {
        testRun.startTime = event.getInstant();
        if (!EXECUTION_ID.isEmpty()) {
            testRun.executionId = EXECUTION_ID;
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        if (!EXECUTION_ID.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("step", event.getTestStep().getCodeLocation());
            data.put("status", event.getResult().getStatus().name());
            data.put("duration", event.getResult().getDuration().toMillis());
            post("/api/v1/executions/" + EXECUTION_ID + "/logs", data);
        }
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        testRun.scenarios.add(event);
    }

    private void onTestRunFinished(TestRunFinished event) {
        if (!EXECUTION_ID.isEmpty()) {
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("status", event.getResult().getStatus().name());
            post("/api/v1/executions/" + EXECUTION_ID + "/status", statusUpdate);
        }
    }

    private void post(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(DASHBOARD_URL + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            if (!API_KEY.isEmpty()) {
                builder.header("Authorization", "Bearer " + API_KEY);
            }
            httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            System.err.println("[OpenQADashboard] Failed to post to " + path + ": " + e.getMessage());
        }
    }

    private static class TestRun {
        String executionId;
        Instant startTime;
        List<TestCaseFinished> scenarios = new ArrayList<>();
    }
}
