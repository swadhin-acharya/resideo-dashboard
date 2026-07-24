package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.ExecutionFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureRepository extends JpaRepository<ExecutionFeature, String> {
    List<ExecutionFeature> findByExecutionIdOrderByFeatureName(String executionId);
}
