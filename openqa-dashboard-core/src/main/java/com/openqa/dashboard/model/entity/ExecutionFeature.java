package com.openqa.dashboard.model.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "features")
public class ExecutionFeature {

    @Id
    private String id;

    @Column(name = "execution_id", nullable = false)
    private String executionId;

    @Column(name = "name")
    private String featureName;

    @Column(name = "uri")
    private String uri;

    @Column(name = "status")
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs = 0L;

    @Column(name = "pass_count")
    private Integer passCount = 0;

    @Column(name = "fail_count")
    private Integer failCount = 0;

    @Column(name = "skip_count")
    private Integer skipCount = 0;

    public ExecutionFeature() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
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
