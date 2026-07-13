package com.openqa.dashboard.client.cucumber;

import com.openqa.dashboard.client.DashboardReporter;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cucumber plugin that publishes execution events to the Resideo Dashboard in real time.
 *
 * Activate via:
 *   cucumber.properties:  cucumber.plugin=com.openqa.dashboard.client.cucumber.DashboardCucumberPlugin
 *
 * Configuration (system properties or environment variables):
 *   OPENQA_DASHBOARD_URL   or  resideo.dashboard.url   = http://localhost:8080  (default)
 *   OPENQA_API_TOKEN       or  resideo.api.token       = rd_xxx... (optional)
 *   OPENQA_EXECUTION_ID    or  resideo.execution.id    = pre-created execution ID (optional)
 *   OPENQA_PROJECT_ID      or  resideo.project.id      = project UUID (optional)
 */
public class DashboardCucumberPlugin implements ConcurrentEventListener {

    private static final String DEFAULT_URL = "http://localhost:8080";

    private final DashboardReporter reporter;
    private final String executionId;
    private final String projectId;
    private final boolean autoCreate;

    private String resolvedExecutionId;
    private String currentFeatureId;
    private String currentFeatureName;
    private String currentScenarioId;
    private final Map<String, String> featureIds = new LinkedHashMap<>();
    private final Map<String, String> scenarioIds = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    public DashboardCucumberPlugin() {
        this.reporter = new DashboardReporter(getEnv("OPENQA_DASHBOARD_URL", "resideo.dashboard.url", DEFAULT_URL),
                                              getEnv("OPENQA_API_TOKEN", "resideo.api.token", null));
        this.executionId = getEnv("OPENQA_EXECUTION_ID", "resideo.execution.id", null);
        this.projectId = getEnv("OPENQA_PROJECT_ID", "resideo.project.id", null);
        this.autoCreate = (executionId == null || executionId.isEmpty());
    }

    DashboardCucumberPlugin(DashboardReporter reporter, String executionId, String projectId) {
        this.reporter = reporter;
        this.executionId = executionId;
        this.projectId = projectId;
        this.autoCreate = (executionId == null || executionId.isEmpty());
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::onTestRunStarted);
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
    }

    private void onTestRunStarted(TestRunStarted event) {
        if (autoCreate) {
            try {
                var req = new DashboardReporter.CreateExecutionRequest();
                req.name = "Cucumber Run " + new Date();
                req.source = "CUCUMBER";
                req.machineName = getHostname();
                req.platform = "ANDROID";
                req.environment = "QA";
                req.firmwareVersion = "1.3605.1550";
                req.appVersion = "3.0.0";
                req.executionType = "REGRESSION";
                req.branch = "main";
                req.triggeredBy = System.getProperty("user.name", "cucumber");
                DashboardReporter.Execution exec;
                if (projectId != null && !projectId.isEmpty()) {
                    exec = reporter.createExecution(req, projectId);
                } else {
                    exec = reporter.createExecution(req);
                }
                resolvedExecutionId = exec.id;
                log("Created execution: " + resolvedExecutionId);
            } catch (Exception e) {
                System.err.println("[DashboardCucumberPlugin] Failed to create execution: " + e.getMessage());
                resolvedExecutionId = null;
            }
        } else {
            resolvedExecutionId = executionId;
        }
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        if (resolvedExecutionId == null) return;
        String uri = event.getTestCase().getUri().toString();
        String featureName = extractFeatureName(uri);
        if (featureName != null && !featureName.equals(currentFeatureName)) {
            currentFeatureName = featureName;
            currentFeatureId = featureIds.get(featureName);
            if (currentFeatureId == null) {
                try {
                    var req = new DashboardReporter.FeatureRequest();
                    req.featureName = featureName;
                    req.uri = uri;
                    req.status = "RUNNING";
                    DashboardReporter.Feature feat = reporter.addFeature(resolvedExecutionId, req);
                    currentFeatureId = feat.id;
                    featureIds.put(featureName, currentFeatureId);
                } catch (Exception e) {
                    System.err.println("[DashboardCucumberPlugin] Failed to add feature: " + e.getMessage());
                }
            }
        }

        String scenarioName = event.getTestCase().getName();
        String scenarioKey = event.getTestCase().getUri() + ":" + event.getTestCase().getLine();
        if (!scenarioIds.containsKey(scenarioKey)) {
            try {
                var req = new DashboardReporter.ScenarioRequest();
                req.featureId = currentFeatureId;
                req.scenarioName = scenarioName;
                List<String> tagList = new ArrayList<>(event.getTestCase().getTags());
                req.tags = tagList.isEmpty() ? null : String.join(",", tagList);
                req.status = "RUNNING";
                DashboardReporter.Scenario scen = reporter.addScenario(resolvedExecutionId, req);
                currentScenarioId = scen.id;
                scenarioIds.put(scenarioKey, currentScenarioId);
            } catch (Exception e) {
                System.err.println("[DashboardCucumberPlugin] Failed to add scenario: " + e.getMessage());
            }
        } else {
            currentScenarioId = scenarioIds.get(scenarioKey);
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        if (resolvedExecutionId == null) return;
        if (event.getTestStep() instanceof HookTestStep) return;

        try {
            String stepText = event.getTestStep().getCodeLocation();
            if (event.getTestStep() instanceof PickleStepTestStep) {
                stepText = ((PickleStepTestStep) event.getTestStep()).getStep().getText();
            }
            String stepStatus = event.getResult().getStatus().name();
            long durationMs = event.getResult().getDuration().toMillis();

            reporter.addLog(resolvedExecutionId, "INFO",
                    "[" + stepStatus + "] " + stepText + " (" + durationMs + "ms)");

            if (currentScenarioId != null) {
                var stepReq = new DashboardReporter.StepRequest();
                stepReq.stepName = stepText;
                stepReq.status = stepStatus;
                stepReq.durationMs = durationMs;
                if (event.getResult().getError() != null) {
                    stepReq.logText = event.getResult().getError().getMessage();
                }
                reporter.addStep(currentScenarioId, stepReq);
            }
        } catch (Exception e) {
            // silently ignore step-level errors
        }
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        if (resolvedExecutionId == null) return;

        try {
            String scenarioName = event.getTestCase().getName();
            String status = event.getResult().getStatus().name();
            long durationMs = event.getResult().getDuration().toMillis();
            String failureReason = null;
            if (event.getResult().getError() != null) {
                failureReason = event.getResult().getError().getMessage();
            }

            String mappedStatus = "PASSED".equals(status) ? "PASSED" :
                                  "FAILED".equals(status) ? "FAILED" : "SKIPPED";

            String scenarioKey = event.getTestCase().getUri() + ":" + event.getTestCase().getLine();
            String sid = scenarioIds.get(scenarioKey);
            if (sid != null) {
                reporter.updateScenarioStatus(sid, mappedStatus, durationMs, failureReason);
            }

            log("Scenario [" + mappedStatus + "] " + scenarioName);
        } catch (Exception e) {
            System.err.println("[DashboardCucumberPlugin] Failed to send scenario: " + e.getMessage());
        }
    }

    private void onTestRunFinished(TestRunFinished event) {
        if (resolvedExecutionId == null) return;

        try {
            String finalStatus = event.getResult().getStatus().name();
            String mapped = "PASSED".equals(finalStatus) ? "PASSED" :
                           "FAILED".equals(finalStatus) ? "FAILED" : "ABORTED";
            reporter.updateStatus(resolvedExecutionId, mapped);

            for (Map.Entry<String, String> entry : featureIds.entrySet()) {
                try {
                    if (projectId != null && !projectId.isEmpty()) {
                        reporter.completeFeature(resolvedExecutionId, entry.getValue(), projectId);
                    } else {
                        reporter.completeFeature(resolvedExecutionId, entry.getValue());
                    }
                    log("Feature completed: " + entry.getKey());
                } catch (Exception e) {
                    System.err.println("[DashboardCucumberPlugin] Failed to complete feature " + entry.getKey() + ": " + e.getMessage());
                }
            }

            log("Execution completed: " + resolvedExecutionId + " -> " + mapped);
        } catch (Exception e) {
            System.err.println("[DashboardCucumberPlugin] Failed to update status: " + e.getMessage());
        }
    }

    private void log(String msg) {
        System.out.println("[OpenQADashboard] " + msg);
    }

    private static String extractFeatureName(String uri) {
        if (uri == null) return null;
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash >= 0) uri = uri.substring(lastSlash + 1);
        if (uri.endsWith(".feature")) uri = uri.substring(0, uri.length() - ".feature".length());
        return uri.replace("_", " ").replace("-", " ");
    }

    private static String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getEnv(String envVar, String sysProp, String defaultValue) {
        String val = System.getenv(envVar);
        if (val != null && !val.isEmpty()) return val;
        val = System.getProperty(sysProp);
        if (val != null && !val.isEmpty()) return val;
        return defaultValue;
    }
}
