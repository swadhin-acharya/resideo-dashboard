package com.resideo.dashboard.model.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "execution_features", indexes = {
    @Index(name = "idx_features_execution_id", columnList = "executionId")
})
public class ExecutionFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "feature_name", nullable = false, length = 255)
    private String featureName;

    @Column(columnDefinition = "TEXT")
    private String uri;

    @Column(length = 20)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "pass_count")
    private Integer passCount = 0;

    @Column(name = "fail_count")
    private Integer failCount = 0;

    @Column(name = "skip_count")
    private Integer skipCount = 0;

    public ExecutionFeature() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public Integer getPassCount() { return passCount; }
    public void setPassCount(Integer passCount) { this.passCount = passCount; }
    public Integer getFailCount() { return failCount; }
    public void setFailCount(Integer failCount) { this.failCount = failCount; }
    public Integer getSkipCount() { return skipCount; }
    public void setSkipCount(Integer skipCount) { this.skipCount = skipCount; }
}
