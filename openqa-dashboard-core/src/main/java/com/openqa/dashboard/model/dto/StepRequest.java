package com.openqa.dashboard.model.dto;

public class StepRequest {
    private String scenarioId;
    private String keyword;
    private String stepName;
    private String status;
    private Long durationMs;
    private String logText;

    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getLogText() { return logText; }
    public void setLogText(String logText) { this.logText = logText; }
}
