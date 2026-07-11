package com.resideo.dashboard.model.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "execution_scenarios", indexes = {
    @Index(name = "idx_scenarios_execution_id", columnList = "executionId"),
    @Index(name = "idx_scenarios_feature_id", columnList = "featureId")
})
public class ExecutionScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "feature_id")
    private UUID featureId;

    @Column(name = "scenario_name", nullable = false, length = 500)
    private String scenarioName;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(length = 20)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "screenshot_path", columnDefinition = "TEXT")
    private String screenshotPath;

    public ExecutionScenario() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public UUID getFeatureId() { return featureId; }
    public void setFeatureId(UUID featureId) { this.featureId = featureId; }
    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
}
