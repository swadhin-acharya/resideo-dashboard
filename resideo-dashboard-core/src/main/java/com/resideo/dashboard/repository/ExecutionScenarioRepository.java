package com.resideo.dashboard.repository;

import com.resideo.dashboard.model.entity.ExecutionScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionScenarioRepository extends JpaRepository<ExecutionScenario, UUID> {

    List<ExecutionScenario> findByExecutionId(UUID executionId);

    List<ExecutionScenario> findByFeatureId(UUID featureId);

    void deleteByExecutionId(UUID executionId);

    @Query("SELECT s.scenarioName, COUNT(s), " +
           "SUM(CASE WHEN s.status = 'PASSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 'FAILED' THEN 1 ELSE 0 END) " +
           "FROM ExecutionScenario s GROUP BY s.scenarioName " +
           "HAVING COUNT(s) > 1 AND SUM(CASE WHEN s.status = 'FAILED' THEN 1 ELSE 0 END) > 0 " +
           "AND SUM(CASE WHEN s.status = 'PASSED' THEN 1 ELSE 0 END) > 0 " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> findFlakyScenarios();

    @Query("SELECT s.scenarioName, SUM(CASE WHEN s.status = 'FAILED' THEN 1 ELSE 0 END) as failures " +
           "FROM ExecutionScenario s GROUP BY s.scenarioName ORDER BY failures DESC")
    List<Object[]> findMostFailedScenarios();
}
