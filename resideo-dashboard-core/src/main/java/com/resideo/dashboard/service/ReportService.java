package com.resideo.dashboard.service;

import com.resideo.dashboard.model.entity.*;
import com.resideo.dashboard.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ExecutionRepository executionRepository;
    private final ExecutionFeatureRepository featureRepository;
    private final ExecutionScenarioRepository scenarioRepository;
    private final ExecutionStepRepository stepRepository;
    private final ExecutionLogRepository logRepository;

    public ReportService(ExecutionRepository executionRepository,
                         ExecutionFeatureRepository featureRepository,
                         ExecutionScenarioRepository scenarioRepository,
                         ExecutionStepRepository stepRepository,
                         ExecutionLogRepository logRepository) {
        this.executionRepository = executionRepository;
        this.featureRepository = featureRepository;
        this.scenarioRepository = scenarioRepository;
        this.stepRepository = stepRepository;
        this.logRepository = logRepository;
    }

    public String generateHtmlReport(UUID executionId) {
        Execution exec = executionRepository.findById(executionId)
                .orElseThrow(() -> new NoSuchElementException("Execution not found: " + executionId));

        List<ExecutionFeature> features = featureRepository.findByExecutionId(executionId);
        List<ExecutionScenario> scenarios = scenarioRepository.findByExecutionId(executionId);
        Map<UUID, List<ExecutionStep>> stepsByScenario = new HashMap<>();
        for (ExecutionScenario s : scenarios) {
            stepsByScenario.put(s.getId(), stepRepository.findByScenarioIdOrderById(s.getId()));
        }

        return buildHtml(exec, features, scenarios, stepsByScenario);
    }

    public String generateLogText(UUID executionId) {
        List<ExecutionLog> logs = logRepository.findByExecutionIdOrderByTimestampAsc(executionId);
        StringBuilder sb = new StringBuilder();
        for (ExecutionLog log : logs) {
            sb.append("[").append(log.getTimestamp()).append("] [").append(log.getLevel()).append("] ")
              .append(log.getMessage()).append("\n");
        }
        return sb.toString();
    }

    private String buildHtml(Execution exec, List<ExecutionFeature> features,
                             List<ExecutionScenario> scenarios,
                             Map<UUID, List<ExecutionStep>> stepsByScenario) {
        long duration = exec.getDurationMs() != null ? exec.getDurationMs() : 0;
        String durationStr = formatDuration(duration);
        int total = exec.getTotalCount() != null ? exec.getTotalCount() : 0;
        int passed = exec.getPassCount() != null ? exec.getPassCount() : 0;
        int failed = exec.getFailCount() != null ? exec.getFailCount() : 0;
        int skipped = exec.getSkipCount() != null ? exec.getSkipCount() : 0;
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;
        String status = exec.getStatus() != null ? exec.getStatus().name() : "UNKNOWN";

        Map<String, List<ExecutionScenario>> scenariosByFeature = scenarios.stream()
                .collect(Collectors.groupingBy(s -> {
                    UUID fid = s.getFeatureId();
                    for (ExecutionFeature f : features) {
                        if (f.getId().equals(fid)) return f.getFeatureName();
                    }
                    return "Unknown";
                }));

        String featuresHtml = "";
        for (ExecutionFeature f : features) {
            int fTotal = f.getPassCount() + f.getFailCount() + f.getSkipCount();
            featuresHtml += String.format("""
                <tr>
                    <td style="padding:8px;border-bottom:1px solid #e0e0e0;">%s</td>
                    <td style="padding:8px;border-bottom:1px solid #e0e0e0;text-align:center;"><span class="badge badge-%s">%s</span></td>
                    <td style="padding:8px;border-bottom:1px solid #e0e0e0;text-align:center;color:#2e7d32;">%d</td>
                    <td style="padding:8px;border-bottom:1px solid #e0e0e0;text-align:center;color:#d32f2f;">%d</td>
                    <td style="padding:8px;border-bottom:1px solid #e0e0e0;text-align:center;">%s</td>
                </tr>""",
                escapeHtml(f.getFeatureName()),
                f.getStatus() != null ? f.getStatus().toLowerCase() : "unknown",
                f.getStatus() != null ? f.getStatus() : "UNKNOWN",
                f.getPassCount(),
                f.getFailCount(),
                formatDuration(f.getDurationMs()));
        }

        String scenariosHtml = "";
        for (ExecutionScenario s : scenarios) {
            String featureName = "Unknown";
            for (ExecutionFeature f : features) {
                if (f.getId().equals(s.getFeatureId())) {
                    featureName = f.getFeatureName();
                    break;
                }
            }
            String tags = s.getTags() != null && !s.getTags().isEmpty() ? s.getTags() : "";
            String tagsHtml = "";
            if (!tags.isEmpty()) {
                tagsHtml = Arrays.stream(tags.split(","))
                        .map(t -> String.format("<span class=\"tag\">%s</span>", escapeHtml(t.trim())))
                        .collect(Collectors.joining(" "));
            }

            List<ExecutionStep> steps = stepsByScenario.getOrDefault(s.getId(), Collections.emptyList());
            String stepsHtml = "";
            if (!steps.isEmpty()) {
                stepsHtml = "<table style=\"width:100%;margin-top:8px;border-collapse:collapse;\">" +
                            "<tr style=\"background:#f5f5f5;\"><th style=\"padding:4px 8px;text-align:left;font-size:12px;\">Step</th>" +
                            "<th style=\"padding:4px 8px;width:80px;font-size:12px;\">Status</th>" +
                            "<th style=\"padding:4px 8px;width:80px;font-size:12px;\">Duration</th></tr>";
                for (ExecutionStep step : steps) {
                    String stepStatus = step.getStatus() != null ? step.getStatus().toLowerCase() : "unknown";
                    stepsHtml += String.format("""
                        <tr>
                            <td style="padding:4px 8px;border-bottom:1px solid #f0f0f0;font-size:12px;">%s</td>
                            <td style="padding:4px 8px;border-bottom:1px solid #f0f0f0;text-align:center;"><span class="badge badge-%s">%s</span></td>
                            <td style="padding:4px 8px;border-bottom:1px solid #f0f0f0;text-align:center;font-size:12px;color:#666;">%s</td>
                        </tr>""",
                        escapeHtml(step.getStepName()),
                        stepStatus,
                        step.getStatus() != null ? step.getStatus() : "UNKNOWN",
                        formatDuration(step.getDurationMs()));
                    if (step.getLogText() != null && !step.getLogText().isEmpty()) {
                        stepsHtml += String.format("""
                            <tr><td colspan="3" style="padding:4px 8px;background:#fff3e0;font-size:11px;font-family:monospace;color:#c62828;">%s</td></tr>""",
                                escapeHtml(step.getLogText()));
                    }
                }
                stepsHtml += "</table>";
            }

            scenariosHtml += String.format("""
                <tr>
                    <td style="padding:10px;border-bottom:1px solid #e0e0e0;">
                        <div style="font-weight:500;">%s</div>
                        <div style="font-size:11px;color:#888;margin-top:4px;">%s</div>
                        <div style="margin-top:4px;">%s</div>
                    </td>
                    <td style="padding:10px;border-bottom:1px solid #e0e0e0;text-align:center;"><span class="badge badge-%s">%s</span></td>
                    <td style="padding:10px;border-bottom:1px solid #e0e0e0;text-align:center;font-size:13px;color:#666;">%s</td>
                </tr>""",
                escapeHtml(s.getScenarioName()),
                escapeHtml(featureName),
                tagsHtml,
                s.getStatus() != null ? s.getStatus().toLowerCase() : "unknown",
                s.getStatus() != null ? s.getStatus() : "UNKNOWN",
                formatDuration(s.getDurationMs()));
        }

        String statusColor = switch (status) {
            case "PASSED" -> "#2e7d32";
            case "FAILED" -> "#d32f2f";
            case "SKIPPED" -> "#ed6c02";
            default -> "#757575";
        };

        String createdDate = exec.getCreatedAt() != null ?
                exec.getCreatedAt().toString().substring(0, 19).replace("T", " ") : "-";

        return String.format("""
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Test Report - %s</title>
<style>
* { margin:0; padding:0; box-sizing:border-box; }
body { font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; background:#f5f7fa; color:#333; }
.header { background:linear-gradient(135deg,#1a237e,#283593); color:#fff; padding:32px 40px; }
.header h1 { font-size:24px; margin-bottom:8px; }
.header .meta { display:flex; gap:24px; flex-wrap:wrap; font-size:14px; opacity:0.9; }
.summary { display:flex; gap:16px; padding:24px 40px; background:#fff; border-bottom:1px solid #e0e0e0; flex-wrap:wrap; }
.stat-card { flex:1; min-width:120px; text-align:center; padding:16px; border-radius:8px; background:#f8f9fa; }
.stat-card .value { font-size:32px; font-weight:700; }
.stat-card .label { font-size:12px; color:#666; margin-top:4px; text-transform:uppercase; letter-spacing:0.5px; }
.content { padding:24px 40px; }
.section { background:#fff; border-radius:8px; margin-bottom:24px; box-shadow:0 1px 3px rgba(0,0,0,0.08); overflow:hidden; }
.section h2 { font-size:16px; padding:16px 20px; background:#f8f9fa; border-bottom:1px solid #e0e0e0; }
table { width:100%%; border-collapse:collapse; }
th { text-align:left; padding:10px 12px; font-size:12px; color:#666; text-transform:uppercase; letter-spacing:0.5px; background:#fafafa; border-bottom:2px solid #e0e0e0; font-weight:600; }
td { padding:10px 12px; border-bottom:1px solid #f0f0f0; font-size:14px; }
.badge { display:inline-block; padding:2px 10px; border-radius:12px; font-size:12px; font-weight:600; }
.badge-passed { background:#e8f5e9; color:#2e7d32; }
.badge-failed { background:#fbe9e7; color:#d32f2f; }
.badge-skipped { background:#fff3e0; color:#ed6c02; }
.badge-running { background:#e3f2fd; color:#1565c0; }
.badge-unknown { background:#f5f5f5; color:#757575; }
.tag { display:inline-block; padding:1px 6px; border-radius:3px; font-size:10px; background:#e3f2fd; color:#1565c0; margin:1px; }
.progress { display:flex; height:8px; border-radius:4px; overflow:hidden; margin-top:8px; }
.progress-pass { background:#2e7d32; }
.progress-fail { background:#d32f2f; }
.progress-skip { background:#ed6c02; }
.footer { text-align:center; padding:24px; color:#999; font-size:12px; }
@media (max-width: 768px) {
    .header { padding:20px; }
    .summary { padding:16px 20px; }
    .content { padding:16px 20px; }
    .stat-card { min-width:80px; }
    .stat-card .value { font-size:24px; }
}
</style>
</head>
<body>
<div class="header">
    <h1>Execution Report: %s</h1>
    <div class="meta">
        <span>Status: <strong>%s</strong></span>
        <span>Duration: <strong>%s</strong></span>
        <span>Date: <strong>%s</strong></span>
        <span>Platform: <strong>%s</strong></span>
        <span>Environment: <strong>%s</strong></span>
        <span>Triggered By: <strong>%s</strong></span>
    </div>
</div>

<div class="summary">
    <div class="stat-card">
        <div class="value" style="color:%s">%d</div>
        <div class="label">Total Scenarios</div>
    </div>
    <div class="stat-card">
        <div class="value" style="color:#2e7d32">%d</div>
        <div class="label">Passed</div>
    </div>
    <div class="stat-card">
        <div class="value" style="color:#d32f2f">%d</div>
        <div class="label">Failed</div>
    </div>
    <div class="stat-card">
        <div class="value" style="color:#ed6c02">%d</div>
        <div class="label">Skipped</div>
    </div>
    <div class="stat-card">
        <div class="value" style="color:#1565c0">%s</div>
        <div class="label">Pass Rate</div>
    </div>
</div>

<div class="progress" style="margin:0 40px 24px;">
    <div class="progress-pass" style="flex:%d"></div>
    <div class="progress-fail" style="flex:%d"></div>
    <div class="progress-skip" style="flex:%d"></div>
</div>

<div class="content">
    <div class="section">
        <h2>Features (%d)</h2>
        <table>
            <thead>
                <tr><th>Feature</th><th style="text-align:center;">Status</th><th style="text-align:center;">Passed</th><th style="text-align:center;">Failed</th><th style="text-align:center;">Duration</th></tr>
            </thead>
            <tbody>
                %s
            </tbody>
        </table>
    </div>

    <div class="section">
        <h2>Scenarios (%d)</h2>
        <table>
            <thead>
                <tr><th>Scenario</th><th style="text-align:center;width:100px;">Status</th><th style="text-align:center;width:100px;">Duration</th></tr>
            </thead>
            <tbody>
                %s
            </tbody>
        </table>
    </div>
</div>

<div class="footer">
    Generated by Resideo Dashboard &mdash; %s
</div>
</body>
</html>""",
                escapeHtml(exec.getName()),
                escapeHtml(exec.getName()),
                status,
                durationStr,
                createdDate,
                exec.getPlatform() != null ? escapeHtml(exec.getPlatform().name()) : "-",
                exec.getEnvironment() != null ? escapeHtml(exec.getEnvironment()) : "-",
                exec.getTriggeredBy() != null ? escapeHtml(exec.getTriggeredBy()) : "-",
                statusColor,
                total,
                passed,
                failed,
                skipped,
                String.format("%.1f%%", passRate),
                passed, failed, skipped,
                features.size(),
                featuresHtml,
                scenarios.size(),
                scenariosHtml,
                java.time.LocalDate.now().toString());
    }

    private String formatDuration(Long ms) {
        if (ms == null) return "-";
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
