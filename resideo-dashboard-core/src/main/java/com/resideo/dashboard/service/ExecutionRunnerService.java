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

import java.io.*;
import java.nio.file.*;
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
            "status", "RUNNING"
        ));

        String mavenCmd = created.getMavenCommand();
        executor.submit(() -> runMaven(execId, mavenCmd, request));
        return created;
    }

    private void runMaven(UUID execId, String mavenCmd, ExecutionRequest request) {
        try {
            liveHandler.broadcast(execId, "EXECUTION_LOG", Map.of("message", "Starting: " + mavenCmd));

            Execution exec = executionRepository.findById(execId).orElse(null);
            if (exec == null) return;

            Path execDir = Paths.get(System.getProperty("user.dir"), "executions", execId.toString());
            Files.createDirectories(execDir);
            exec.setWorkspacePath(execDir.toString());

            Path logFile = execDir.resolve("execution.log");
            Path cucumberJson = execDir.resolve("target/cucumber.json");

            ProcessBuilder pb = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("cmd.exe", "/c", mavenCmd);
            } else {
                pb.command("sh", "-c", mavenCmd);
            }
            pb.directory(execDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            runningProcesses.put(execId, process);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedWriter logWriter = Files.newBufferedWriter(logFile)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    logWriter.write(line);
                    logWriter.newLine();
                    logWriter.flush();
                    liveHandler.broadcast(execId, "EXECUTION_LOG", Map.of("message", line));
                }
            }

            int exitCode = process.waitFor();
            runningProcesses.remove(execId);

            liveHandler.broadcast(execId, "EXECUTION_LOG", Map.of("message", "Maven exited with code: " + exitCode));

            if (Files.exists(cucumberJson)) {
                liveHandler.broadcast(execId, "EXECUTION_LOG", Map.of("message", "Parsing cucumber.json results..."));
                reportParser.parse(execId, cucumberJson.toFile());

                Path reportsDir = execDir.resolve("target");
                if (Files.exists(reportsDir)) {
                    try (var paths = Files.walk(reportsDir)) {
                        paths.filter(p -> p.toString().endsWith(".html"))
                             .findFirst()
                             .ifPresent(p -> {
                                 exec.setReportPath(p.toAbsolutePath().toString());
                                 executionRepository.save(exec);
                             });
                    }
                }

                liveHandler.broadcast(execId, "EXECUTION_COMPLETED", Map.of(
                    "id", execId.toString(),
                    "status", exec.getStatus() != null ? exec.getStatus().name() : "PASSED"
                ));
            } else {
                exec.setStatus(ExecutionStatus.FAILED);
                exec.setEndTime(Instant.now());
                if (exec.getStartTime() != null) {
                    exec.setDurationMs(Duration.between(exec.getStartTime(), exec.getEndTime()).toMillis());
                }
                executionRepository.save(exec);
                liveHandler.broadcast(execId, "EXECUTION_COMPLETED", Map.of(
                    "id", execId.toString(),
                    "status", "FAILED",
                    "error", "cucumber.json not found"
                ));
            }

        } catch (Exception e) {
            log.error("Execution failed: " + execId, e);
            liveHandler.broadcast(execId, "EXECUTION_ERROR", Map.of("error", e.getMessage()));

            Execution exec = executionRepository.findById(execId).orElse(null);
            if (exec != null) {
                exec.setStatus(ExecutionStatus.FAILED);
                exec.setEndTime(Instant.now());
                exec.setDurationMs(Duration.between(exec.getStartTime(), Instant.now()).toMillis());
                executionRepository.save(exec);
            }
        }
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
