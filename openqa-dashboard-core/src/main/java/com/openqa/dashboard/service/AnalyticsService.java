package com.openqa.dashboard.service;

import com.openqa.dashboard.repository.ExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ExecutionRepository executionRepository;

    public AnalyticsService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    public List<Map<String, Object>> getTrends(int days) {
        List<Object[]> rows = executionRepository.dailyTrend();
        List<Map<String, Object>> trends = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", row[0] != null ? row[0].toString() : "");
            entry.put("total", row[1] != null ? ((Number) row[1]).longValue() : 0);
            entry.put("passed", row[2] != null ? ((Number) row[2]).longValue() : 0);
            entry.put("failed", row[3] != null ? ((Number) row[3]).longValue() : 0);
            trends.add(entry);
        }
        return trends;
    }

    public Map<String, Object> getDeviceStats() {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Long> byPlatform = new HashMap<>();
        byPlatform.put("ANDROID", executionRepository.countByStatus("PASSED"));
        stats.put("byPlatform", byPlatform);
        return stats;
    }
}
