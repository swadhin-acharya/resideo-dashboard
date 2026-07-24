package com.openqa.dashboard.service;

import com.openqa.dashboard.model.entity.Report;
import com.openqa.dashboard.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<Report> findAll() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Report findById(String id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Report not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Report> findByExecutionId(String executionId) {
        return reportRepository.findByExecutionId(executionId);
    }

    public Report create(String executionId, String name, String filePath, Long fileSize) {
        Report report = new Report();
        report.setExecutionId(executionId);
        report.setName(name);
        report.setFilePath(filePath);
        report.setFileSize(fileSize != null ? fileSize : 0L);
        report.setCreatedAt(Instant.now().toString());
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public String getContent(String id) {
        Report report = findById(id);
        if (report.getFilePath() == null || report.getFilePath().isBlank()) {
            throw new NoSuchElementException("Report has no file path");
        }
        try {
            Path path = Paths.get(report.getFilePath());
            if (!Files.exists(path)) {
                throw new NoSuchElementException("Report file not found: " + report.getFilePath());
            }
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read report file: " + report.getFilePath(), e);
        }
    }
}
