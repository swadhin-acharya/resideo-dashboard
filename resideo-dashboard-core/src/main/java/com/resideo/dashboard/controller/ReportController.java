package com.resideo.dashboard.controller;

import com.resideo.dashboard.config.ResideoProperties;
import com.resideo.dashboard.model.entity.Execution;
import com.resideo.dashboard.repository.ExecutionRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/executions/{id}/report")
public class ReportController {

    private final ExecutionRepository executionRepository;
    private final ResideoProperties properties;

    public ReportController(ExecutionRepository executionRepository, ResideoProperties properties) {
        this.executionRepository = executionRepository;
        this.properties = properties;
    }

    @GetMapping
    public ResponseEntity<Resource> getReport(@PathVariable UUID id) {
        Execution execution = executionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Execution not found: " + id));
        String reportPath = execution.getReportPath();
        if (reportPath == null) {
            reportPath = properties.getWorkspace() + File.separator + properties.getReportPath();
        }
        File reportFile = new File(reportPath);
        if (!reportFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(reportFile);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.html\"")
                .body(resource);
    }

    @GetMapping("/screenshots/{name}")
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
