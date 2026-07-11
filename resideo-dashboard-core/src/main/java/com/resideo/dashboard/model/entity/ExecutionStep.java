package com.resideo.dashboard.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_steps", indexes = {
    @Index(name = "idx_steps_scenario_id", columnList = "scenarioId")
})
public class ExecutionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenario_id", nullable = false)
    private UUID scenarioId;

    @Column(name = "step_name", nullable = false, columnDefinition = "TEXT")
    private String stepName;

    @Column(length = 20)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "log_text", columnDefinition = "TEXT")
    private String logText;

    @Column(name = "screenshot_path", columnDefinition = "TEXT")
    private String screenshotPath;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public ExecutionStep() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getScenarioId() { return scenarioId; }
    public void setScenarioId(UUID scenarioId) { this.scenarioId = scenarioId; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getLogText() { return logText; }
    public void setLogText(String logText) { this.logText = logText; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
