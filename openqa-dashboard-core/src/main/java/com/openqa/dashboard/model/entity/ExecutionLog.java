package com.openqa.dashboard.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "logs")
public class ExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_id", nullable = false)
    private String executionId;

    @Column(name = "level")
    private String level = "INFO";

    @Column(name = "message")
    private String message;

    @Column(name = "timestamp")
    private String timestamp;

    public ExecutionLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
