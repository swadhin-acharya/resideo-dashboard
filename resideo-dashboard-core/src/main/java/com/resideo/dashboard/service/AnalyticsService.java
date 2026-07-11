package com.resideo.dashboard.service;

import com.resideo.dashboard.model.entity.Execution;
import com.resideo.dashboard.repository.ExecutionFeatureRepository;
import com.resideo.dashboard.repository.ExecutionRepository;
import com.resideo.dashboard.repository.ExecutionScenarioRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ExecutionRepository executionRepository;
    private final ExecutionFeatureRepository featureRepository;
    private final ExecutionScenarioRepository scenarioRepository;

    public AnalyticsService(ExecutionRepository executionRepository,
                            ExecutionFeatureRepository featureRepository,
                            ExecutionScenarioRepository scenarioRepository) {
        this.executionRepository = executionRepository;
        this.featureRepository = featureRepository;
        this.scenarioRepository = scenarioRepository;
    }

    public List<Map<String, Object>> getTrends(int days) {
        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);
        List<Execution> executions = executionRepository.findByDateRange(from, Instant.now(), null);

        Map<String, Map<String, Long>> daily = new LinkedHashMap<>();
        for (Execution e : executions) {
            String day = e.getCreatedAt().toString().substring(0, 10);
            daily.computeIfAbsent(day, k -> {
                Map<String, Long> m = new HashMap<>();
                m.put("passed", 0L);
                m.put("failed", 0L);
                m.put("skipped", 0L);
                m.put("total", 0L);
                return m;
            });
            Map<String, Long> d = daily.get(day);
            d.merge("total", 1L, Long::sum);
            switch (e.getStatus().name()) {
                case "PASSED" -> d.merge("passed", 1L, Long::sum);
                case "FAILED" -> d.merge("failed", 1L, Long::sum);
            }
        }

        return daily.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>(e.getValue());
            m.put("date", e.getKey());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getFlakyTests() {
        List<Object[]> results = scenarioRepository.findFlakyScenarios();
        return results.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("scenarioName", r[0]);
            m.put("totalRuns", r[1]);
            m.put("passed", r[2]);
            m.put("failed", r[3]);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMostFailedFeatures() {
        List<Object[]> results = featureRepository.findMostFailedFeatures(20);
        return results.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("featureName", r[0]);
            m.put("failures", r[1]);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMostFailedScenarios() {
        List<Object[]> results = scenarioRepository.findMostFailedScenarios();
        return results.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("scenarioName", r[0]);
            m.put("failures", r[1]);
            return m;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getDeviceStats() {
        List<Execution> executions = executionRepository.findAll();
        Map<String, Long> byPlatform = executions.stream()
                .filter(e -> e.getPlatform() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getPlatform().name(),
                    Collectors.counting()
                ));
        Map<String, Long> byStatus = executions.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus().name(),
                    Collectors.counting()
                ));
        Map<String, Object> result = new HashMap<>();
        result.put("byPlatform", byPlatform);
        result.put("byStatus", byStatus);
        return result;
    }

    public Map<String, Object> getFirmwareStats() {
        List<Execution> executions = executionRepository.findAll();
        return executions.stream()
                .filter(e -> e.getFirmwareVersion() != null)
                .collect(Collectors.groupingBy(
                    Execution::getFirmwareVersion,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Map<String, Object> s = new HashMap<>();
                            s.put("total", (long) list.size());
                            s.put("passed", list.stream().filter(e -> e.getStatus().name().equals("PASSED")).count());
                            s.put("failed", list.stream().filter(e -> e.getStatus().name().equals("FAILED")).count());
                            return s;
                        }
                    )
                ));
    }

    public Map<String, Object> compareExecutions(UUID leftId, UUID rightId) {
        Execution left = executionRepository.findById(leftId).orElse(null);
        Execution right = executionRepository.findById(rightId).orElse(null);
        Map<String, Object> result = new HashMap<>();
        result.put("left", left != null ? buildComparison(left) : null);
        result.put("right", right != null ? buildComparison(right) : null);
        return result;
    }

    private Map<String, Object> buildComparison(Execution e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("buildNumber", e.getBuildNumber());
        m.put("platform", e.getPlatform());
        m.put("environment", e.getEnvironment());
        m.put("firmwareVersion", e.getFirmwareVersion());
        m.put("appVersion", e.getAppVersion());
        m.put("status", e.getStatus());
        m.put("passCount", e.getPassCount());
        m.put("failCount", e.getFailCount());
        m.put("skipCount", e.getSkipCount());
        m.put("durationMs", e.getDurationMs());
        m.put("startTime", e.getStartTime());
        return m;
    }
}
