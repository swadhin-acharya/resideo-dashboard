package com.resideo.dashboard.repository;

import com.resideo.dashboard.model.entity.ExecutionFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionFeatureRepository extends JpaRepository<ExecutionFeature, UUID> {

    List<ExecutionFeature> findByExecutionId(UUID executionId);

    void deleteByExecutionId(UUID executionId);

    @Query("SELECT f.featureName, COUNT(f), SUM(CASE WHEN f.status = 'PASSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN f.status = 'FAILED' THEN 1 ELSE 0 END) " +
           "FROM ExecutionFeature f GROUP BY f.featureName ORDER BY COUNT(f) DESC")
    List<Object[]> findFeatureStability();

    @Query("SELECT f.featureName, COUNT(f), AVG(f.durationMs) " +
           "FROM ExecutionFeature f GROUP BY f.featureName ORDER BY AVG(f.durationMs) DESC")
    List<Object[]> findFeatureDurationTrends();

    @Query("SELECT f.featureName, SUM(f.failCount) as failures " +
           "FROM ExecutionFeature f GROUP BY f.featureName ORDER BY failures DESC")
    List<Object[]> findMostFailedFeatures(@Param("limit") int limit);
}
