package com.openqa.dashboard.model.dto;

public class FeatureRequest {
    private String executionId;
    private String featureName;
    private String uri;
    private String status;
    private Long durationMs;

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
}
