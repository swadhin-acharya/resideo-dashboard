package com.resideo.dashboard.model.dto;

import com.resideo.dashboard.model.entity.Execution;
import com.resideo.dashboard.model.entity.ExecutionScenario;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExecutionResponse {

    private UUID id;
    private String name;
    private String buildNumber;
    private String triggeredBy;
    private String branch;
    private String commitHash;
    private String platform;
    private String environment;
    private String firmwareVersion;
    private String appVersion;
    private String executionType;
    private String cucumberTags;
    private String featurePaths;
    private String mavenCommand;
    private String status;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private int passCount;
    private int failCount;
    private int skipCount;
    private int totalCount;
    private String reportPath;
    private String logPath;
    private List<FeatureSummary> features;
    private List<ScenarioInfo> scenarios;
    private String visibility;
    private String machineName;
    private String source;
    private UUID projectId;
    private UUID userId;
    private Instant createdAt;

    public static ExecutionResponse from(Execution e) {
        ExecutionResponse r = new ExecutionResponse();
        r.id = e.getId();
        r.name = e.getName();
        r.buildNumber = e.getBuildNumber();
        r.triggeredBy = e.getTriggeredBy();
        r.branch = e.getBranch();
        r.commitHash = e.getCommitHash();
        r.platform = e.getPlatform() != null ? e.getPlatform().name() : null;
        r.environment = e.getEnvironment();
        r.firmwareVersion = e.getFirmwareVersion();
        r.appVersion = e.getAppVersion();
        r.executionType = e.getExecutionType();
        r.cucumberTags = e.getCucumberTags();
        r.featurePaths = e.getFeaturePaths();
        r.status = e.getStatus().name();
        r.startTime = e.getStartTime();
        r.endTime = e.getEndTime();
        r.durationMs = e.getDurationMs();
        r.passCount = e.getPassCount() != null ? e.getPassCount() : 0;
        r.failCount = e.getFailCount() != null ? e.getFailCount() : 0;
        r.skipCount = e.getSkipCount() != null ? e.getSkipCount() : 0;
        r.totalCount = e.getTotalCount() != null ? e.getTotalCount() : 0;
        r.reportPath = e.getReportPath();
        r.visibility = e.getVisibility() != null ? e.getVisibility().name() : null;
        r.machineName = e.getMachineName();
        r.source = e.getSource();
        r.projectId = e.getProjectId();
        r.userId = e.getUserId();
        r.createdAt = e.getCreatedAt();
        r.mavenCommand = e.getMavenCommand();
        return r;
    }

    public void setMavenCommand(String cmd) { this.mavenCommand = cmd; }

    public static class FeatureSummary {
        private UUID id;
        private String featureName;
        private String status;
        private Long durationMs;
        private int passCount;
        private int failCount;
        private int skipCount;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getFeatureName() { return featureName; }
        public void setFeatureName(String featureName) { this.featureName = featureName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getDurationMs() { return durationMs; }
        public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
        public int getPassCount() { return passCount; }
        public void setPassCount(int passCount) { this.passCount = passCount; }
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
        public int getSkipCount() { return skipCount; }
        public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
    }

    public static class ScenarioInfo {
        private UUID id;
        private UUID featureId;
        private String scenarioName;
        private String tags;
        private String status;
        private Long durationMs;
        private String failureReason;
        private String deviceName;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
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
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBuildNumber() { return buildNumber; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getBranch() { return branch; }
    public String getCommitHash() { return commitHash; }
    public String getPlatform() { return platform; }
    public String getEnvironment() { return environment; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public String getAppVersion() { return appVersion; }
    public String getExecutionType() { return executionType; }
    public String getCucumberTags() { return cucumberTags; }
    public void setCucumberTags(String cucumberTags) { this.cucumberTags = cucumberTags; }
    public String getFeaturePaths() { return featurePaths; }
    public void setFeaturePaths(String featurePaths) { this.featurePaths = featurePaths; }
    public String getMavenCommand() { return mavenCommand; }
    public String getStatus() { return status; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Long getDurationMs() { return durationMs; }
    public int getPassCount() { return passCount; }
    public int getFailCount() { return failCount; }
    public int getSkipCount() { return skipCount; }
    public int getTotalCount() { return totalCount; }
    public String getReportPath() { return reportPath; }
    public String getLogPath() { return logPath; }
    public void setLogPath(String logPath) { this.logPath = logPath; }
    public List<FeatureSummary> getFeatures() { return features; }
    public void setFeatures(List<FeatureSummary> features) { this.features = features; }
    public List<ScenarioInfo> getScenarios() { return scenarios; }
    public void setScenarios(List<ScenarioInfo> scenarios) { this.scenarios = scenarios; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Instant getCreatedAt() { return createdAt; }
}
