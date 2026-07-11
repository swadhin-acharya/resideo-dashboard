package com.resideo.dashboard.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "thermostats", indexes = {
    @Index(name = "idx_thermostats_status", columnList = "status")
})
public class Thermostat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String name;

    @Column(name = "serial_port", length = 100)
    private String serialPort;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(length = 20)
    private String status = "ONLINE";

    @Column(name = "reserved_by", length = 100)
    private String reservedBy;

    @Column(name = "current_execution_id")
    private UUID currentExecutionId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Thermostat() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSerialPort() { return serialPort; }
    public void setSerialPort(String serialPort) { this.serialPort = serialPort; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReservedBy() { return reservedBy; }
    public void setReservedBy(String reservedBy) { this.reservedBy = reservedBy; }
    public UUID getCurrentExecutionId() { return currentExecutionId; }
    public void setCurrentExecutionId(UUID currentExecutionId) { this.currentExecutionId = currentExecutionId; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
