package com.openqa.dashboard.controller;

import com.openqa.dashboard.config.OpenQAProperties;
import com.openqa.dashboard.model.dto.ExecutionResponse;
import com.openqa.dashboard.model.entity.Execution;
import com.openqa.dashboard.repository.ExecutionRepository;
import com.openqa.dashboard.service.ExecutionService;
import com.openqa.dashboard.service.NotificationService;
import com.openqa.dashboard.service.ReportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReportController {

    private final ExecutionRepository executionRepository;
    private final ExecutionService executionService;
    private final ReportService reportService;
    private final NotificationService notificationService;
    private final OpenQAProperties properties;

    public ReportController(ExecutionRepository executionRepository,
                            ExecutionService executionService,
                            ReportService reportService,
                            NotificationService notificationService,
                            OpenQAProperties properties) {
        this.executionRepository = executionRepository;
        this.executionService = executionService;
        this.reportService = reportService;
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ExecutionResponse>> listReports(
            @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        return ResponseEntity.ok(executionService.listWithReports(projectId));
    }

    @GetMapping("/executions/{id}/report")
    public ResponseEntity<Resource> getReport(@PathVariable UUID id) {
        Execution execution = executionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Execution not found: " + id));
        String reportPath = execution.getReportPath();
        if (reportPath == null) {
            reportPath = properties.getWorkspace() + File.separator + properties.getReportPath();
        }
        File reportFile = new File(reportPath);
        if (!reportFile.exists()) {
            String html = reportService.generateHtmlReport(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.html\"")
                    .body(new org.springframework.core.io.ByteArrayResource(html.getBytes(StandardCharsets.UTF_8)));
        }
        Resource resource = new FileSystemResource(reportFile);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.html\"")
                .body(resource);
    }

    @GetMapping("/executions/{id}/report/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable UUID id) {
        String html = reportService.generateHtmlReport(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report_" + id + ".html\"")
                .body(new org.springframework.core.io.ByteArrayResource(html.getBytes(StandardCharsets.UTF_8)));
    }

    @GetMapping("/executions/{id}/report/email")
    public ResponseEntity<Map<String, Object>> getReportEmailPreview(@PathVariable UUID id) {
        String html = reportService.generateHtmlReport(id);
        Execution exec = executionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Execution not found: " + id));
        String subject = "Test Report: " + exec.getName();
        return ResponseEntity.ok(Map.of(
                "subject", subject,
                "html", html,
                "executionName", exec.getName()
        ));
    }

    @PostMapping("/executions/{id}/report/email")
    public ResponseEntity<Map<String, String>> emailReport(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String to = body.get("to");
        String from = body.getOrDefault("from", "noreply@openqa.in");
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Recipient email is required"));
        }
        String html = reportService.generateHtmlReport(id);
        Execution exec = executionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Execution not found: " + id));
        String subject = "Test Report: " + exec.getName() + " [" + exec.getStatus() + "]";
        notificationService.sendEmail(from, to, subject, html);
        return ResponseEntity.ok(Map.of("message", "Report emailed to " + to));
    }

    @GetMapping("/executions/{id}/logs/download")
    public ResponseEntity<Resource> downloadLogs(@PathVariable UUID id) {
        String logText = reportService.generateLogText(id);
        byte[] bytes = logText.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"execution_" + id + ".log\"")
                .body(new org.springframework.core.io.ByteArrayResource(bytes));
    }

    @GetMapping("/executions/{id}/screenshots/{name}")
    public ResponseEntity<Resource> getScreenshot(@PathVariable UUID id, @PathVariable String name) {
        Execution execution = executionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Execution not found: " + id));
        String workspace = execution.getWorkspacePath() != null ? execution.getWorkspacePath() : properties.getWorkspace();
        File screenshot = new File(workspace + "/target/open-reporter/screenshots/" + name);
        if (!screenshot.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(screenshot));
    }
}
