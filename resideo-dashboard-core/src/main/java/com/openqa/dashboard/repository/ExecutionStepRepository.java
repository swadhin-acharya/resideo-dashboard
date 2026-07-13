package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.ExecutionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionStepRepository extends JpaRepository<ExecutionStep, Long> {

    List<ExecutionStep> findByScenarioId(UUID scenarioId);

    List<ExecutionStep> findByScenarioIdOrderById(UUID scenarioId);

    void deleteByScenarioId(UUID scenarioId);
}
