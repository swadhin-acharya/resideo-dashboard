package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.entity.Report;
import com.openqa.dashboard.service.ReportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<Report> getAll() {
        return reportService.findAll();
    }

    @GetMapping("/{id}")
    public Report getById(@PathVariable String id) {
        return reportService.findById(id);
    }

    @GetMapping("/by-execution/{executionId}")
    public List<Report> getByExecution(@PathVariable String executionId) {
        return reportService.findByExecutionId(executionId);
    }

    @PostMapping
    public Report create(@RequestBody Map<String, String> body) {
        return reportService.create(
                body.get("executionId"),
                body.get("name"),
                body.get("filePath"),
                body.get("fileSize") != null ? Long.parseLong(body.get("fileSize")) : 0L
        );
    }

    @GetMapping(value = "/{id}/content", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getContent(@PathVariable String id) {
        String content = reportService.getContent(id);
        return ResponseEntity.ok(content);
    }
}
