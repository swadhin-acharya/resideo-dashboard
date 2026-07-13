package com.openqa.dashboard.model.entity;

import com.openqa.dashboard.model.enums.ExecutionStatus;
import com.openqa.dashboard.model.enums.Platform;
import com.openqa.dashboard.model.enums.Visibility;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "executions", indexes = {
    @Index(name = "idx_executions_status", columnList = "status"),
    @Index(name = "idx_executions_created_at", columnList = "createdAt DESC")
})
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "build_number", length = 50)
    private String buildNumber;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    @Column(length = 100)
    private String branch;

    @Column(name = "commit_hash", length = 100)
    private String commitHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Platform platform;

    @Column(length = 50)
    private String environment;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "execution_type", length = 30)
    private String executionType;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "cucumber_tags", columnDefinition = "TEXT")
    private String cucumberTags;

    @Column(name = "feature_paths", columnDefinition = "TEXT")
    private String featurePaths;

    @Column(name = "maven_profile", length = 50)
    private String mavenProfile;

    @Column(nullable = false)
    private Boolean parallel = false;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "jvm_params", columnDefinition = "TEXT")
    private String jvmParams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionStatus status = ExecutionStatus.PENDING;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "pass_count")
    private Integer passCount = 0;

    @Column(name = "fail_count")
    private Integer failCount = 0;

    @Column(name = "skip_count")
    private Integer skipCount = 0;

    @Column(name = "total_count")
    private Integer totalCount = 0;

    @Column(name = "report_path", columnDefinition = "TEXT")
    private String reportPath;

    @Column(name = "workspace_path", columnDefinition = "TEXT")
    private String workspacePath;

    @Column(name = "maven_command", columnDefinition = "TEXT")
    private String mavenCommand;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility = Visibility.PROJECT;

    @Column(name = "machine_name")
    private String machineName;

    @Column(length = 20)
    private String source = "DASHBOARD";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Execution() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBuildNumber() { return buildNumber; }
    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getExecutionType() { return executionType; }
    public void setExecutionType(String executionType) { this.executionType = executionType; }
    public String getCucumberTags() { return cucumberTags; }
    public void setCucumberTags(String cucumberTags) { this.cucumberTags = cucumberTags; }
    public String getFeaturePaths() { return featurePaths; }
    public void setFeaturePaths(String featurePaths) { this.featurePaths = featurePaths; }
    public String getMavenProfile() { return mavenProfile; }
    public void setMavenProfile(String mavenProfile) { this.mavenProfile = mavenProfile; }
    public Boolean getParallel() { return parallel; }
    public void setParallel(Boolean parallel) { this.parallel = parallel; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getJvmParams() { return jvmParams; }
    public void setJvmParams(String jvmParams) { this.jvmParams = jvmParams; }
    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
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
    public String getWorkspacePath() { return workspacePath; }
    public void setWorkspacePath(String workspacePath) { this.workspacePath = workspacePath; }
    public String getMavenCommand() { return mavenCommand; }
    public void setMavenCommand(String mavenCommand) { this.mavenCommand = mavenCommand; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
