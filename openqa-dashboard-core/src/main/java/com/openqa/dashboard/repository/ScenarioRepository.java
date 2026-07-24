package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.ExecutionScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<ExecutionScenario, String> {
    List<ExecutionScenario> findByExecutionIdOrderByScenarioName(String executionId);
    List<ExecutionScenario> findByFeatureIdOrderByScenarioName(String featureId);
    long countByStatus(String status);
    long countByExecutionIdAndStatus(String executionId, String status);
}
