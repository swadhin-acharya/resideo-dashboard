package com.openqa.dashboard.service;

import com.openqa.dashboard.model.dto.*;
import com.openqa.dashboard.model.entity.*;
import com.openqa.dashboard.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final FeatureRepository featureRepository;
    private final ScenarioRepository scenarioRepository;
    private final StepRepository stepRepository;
    private final LogRepository logRepository;
    private final ReportRepository reportRepository;

    public ExecutionService(ExecutionRepository executionRepository,
                            FeatureRepository featureRepository,
                            ScenarioRepository scenarioRepository,
                            StepRepository stepRepository,
                            LogRepository logRepository,
                            ReportRepository reportRepository) {
        this.executionRepository = executionRepository;
        this.featureRepository = featureRepository;
        this.scenarioRepository = scenarioRepository;
        this.stepRepository = stepRepository;
        this.logRepository = logRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<ExecutionSummary> getAll() {
        return executionRepository.findAllOrderByCreatedAtDesc(PageRequest.of(0, 100))
                .getContent().stream()
                .map(ExecutionSummary::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExecutionResponse getById(String id) {
        return getExecution(id);
    }

    public ExecutionSummary create(ExecutionRequest request) {
        Execution execution = new Execution();
        execution.setName(request.getName() != null ? request.getName() : "Untitled");
        execution.setFramework(request.getFramework() != null ? request.getFramework() : "CUCUMBER");
        execution.setBranch(request.getBranch());
        execution.setMachine(request.getMachine());
        execution.setPlatform(request.getPlatform());
        execution.setEnvironment(request.getEnvironment());
        execution.setBuildNumber(request.getBuildNumber());
        execution.setExecutionType(request.getExecutionType() != null ? request.getExecutionType() : "REGRESSION");
        execution.setMavenCommand(request.getMavenCommand());
        execution.setStatus("RUNNING");
        execution.setStartTime(Instant.now().toString());
        execution.setTriggerSource("MANUAL");
        if (request.getType() != null) execution.setExecutionType(request.getType());
        if (request.getSource() != null) execution.setTriggerSource(request.getSource());
        if (request.getExecutionName() != null) execution.setName(request.getExecutionName());
        execution = executionRepository.save(execution);
        log.info("Execution created: {} ({})", execution.getId(), execution.getName());
        return ExecutionSummary.from(execution);
    }

    public void delete(String id) {
        logRepository.findByExecutionIdOrderByTimestamp(id).forEach(logRepository::delete);
        featureRepository.findByExecutionIdOrderByFeatureName(id).forEach(feature -> {
            scenarioRepository.findByFeatureIdOrderByScenarioName(feature.getId()).forEach(scenario -> {
                stepRepository.findByScenarioIdOrderById(scenario.getId()).forEach(stepRepository::delete);
            });
            scenarioRepository.findByFeatureIdOrderByScenarioName(feature.getId()).forEach(scenarioRepository::delete);
        });
        scenarioRepository.findByExecutionIdOrderByScenarioName(id).forEach(scenario -> {
            stepRepository.findByScenarioIdOrderById(scenario.getId()).forEach(stepRepository::delete);
        });
        scenarioRepository.findByExecutionIdOrderByScenarioName(id).forEach(scenarioRepository::delete);
        featureRepository.findByExecutionIdOrderByFeatureName(id).forEach(featureRepository::delete);
        reportRepository.findByExecutionId(id).forEach(reportRepository::delete);
        executionRepository.deleteById(id);
        log.info("Deleted execution: {}", id);
    }

    public ExecutionFeature addFeature(FeatureRequest request) {
        ExecutionFeature feature = new ExecutionFeature();
        feature.setExecutionId(request.getExecutionId());
        feature.setFeatureName(request.getFeatureName());
        feature.setUri(request.getUri());
        feature.setStatus(request.getStatus() != null ? request.getStatus() : "RUNNING");
        feature.setDurationMs(request.getDurationMs() != null ? request.getDurationMs() : 0L);
        return featureRepository.save(feature);
    }

    public ExecutionResponse.ScenarioInfo addScenario(ScenarioRequest request) {
        ExecutionScenario scenario = new ExecutionScenario();
        scenario.setExecutionId(request.getExecutionId());
        scenario.setFeatureId(request.getFeatureId());
        scenario.setScenarioName(request.getScenarioName());
        scenario.setTags(request.getTags());
        scenario.setStatus(request.getStatus() != null ? request.getStatus() : "UNKNOWN");
        scenario.setDurationMs(request.getDurationMs() != null ? request.getDurationMs() : 0L);
        scenario.setFailureReason(request.getFailureReason());
        scenario.setDeviceName(request.getDeviceName());
        ExecutionScenario saved = scenarioRepository.save(scenario);

        executionRepository.findById(request.getExecutionId()).ifPresent(exec -> {
            exec.setTotalCount(exec.getTotalCount() + 1);
            String s = saved.getStatus();
            if ("PASSED".equals(s)) exec.setPassCount(exec.getPassCount() + 1);
            else if ("FAILED".equals(s)) {
                exec.setFailCount(exec.getFailCount() + 1);
                exec.setStatus("FAILED");
            } else if ("SKIPPED".equals(s)) exec.setSkipCount(exec.getSkipCount() + 1);
            executionRepository.save(exec);
        });

        if (request.getFeatureId() != null) {
            ExecutionScenario fs = saved;
            featureRepository.findById(request.getFeatureId()).ifPresent(feature -> {
                String s = fs.getStatus();
                feature.setPassCount(feature.getPassCount() + ("PASSED".equals(s) ? 1 : 0));
                feature.setFailCount(feature.getFailCount() + ("FAILED".equals(s) ? 1 : 0));
                feature.setSkipCount(feature.getSkipCount() + ("SKIPPED".equals(s) ? 1 : 0));
                Long dur = fs.getDurationMs();
                if (dur != null && dur > 0) {
                    feature.setDurationMs(feature.getDurationMs() != null ? feature.getDurationMs() + dur : dur);
                }
                feature.setStatus(feature.getFailCount() > 0 ? "FAILED" : feature.getPassCount() > 0 ? "PASSED" : "RUNNING");
                featureRepository.save(feature);
            });
        }

        ExecutionResponse.ScenarioInfo info = new ExecutionResponse.ScenarioInfo();
        info.setId(saved.getId());
        info.setFeatureId(saved.getFeatureId());
        info.setScenarioName(saved.getScenarioName());
        info.setTags(saved.getTags());
        info.setStatus(saved.getStatus());
        info.setDurationMs(saved.getDurationMs());
        info.setFailureReason(saved.getFailureReason());
        info.setDeviceName(saved.getDeviceName());
        return info;
    }

    public ExecutionStep addStep(StepRequest request) {
        ExecutionStep step = new ExecutionStep();
        step.setScenarioId(request.getScenarioId());
        step.setKeyword(request.getKeyword() != null ? request.getKeyword() : "");
        step.setStepName(request.getStepName());
        step.setStatus(request.getStatus() != null ? request.getStatus() : "PASSED");
        step.setDurationMs(request.getDurationMs() != null ? request.getDurationMs() : 0L);
        step.setLogText(request.getLogText());
        return stepRepository.save(step);
    }

    public ExecutionLog addLog(LogRequest request) {
        ExecutionLog logEntry = new ExecutionLog();
        logEntry.setExecutionId(request.getExecutionId());
        logEntry.setLevel(request.getLevel() != null ? request.getLevel() : "INFO");
        logEntry.setMessage(request.getMessage());
        logEntry.setTimestamp(Instant.now().toString());
        return logRepository.save(logEntry);
    }

    public void updateStatus(String id, String status) {
        executionRepository.findById(id).ifPresent(exec -> {
            exec.setStatus(status);
            if ("PASSED".equals(status) || "FAILED".equals(status) || "ABORTED".equals(status)) {
                exec.setEndTime(Instant.now().toString());
                if (exec.getStartTime() != null) {
                    long start = Instant.parse(exec.getStartTime()).toEpochMilli();
                    long end = Instant.now().toEpochMilli();
                    exec.setDurationMs(end - start);
                }
            }
            executionRepository.save(exec);
        });
    }

    public String determineFinalStatus(String id) {
        long failed = scenarioRepository.countByExecutionIdAndStatus(id, "FAILED");
        return failed > 0 ? "FAILED" : "PASSED";
    }

    private ExecutionResponse getExecution(String id) {
        Execution execution = executionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Execution not found: " + id));
        ExecutionResponse response = ExecutionResponse.from(execution);

        List<ExecutionFeature> features = featureRepository.findByExecutionIdOrderByFeatureName(id);
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

        List<ExecutionScenario> scenarios = scenarioRepository.findByExecutionIdOrderByScenarioName(id);
        List<ExecutionResponse.ScenarioInfo> infos = scenarios.stream().map(s -> {
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
        response.setScenarios(infos);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> listExecutions(int page, int size) {
        Page<Execution> execs = executionRepository.findAllOrderByCreatedAtDesc(PageRequest.of(page, size));
        return execs.getContent().stream()
                .map(ExecutionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countExecutions() {
        return executionRepository.count();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummary() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExecutions", executionRepository.count());
        stats.put("passed", executionRepository.countByStatus("PASSED"));
        stats.put("failed", executionRepository.countByStatus("FAILED"));
        stats.put("aborted", executionRepository.countByStatus("ABORTED"));
        stats.put("running", executionRepository.countByStatus("RUNNING"));
        long total = executionRepository.count();
        stats.put("passRate", total > 0 ? (double) executionRepository.countByStatus("PASSED") / total * 100 : 0);
        stats.put("avgDurationMs", executionRepository.avgDurationMs());

        List<ExecutionScenario> scenarios = scenarioRepository.findAll();
        stats.put("totalScenarios", scenarios.size());
        stats.put("totalPassed", scenarios.stream().filter(s -> "PASSED".equals(s.getStatus())).count());
        stats.put("totalFailed", scenarios.stream().filter(s -> "FAILED".equals(s.getStatus())).count());
        stats.put("totalSkipped", scenarios.stream().filter(s -> "SKIPPED".equals(s.getStatus())).count());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> getRunning() {
        return executionRepository.findRunningOrPending().stream()
                .map(ExecutionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLogs(String executionId) {
        return logRepository.findByExecutionIdOrderByTimestamp(executionId).stream()
                .map(l -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("level", l.getLevel());
                    m.put("message", l.getMessage());
                    m.put("timestamp", l.getTimestamp());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStepsByScenario(String scenarioId) {
        return stepRepository.findByScenarioIdOrderById(scenarioId).stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getId());
                    m.put("keyword", s.getKeyword());
                    m.put("stepName", s.getStepName());
                    m.put("status", s.getStatus());
                    m.put("durationMs", s.getDurationMs());
                    m.put("logText", s.getLogText() != null ? s.getLogText() : "");
                    return m;
                })
                .collect(Collectors.toList());
    }
}
