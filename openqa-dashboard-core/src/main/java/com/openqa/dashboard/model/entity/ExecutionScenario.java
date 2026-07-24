package com.openqa.dashboard.model.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "scenarios")
public class ExecutionScenario {

    @Id
    private String id;

    @Column(name = "execution_id", nullable = false)
    private String executionId;

    @Column(name = "feature_id")
    private String featureId;

    @Column(name = "name")
    private String scenarioName;

    @Column(name = "tags")
    private String tags;

    @Column(name = "status")
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs = 0L;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "device_name")
    private String deviceName;

    public ExecutionScenario() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getFeatureId() { return featureId; }
    public void setFeatureId(String featureId) { this.featureId = featureId; }
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
}
