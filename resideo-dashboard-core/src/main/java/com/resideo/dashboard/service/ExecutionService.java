package com.resideo.dashboard.service;

import com.resideo.dashboard.model.dto.ExecutionRequest;
import com.resideo.dashboard.model.dto.ExecutionResponse;
import com.resideo.dashboard.model.dto.ExecutionSummary;
import com.resideo.dashboard.model.dto.PagedResponse;
import com.resideo.dashboard.model.entity.*;
import com.resideo.dashboard.model.enums.ExecutionStatus;
import static com.resideo.dashboard.model.enums.ExecutionStatus.*;
import com.resideo.dashboard.model.enums.Platform;
import com.resideo.dashboard.model.enums.Visibility;
import com.resideo.dashboard.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);

    private final ExecutionRepository executionRepository;
    private final ExecutionFeatureRepository featureRepository;
    private final ExecutionScenarioRepository scenarioRepository;
    private final ExecutionLogRepository logRepository;
    private final ExecutionStepRepository stepRepository;

    public ExecutionService(ExecutionRepository executionRepository,
                            ExecutionFeatureRepository featureRepository,
                            ExecutionScenarioRepository scenarioRepository,
                            ExecutionLogRepository logRepository,
                            ExecutionStepRepository stepRepository) {
        this.executionRepository = executionRepository;
        this.featureRepository = featureRepository;
        this.scenarioRepository = scenarioRepository;
        this.logRepository = logRepository;
        this.stepRepository = stepRepository;
    }

    public ExecutionResponse create(ExecutionRequest request) {
        return create(request, null, null);
    }

    public ExecutionResponse create(ExecutionRequest request, UUID projectId, UUID userId) {
        Execution execution = new Execution();
        execution.setName(request.getName());
        execution.setBuildNumber(request.getBuildNumber());
        execution.setTriggeredBy(request.getTriggeredBy());
        execution.setBranch(request.getBranch());
        execution.setCommitHash(request.getCommitHash());
        if (request.getPlatform() != null) {
            execution.setPlatform(Platform.valueOf(request.getPlatform().toUpperCase()));
        }
        execution.setEnvironment(request.getEnvironment());
        execution.setFirmwareVersion(request.getFirmwareVersion());
        execution.setAppVersion(request.getAppVersion());
        execution.setExecutionType(request.getExecutionType());
        execution.setCucumberTags(request.getCucumberTags());
        if (request.getFeaturePaths() != null) {
            execution.setFeaturePaths(String.join(",", request.getFeaturePaths()));
        }
        execution.setMavenProfile(request.getMavenProfile());
        execution.setParallel(request.getParallel() != null ? request.getParallel() : false);
        execution.setRetryCount(request.getRetryCount() != null ? request.getRetryCount() : 0);
        execution.setJvmParams(request.getJvmParams());
        execution.setStatus(ExecutionStatus.PENDING);
        if (projectId != null) {
            execution.setProjectId(projectId);
        }
        if (userId != null) {
            execution.setUserId(userId);
        }
        if (request.getVisibility() != null) {
            execution.setVisibility(Visibility.valueOf(request.getVisibility().toUpperCase()));
        } else {
            execution.setVisibility(Visibility.PROJECT);
        }
        execution.setMachineName(request.getMachineName());
        execution.setSource(request.getSource() != null ? request.getSource() : "DASHBOARD");
        execution.setMavenCommand(buildMavenCommand(request));

        execution = executionRepository.save(execution);
        log.info("Created execution: {} (project={})", execution.getId(), execution.getProjectId());

        ExecutionResponse r = ExecutionResponse.from(execution);
        r.setMavenCommand(execution.getMavenCommand());
        return r;
    }

    private String buildMavenCommand(ExecutionRequest request) {
        if (request.getMavenCommand() != null && !request.getMavenCommand().isBlank()) {
            return request.getMavenCommand();
        }
        StringBuilder cmd = new StringBuilder("mvn test");
        String tags = request.getCucumberTags();
        if (tags != null && !tags.isBlank()) {
            cmd.append(" -Dcucumber.filter.tags=\"").append(tags).append("\"");
        }
        if (request.getFeaturePaths() != null && !request.getFeaturePaths().isEmpty()) {
            cmd.append(" -Dcucumber.features=\"").append(String.join(",", request.getFeaturePaths())).append("\"");
        }
        if (request.getMavenProfile() != null && !request.getMavenProfile().isBlank()) {
            cmd.append(" -P").append(request.getMavenProfile());
        }
        if (request.getPlatform() != null) {
            cmd.append(" -Dplatform=").append(request.getPlatform().toLowerCase());
        }
        if (request.getEnvironment() != null) {
            cmd.append(" -Denvironment=").append(request.getEnvironment());
        }
        if (request.getFirmwareVersion() != null) {
            cmd.append(" -Dfirmware.version=").append(request.getFirmwareVersion());
        }
        if (request.getAppVersion() != null) {
            cmd.append(" -Dapp.version=").append(request.getAppVersion());
        }
        if (Boolean.TRUE.equals(request.getParallel())) {
            cmd.append(" -Dparallel=true");
        }
        if (request.getRetryCount() != null && request.getRetryCount() > 0) {
            cmd.append(" -DretryCount=").append(request.getRetryCount());
        }
        if (request.getJvmParams() != null && !request.getJvmParams().isBlank()) {
            cmd.append(" ").append(request.getJvmParams());
        }
        if (request.getAdditionalConfig() != null) {
            for (Map.Entry<String, String> entry : request.getAdditionalConfig().entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    cmd.append(" -D").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }
        return cmd.toString();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExecutionResponse> list(int page, int size, String status, String platform,
                                                   String environment, String firmware, String appVersion) {
        return list(page, size, status, platform, environment, firmware, appVersion, null, null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExecutionResponse> list(int page, int size, String status, String platform,
                                                   String environment, String firmware, String appVersion,
                                                   UUID projectId, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        ExecutionStatus statusEnum = status != null ? ExecutionStatus.valueOf(status.toUpperCase()) : null;
        List<String> visibilities = List.of("PROJECT", "ORGANIZATION");
        if (userId != null) {
            visibilities = List.of("PRIVATE", "PROJECT", "ORGANIZATION");
        }
        Page<Execution> execs = executionRepository.search(projectId, statusEnum, platform, environment, firmware, appVersion, visibilities, pageable);
        List<ExecutionResponse> content = execs.getContent().stream()
                .map(ExecutionResponse::from)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page, size, execs.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ExecutionResponse getById(UUID id) {
        return getById(id, null);
    }

    @Transactional(readOnly = true)
    public ExecutionResponse getById(UUID id, UUID projectId) {
        Execution execution;
        if (projectId != null) {
            execution = executionRepository.findByIdAndProjectId(id, projectId)
                    .orElseThrow(() -> new NoSuchElementException("Execution not found: " + id));
        } else {
            execution = executionRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Execution not found: " + id));
        }
        ExecutionResponse response = ExecutionResponse.from(execution);

        List<ExecutionFeature> features = featureRepository.findByExecutionId(id);
        List<ExecutionResponse.FeatureSummary> summaries = features.stream().map(f -> {
            ExecutionResponse.FeatureSummary s = new ExecutionResponse.FeatureSummary();
            s.setId(f.getId());
            s.setFeatureName(f.getFeatureName());
            s.setStatus(f.getStatus());
            s.setDurationMs(f.getDurationMs());
            s.setPassCount(f.getPassCount() != null ? f.getPassCount() : 0);
            s.setFailCount(f.getFailCount() != null ? f.getFailCount() : 0);
            s.setSkipCount(f.getSkipCount() != null ? f.getSkipCount() : 0);
            return s;
        }).collect(Collectors.toList());
        response.setFeatures(summaries);

        List<ExecutionScenario> scenarios = scenarioRepository.findByExecutionId(id);
        List<ExecutionResponse.ScenarioInfo> scenarioInfos = scenarios.stream().map(s -> {
            ExecutionResponse.ScenarioInfo si = new ExecutionResponse.ScenarioInfo();
            si.setId(s.getId());
            si.setFeatureId(s.getFeatureId());
            si.setScenarioName(s.getScenarioName());
            si.setTags(s.getTags());
            si.setStatus(s.getStatus());
            si.setDurationMs(s.getDurationMs());
            si.setFailureReason(s.getFailureReason());
            si.setDeviceName(s.getDeviceName());
            return si;
        }).collect(Collectors.toList());
        response.setScenarios(scenarioInfos);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> listWithReports(UUID projectId) {
        List<Execution> execs = executionRepository.findWithReports(projectId);
        return execs.stream().map(ExecutionResponse::from).collect(Collectors.toList());
    }

    public ExecutionResponse updateName(UUID id, String name) {
        Execution execution = executionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Execution not found: " + id));
        execution.setName(name);
        execution = executionRepository.save(execution);
        return ExecutionResponse.from(execution);
    }

    public ExecutionResponse updateStatus(UUID id, String status) {
        Execution execution = executionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Execution not found: " + id));
        execution.setStatus(ExecutionStatus.valueOf(status.toUpperCase()));
        if (status.equalsIgnoreCase("PASSED") || status.equalsIgnoreCase("FAILED") || status.equalsIgnoreCase("ABORTED")) {
            execution.setEndTime(Instant.now());
            if (execution.getStartTime() != null) {
                execution.setDurationMs(java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());
            }
        }
        execution = executionRepository.save(execution);
        return ExecutionResponse.from(execution);
    }

    public void delete(UUID id) {
        logRepository.deleteByExecutionId(id);
        stepRepository.deleteAll(stepRepository.findAll()); // bulk in production
        scenarioRepository.deleteByExecutionId(id);
        featureRepository.deleteByExecutionId(id);
        executionRepository.deleteById(id);
        log.info("Deleted execution: {}", id);
    }

    @Transactional(readOnly = true)
    public ExecutionSummary getSummary() {
        return getSummary(null);
    }

    @Transactional(readOnly = true)
    public ExecutionSummary getSummary(UUID projectId) {
        ExecutionSummary summary = new ExecutionSummary();
        if (projectId != null) {
            summary.setTotalExecutions(executionRepository.countByProjectId(projectId));
            summary.setPassed(executionRepository.countByProjectIdAndStatus(projectId, ExecutionStatus.PASSED));
            summary.setFailed(executionRepository.countByProjectIdAndStatus(projectId, ExecutionStatus.FAILED));
            summary.setAborted(executionRepository.countByProjectIdAndStatus(projectId, ExecutionStatus.ABORTED));
            summary.setRunning(executionRepository.countByProjectIdAndStatus(projectId, ExecutionStatus.RUNNING));
        } else {
            summary.setTotalExecutions(executionRepository.count());
            summary.setPassed(executionRepository.countByStatus(ExecutionStatus.PASSED));
            summary.setFailed(executionRepository.countByStatus(ExecutionStatus.FAILED));
            summary.setAborted(executionRepository.countByStatus(ExecutionStatus.ABORTED));
            summary.setRunning(executionRepository.countByStatus(ExecutionStatus.RUNNING));
        }
        summary.setPassRate(summary.getTotalExecutions() > 0
                ? (double) summary.getPassed() / summary.getTotalExecutions() * 100 : 0);

        List<ExecutionScenario> scenarios = scenarioRepository.findAll();
        summary.setTotalScenarios(scenarios.size());
        summary.setTotalPassed(scenarios.stream().filter(s -> "PASSED".equals(s.getStatus())).count());
        summary.setTotalFailed(scenarios.stream().filter(s -> "FAILED".equals(s.getStatus())).count());
        summary.setTotalSkipped(scenarios.stream().filter(s -> "SKIPPED".equals(s.getStatus())).count());
        return summary;
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> getPendingAgentExecutions(UUID userId) {
        return executionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, ExecutionStatus.PENDING).stream()
                .map(ExecutionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> getRunningExecutions() {
        return getRunningExecutions(null);
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> getRunningExecutions(UUID projectId) {
        if (projectId != null) {
            return executionRepository.findByProjectIdAndStatus(projectId, ExecutionStatus.RUNNING).stream()
                    .map(ExecutionResponse::from)
                    .collect(Collectors.toList());
        }
        return executionRepository.findByStatus(ExecutionStatus.RUNNING).stream()
                .map(ExecutionResponse::from)
                .collect(Collectors.toList());
    }

    public ExecutionFeature addFeature(UUID executionId, String featureName, String uri, String status, Long durationMs) {
        ExecutionFeature feature = new ExecutionFeature();
        feature.setExecutionId(executionId);
        feature.setFeatureName(featureName);
        feature.setUri(uri);
        feature.setStatus(status);
        feature.setDurationMs(durationMs);
        return featureRepository.save(feature);
    }

    public ExecutionScenario addScenario(UUID executionId, UUID featureId, String scenarioName, String tags,
                                          String status, Long durationMs, String failureReason, String deviceName) {
        ExecutionScenario scenario = new ExecutionScenario();
        scenario.setExecutionId(executionId);
        scenario.setFeatureId(featureId);
        scenario.setScenarioName(scenarioName);
        scenario.setTags(tags);
        scenario.setStatus(status);
        scenario.setDurationMs(durationMs);
        scenario.setFailureReason(failureReason);
        scenario.setDeviceName(deviceName);
        scenario = scenarioRepository.save(scenario);

        executionRepository.findById(executionId).ifPresent(exec -> {
            exec.setTotalCount(exec.getTotalCount() + 1);
            if ("PASSED".equals(status)) exec.setPassCount(exec.getPassCount() + 1);
            else if ("FAILED".equals(status)) {
                exec.setFailCount(exec.getFailCount() + 1);
                exec.setStatus(FAILED);
            } else if ("SKIPPED".equals(status)) exec.setSkipCount(exec.getSkipCount() + 1);
            executionRepository.save(exec);
        });

        if (featureId != null) {
            featureRepository.findById(featureId).ifPresent(feature -> {
                feature.setPassCount(feature.getPassCount() + ("PASSED".equals(status) ? 1 : 0));
                feature.setFailCount(feature.getFailCount() + ("FAILED".equals(status) ? 1 : 0));
                feature.setSkipCount(feature.getSkipCount() + ("SKIPPED".equals(status) ? 1 : 0));
                if (durationMs != null) {
                    feature.setDurationMs(feature.getDurationMs() != null
                            ? feature.getDurationMs() + durationMs : durationMs);
                }
                feature.setStatus(feature.getFailCount() > 0 ? "FAILED" : feature.getPassCount() > 0 ? "PASSED" : "RUNNING");
                featureRepository.save(feature);
            });
        }

        return scenario;
    }

    public void completeFeature(UUID featureId) {
        featureRepository.findById(featureId).ifPresent(feature -> {
            String featureStatus = feature.getFailCount() > 0 ? "FAILED" : "PASSED";
            feature.setStatus(featureStatus);
            featureRepository.save(feature);
        });
    }

    public void appendLog(UUID executionId, String level, String message) {
        ExecutionLog logEntry = new ExecutionLog();
        logEntry.setExecutionId(executionId);
        logEntry.setLevel(level);
        logEntry.setMessage(message);
        logRepository.save(logEntry);
    }

    public List<Map<String, Object>> getLogs(UUID executionId) {
        return logRepository.findByExecutionIdOrderByTimestampAsc(executionId).stream()
                .map(l -> Map.<String, Object>of(
                        "level", l.getLevel(),
                        "message", l.getMessage(),
                        "timestamp", l.getTimestamp().toString()
                ))
                .collect(Collectors.toList());
    }

    public ExecutionStep addStep(UUID scenarioId, String stepName, String status, Long durationMs, String logText) {
        ExecutionStep step = new ExecutionStep();
        step.setScenarioId(scenarioId);
        step.setStepName(stepName);
        step.setStatus(status);
        step.setDurationMs(durationMs);
        step.setLogText(logText);
        return stepRepository.save(step);
    }

    public List<Map<String, Object>> getStepsByScenario(UUID scenarioId) {
        return stepRepository.findByScenarioId(scenarioId).stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getId());
                    m.put("stepName", s.getStepName());
                    m.put("status", s.getStatus());
                    m.put("durationMs", s.getDurationMs());
                    m.put("logText", s.getLogText() != null ? s.getLogText() : "");
                    if (s.getScreenshotPath() != null) {
                        m.put("screenshotPath", s.getScreenshotPath());
                    }
                    return m;
                })
                .collect(Collectors.toList());
    }
}
