package com.openqa.dashboard.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "steps")
public class ExecutionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenario_id", nullable = false)
    private String scenarioId;

    @Column(name = "keyword")
    private String keyword = "";

    @Column(name = "name")
    private String stepName;

    @Column(name = "status")
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs = 0L;

    @Column(name = "log_text")
    private String logText;

    public ExecutionStep() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
