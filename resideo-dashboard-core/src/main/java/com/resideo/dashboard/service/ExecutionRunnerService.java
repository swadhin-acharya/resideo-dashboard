package com.resideo.dashboard.service;

import com.resideo.dashboard.model.dto.ExecutionRequest;
import com.resideo.dashboard.model.dto.ExecutionResponse;
import com.resideo.dashboard.model.entity.Execution;
import com.resideo.dashboard.model.enums.ExecutionStatus;
import com.resideo.dashboard.repository.ExecutionRepository;
import com.resideo.dashboard.websocket.LiveExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ExecutionRunnerService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionRunnerService.class);

    private final ExecutionService executionService;
    private final ExecutionRepository executionRepository;
    private final WorkspaceWatcherService workspaceWatcher;
    private final CucumberReportParser reportParser;
    private final LiveExecutionHandler liveHandler;
    private final Map<UUID, Process> runningProcesses = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ExecutionRunnerService(ExecutionService executionService,
                                  ExecutionRepository executionRepository,
                                  WorkspaceWatcherService workspaceWatcher,
                                  CucumberReportParser reportParser,
                                  LiveExecutionHandler liveHandler) {
        this.executionService = executionService;
        this.executionRepository = executionRepository;
        this.workspaceWatcher = workspaceWatcher;
        this.reportParser = reportParser;
        this.liveHandler = liveHandler;
    }

    public ExecutionResponse createAndRun(ExecutionRequest request) {
        return createAndRun(request, null, null);
    }

    public ExecutionResponse createAndRun(ExecutionRequest request, UUID projectId, UUID userId) {
        ExecutionResponse created = executionService.create(request, projectId, userId);
        UUID execId = created.getId();
        liveHandler.broadcast(execId, "EXECUTION_CREATED", Map.of(
            "id", execId.toString(),
            "name", created.getName(),
            "status", "PENDING"
        ));
        return created;
    }

    public List<Map<String, String>> listFeatureFiles(String workspacePath) {
        List<Map<String, String>> features = new ArrayList<>();
        try {
            Path base = Paths.get(workspacePath != null ? workspacePath : ".");
            if (!Files.exists(base)) return features;

            Files.walk(base)
                .filter(p -> p.toString().endsWith(".feature"))
                .forEach(p -> {
                    Map<String, String> f = new HashMap<>();
                    f.put("path", base.relativize(p).toString());
                    f.put("name", p.getFileName().toString().replace(".feature", ""));
                    f.put("fullPath", p.toAbsolutePath().toString());
                    features.add(f);
                });
        } catch (IOException e) {
            log.warn("Failed to list feature files: {}", e.getMessage());
        }
        return features;
    }

    public void cancelExecution(UUID execId) {
        Process process = runningProcesses.remove(execId);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            Execution exec = executionRepository.findById(execId).orElse(null);
            if (exec != null) {
                exec.setStatus(ExecutionStatus.ABORTED);
                exec.setEndTime(Instant.now());
                executionRepository.save(exec);
            }
            liveHandler.broadcast(execId, "EXECUTION_ABORTED", Map.of(
                "id", execId.toString(),
                "status", "ABORTED"
            ));
        }
    }
}
