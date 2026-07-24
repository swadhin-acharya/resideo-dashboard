package com.openqa.dashboard.service;

import com.openqa.dashboard.config.OpenQAProperties;
import com.openqa.dashboard.model.dto.ExecutionRequest;
import com.openqa.dashboard.model.entity.Report;
import com.openqa.dashboard.repository.ReportRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class WorkspaceWatcherService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceWatcherService.class);

    private final OpenQAProperties properties;
    private final ExecutionService executionService;
    private final CucumberReportParser reportParser;
    private final ReportRepository reportRepository;
    private ScheduledExecutorService scheduler;
    private Set<String> seen = ConcurrentHashMap.newKeySet();

    public WorkspaceWatcherService(OpenQAProperties properties, ExecutionService executionService,
                                   CucumberReportParser reportParser, ReportRepository reportRepository) {
        this.properties = properties;
        this.executionService = executionService;
        this.reportParser = reportParser;
        this.reportRepository = reportRepository;
    }

    @PostConstruct
    public void start() {
        String dir = properties.getWorkspaceDir();
        if (dir == null || dir.isBlank()) {
            log.info("Workspace dir not configured - watcher disabled");
            return;
        }
        File workspace = new File(dir);
        if (!workspace.isDirectory()) {
            log.info("Workspace dir {} does not exist - watcher disabled", dir);
            return;
        }

        log.info("Watching workspace: {}", dir);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::scan, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }

    private void scan() {
        try {
            String dir = properties.getWorkspaceDir();
            Set<File> jsonFiles = Files.walk(Paths.get(dir))
                    .filter(p -> p.toString().endsWith("cucumber.json"))
                    .map(Path::toFile)
                    .collect(Collectors.toSet());

            for (File jsonFile : jsonFiles) {
                String key = jsonFile.getAbsolutePath() + ":" + jsonFile.lastModified();
                if (seen.contains(key)) continue;
                seen.add(key);

                try {
                    processReport(jsonFile);
                } catch (Exception e) {
                    log.warn("Failed to process report {}: {}", jsonFile, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("Workspace scan failed: {}", e.getMessage());
        }
    }

    private void processReport(File jsonFile) throws Exception {
        ExecutionRequest execReq = new ExecutionRequest();
        execReq.setExecutionName(jsonFile.getParentFile().getName());
        execReq.setType("AUTOMATED");
        execReq.setSource("workspace-watcher");
        var execution = executionService.create(execReq);
        String execId = execution.getId();

        reportParser.parse(execId, jsonFile);

        String status = executionService.determineFinalStatus(execId);
        executionService.updateStatus(execId, status);

        Report report = new Report();
        report.setExecutionId(execId);
        report.setName(jsonFile.getName());
        report.setFilePath(jsonFile.getAbsolutePath());
        report.setCreatedAt(Instant.now().toString());
        reportRepository.save(report);

        log.info("Auto-registered report: {} -> execution {}", jsonFile, execId);
    }
}
