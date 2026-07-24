package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.ExecutionStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepRepository extends JpaRepository<ExecutionStep, Long> {
    List<ExecutionStep> findByScenarioIdOrderById(String scenarioId);
}
