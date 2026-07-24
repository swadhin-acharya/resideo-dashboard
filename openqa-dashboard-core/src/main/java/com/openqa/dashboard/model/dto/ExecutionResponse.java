package com.openqa.dashboard.model.dto;

import java.util.ArrayList;
import java.util.List;

public class ExecutionResponse {
    private String id;
    private String name;
    private String framework;
    private String branch;
    private String machine;
    private String platform;
    private String environment;
    private String buildNumber;
    private String status;
    private String executionType;
    private String startTime;
    private String endTime;
    private Long durationMs;
    private Integer passCount;
    private Integer failCount;
    private Integer skipCount;
    private Integer totalCount;
    private String reportPath;
    private String triggerSource;
    private String mavenCommand;
    private String createdAt;
    private String updatedAt;
    private List<FeatureSummary> features = new ArrayList<>();
    private List<ScenarioInfo> scenarios = new ArrayList<>();

    public static ExecutionResponse from(com.openqa.dashboard.model.entity.Execution e) {
        ExecutionResponse r = new ExecutionResponse();
        r.id = e.getId();
        r.name = e.getName();
        r.framework = e.getFramework();
        r.branch = e.getBranch();
        r.machine = e.getMachine();
        r.platform = e.getPlatform();
        r.environment = e.getEnvironment();
        r.buildNumber = e.getBuildNumber();
        r.status = e.getStatus();
        r.executionType = e.getExecutionType();
        r.startTime = e.getStartTime();
        r.endTime = e.getEndTime();
        r.durationMs = e.getDurationMs();
        r.passCount = e.getPassCount();
        r.failCount = e.getFailCount();
        r.skipCount = e.getSkipCount();
        r.totalCount = e.getTotalCount();
        r.reportPath = e.getReportPath();
        r.triggerSource = e.getTriggerSource();
        r.mavenCommand = e.getMavenCommand();
        r.createdAt = e.getCreatedAt();
        r.updatedAt = e.getUpdatedAt();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getMachine() { return machine; }
    public void setMachine(String machine) { this.machine = machine; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getBuildNumber() { return buildNumber; }
    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExecutionType() { return executionType; }
    public void setExecutionType(String executionType) { this.executionType = executionType; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public Integer getPassCount() { return passCount; }
    public void setPassCount(Integer passCount) { this.passCount = passCount; }
    public Integer getFailCount() { return failCount; }
    public void setFailCount(Integer failCount) { this.failCount = failCount; }
    public Integer getSkipCount() { return skipCount; }
    public void setSkipCount(Integer skipCount) { this.skipCount = skipCount; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }
    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }
    public String getMavenCommand() { return mavenCommand; }
    public void setMavenCommand(String mavenCommand) { this.mavenCommand = mavenCommand; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public List<FeatureSummary> getFeatures() { return features; }
    public void setFeatures(List<FeatureSummary> features) { this.features = features; }
    public List<ScenarioInfo> getScenarios() { return scenarios; }
    public void setScenarios(List<ScenarioInfo> scenarios) { this.scenarios = scenarios; }

    public static class FeatureSummary {
        private String id;
        private String featureName;
        private String status;
        private Long durationMs;
        private Integer passCount;
        private Integer failCount;
        private Integer skipCount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFeatureName() { return featureName; }
        public void setFeatureName(String featureName) { this.featureName = featureName; }
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

    public static class ScenarioInfo {
        private String id;
        private String featureId;
        private String scenarioName;
        private String tags;
        private String status;
        private Long durationMs;
        private String failureReason;
        private String deviceName;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
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
}
