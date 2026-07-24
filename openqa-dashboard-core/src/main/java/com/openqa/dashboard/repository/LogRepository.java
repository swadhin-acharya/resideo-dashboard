package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<ExecutionLog, Long> {
    List<ExecutionLog> findByExecutionIdOrderByTimestamp(String executionId);
}
