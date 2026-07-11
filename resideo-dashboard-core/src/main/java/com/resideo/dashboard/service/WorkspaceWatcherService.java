package com.resideo.dashboard.service;

import com.resideo.dashboard.config.ResideoProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
public class WorkspaceWatcherService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceWatcherService.class);

    private final ResideoProperties properties;
    private final CucumberReportParser cucumberReportParser;
    private final Map<String, UUID> pendingFiles = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running = true;

    public WorkspaceWatcherService(ResideoProperties properties, CucumberReportParser cucumberReportParser) {
        this.properties = properties;
        this.cucumberReportParser = cucumberReportParser;
    }

    @PostConstruct
    public void start() {
        String workspace = properties.getWorkspace();
        File workspaceDir = new File(workspace);
        if (!workspaceDir.exists() || !workspaceDir.isDirectory()) {
            log.warn("Workspace does not exist, watcher disabled: {}", workspace);
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path dir = workspaceDir.toPath();
            dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::pollPending, 5, properties.getPollIntervalMs(), TimeUnit.MILLISECONDS);

            watchThread = new Thread(this::watchLoop, "workspace-watcher");
            watchThread.setDaemon(true);
            watchThread.start();

            log.info("Workspace watcher started for: {}", workspace);
        } catch (IOException e) {
            log.error("Failed to start workspace watcher", e);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (watchThread != null) watchThread.interrupt();
        if (scheduler != null) scheduler.shutdown();
        if (watchService != null) {
            try { watchService.close(); } catch (IOException ignored) {}
        }
    }

    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.poll(2, TimeUnit.SECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    String fileName = changed.toString();
                    if (fileName.equals("cucumber.json")) {
                        String fullPath = properties.getWorkspace() + File.separator + "target" + File.separator + fileName;
                        File jsonFile = new File(fullPath);
                        if (jsonFile.exists()) {
                            String absPath = jsonFile.getAbsolutePath();
                            pendingFiles.put(absPath, null);
                            log.debug("Detected cucumber.json change: {}", absPath);
                        }
                    }
                }
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void pollPending() {
        for (Map.Entry<String, UUID> entry : pendingFiles.entrySet()) {
            if (entry.getValue() != null) {
                File file = new File(entry.getKey());
                if (file.exists()) {
                    try {
                        cucumberReportParser.parse(entry.getValue(), file);
                        pendingFiles.remove(entry.getKey());
                    } catch (Exception e) {
                        log.error("Failed to parse cucumber.json: {}", entry.getKey(), e);
                    }
                }
            }
        }
    }

    public void registerForExecution(String cucumberJsonPath, UUID executionId) {
        String key = new File(cucumberJsonPath).getAbsolutePath();
        pendingFiles.put(key, executionId);
        log.info("Registered cucumber.json for execution {}: {}", executionId, key);
    }
}
