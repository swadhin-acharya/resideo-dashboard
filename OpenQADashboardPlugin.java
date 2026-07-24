package com.yourproject.cucumber;

import io.cucumber.plugin.*;
import io.cucumber.plugin.event.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Cucumber plugin that pushes results to OpenQA Dashboard.
 *
 * <p>Register via {@code @CucumberOptions(plugin = "com.yourproject.cucumber.OpenQADashboardPlugin")}
 * or {@code --plugin com.yourproject.cucumber.OpenQADashboardPlugin} on CLI.
 *
 * <p>Configure via environment variables:
 * <table>
 *   <tr><td>OPENQA_URL</td><td>Dashboard URL (default http://localhost:8080)</td></tr>
 *   <tr><td>OPENQA_PROJECT</td><td>Execution name (default "Cucumber Suite")</td></tr>
 *   <tr><td>OPENQA_BRANCH</td><td>Branch name</td></tr>
 *   <tr><td>OPENQA_BUILD</td><td>Build number</td></tr>
 *   <tr><td>OPENQA_PLATFORM</td><td>ANDROID, IOS, WEB (default WEB)</td></tr>
 *   <tr><td>OPENQA_ENV</td><td>QA, STAGING, PRODUCTION (default QA)</td></tr>
 * </table>
 */
public class OpenQADashboardPlugin implements Plugin, EventListener {

    private static final String DEFAULT_URL = "http://localhost:8080";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private String dashboardUrl;
    private String executionId;
    private final Set<String> featureIds = new HashSet<>();
    private String currentFeatureUri = "";
    private String currentFeatureId = "";
    private String currentFeatureName = "";

    @Override
    public void setEventPublisher(EventPublisher pub) {
        dashboardUrl = env("OPENQA_URL", DEFAULT_URL);

        pub.registerHandlerFor(TestRunStarted.class, e -> onRunStarted());
        pub.registerHandlerFor(TestCaseStarted.class, this::onCaseStarted);
        pub.registerHandlerFor(TestCaseFinished.class, this::onCaseFinished);
        pub.registerHandlerFor(TestRunFinished.class, e -> onRunFinished());
    }

    // ── Run started – create execution ────────────────
    private void onRunStarted() {
        String json = """
                {"name":"%s","executionType":"REGRESSION","platform":"%s",
                 "branch":"%s","buildNumber":"%s","environment":"%s"}
                """.formatted(
                env("OPENQA_PROJECT", "Cucumber Suite"),
                env("OPENQA_PLATFORM", "WEB"),
                env("OPENQA_BRANCH", "develop"),
                env("OPENQA_BUILD", "local"),
                env("OPENQA_ENV", "QA")
        ).replaceAll("\\s+", " ").trim();

        executionId = post("/api/executions", json, "id");
        System.out.println("[OpenQA] Execution " + executionId + " created");
    }

    // ── Scenario started – register feature ───────────
    private void onCaseStarted(TestCaseStarted event) {
        TestCase tc = event.getTestCase();
        String uri = tc.getUri().toString();
        String fileName = uri.contains("/") ? uri.substring(uri.lastIndexOf('/') + 1) : uri;
        currentFeatureName = fileName.replace(".feature", "").replace("_", " ");

        if (!featureIds.contains(currentFeatureName)) {
            featureIds.add(currentFeatureName);
            currentFeatureUri = fileName;
        }
    }

    // ── Scenario finished – push result ───────────────
    private void onCaseFinished(TestCaseFinished event) {
        TestCase tc = event.getTestCase();
        String status = event.getResult().getStatus().name(); // PASSED / FAILED / SKIPPED
        long duration = event.getResult().getDuration().toMillis();

        // Ensure feature exists
        if (currentFeatureId.isEmpty() || !featureIds.contains(currentFeatureName)) {
            String fjson = """
                    {"featureName":"%s","uri":"%s","status":"RUNNING","durationMs":0}
                    """.formatted(currentFeatureName, currentFeatureUri);
            currentFeatureId = post("/api/executions/" + executionId + "/features", fjson, "id");
        }
        if (!currentFeatureId.isEmpty()) {
            String failMsg = event.getResult().getError() != null
                    ? event.getResult().getError().getMessage().replace("\"", "'")
                    : "";
            String tags = String.join(",", tc.getTags().stream()
                    .map(EventValue::toString).toList());
            String sjson = """
                    {"featureId":"%s","scenarioName":"%s","tags":"%s",
                     "status":"%s","durationMs":%d,"failureReason":"%s"}
                    """.formatted(
                    currentFeatureId, tc.getKeyword() + ": " + tc.getName(),
                    tags, status, duration, failMsg
            ).replaceAll("\\s+", " ").trim();
            post("/api/executions/" + executionId + "/scenarios", sjson, null);
        }
    }

    // ── Run finished – finalize ────────────────────────
    private void onRunFinished() {
        post("/api/executions/" + executionId + "/finish", "{}", null);
        System.out.println("[OpenQA] Finished: " + dashboardUrl + "/executions/" + executionId);
    }

    // ── HTTP helpers ───────────────────────────────────
    private String post(String path, String json, String field) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(dashboardUrl + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return field != null ? extract(res.body(), field) : null;
        } catch (Exception e) {
            System.err.println("[OpenQA] " + path + " → " + e.getClass().getSimpleName());
            return field != null ? "" : null;
        }
    }

    private static String extract(String body, String field) {
        String key = "\"" + field + "\":\"";
        int s = body.indexOf(key);
        if (s < 0) return "";
        s += key.length();
        int e = body.indexOf("\"", s);
        return e > s ? body.substring(s, e) : "";
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return v != null ? v : fallback;
    }
}
