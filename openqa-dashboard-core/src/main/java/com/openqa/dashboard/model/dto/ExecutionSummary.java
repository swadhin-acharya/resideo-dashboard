package com.openqa.dashboard.model.dto;

import com.openqa.dashboard.model.entity.Execution;

public class ExecutionSummary {
    private String id;
    private String name;
    private String status;
    private String executionType;
    private String platform;
    private String startTime;
    private String endTime;
    private Long durationMs;
    private Integer passCount;
    private Integer failCount;
    private Integer skipCount;
    private Integer totalCount;
    private String triggerSource;
    private String createdAt;

    public static ExecutionSummary from(Execution e) {
        ExecutionSummary s = new ExecutionSummary();
        s.id = e.getId();
        s.name = e.getName();
        s.status = e.getStatus();
        s.executionType = e.getExecutionType();
        s.platform = e.getPlatform();
        s.startTime = e.getStartTime();
        s.endTime = e.getEndTime();
        s.durationMs = e.getDurationMs();
        s.passCount = e.getPassCount();
        s.failCount = e.getFailCount();
        s.skipCount = e.getSkipCount();
        s.totalCount = e.getTotalCount();
        s.triggerSource = e.getTriggerSource();
        s.createdAt = e.getCreatedAt();
        return s;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExecutionType() { return executionType; }
    public void setExecutionType(String executionType) { this.executionType = executionType; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
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
    public String getTriggerSource() { return triggerSource; }
    public void setTriggerSource(String triggerSource) { this.triggerSource = triggerSource; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
