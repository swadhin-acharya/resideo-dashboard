package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.ExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {

    List<ExecutionLog> findByExecutionIdOrderByTimestampAsc(UUID executionId);

    Page<ExecutionLog> findByExecutionIdOrderByTimestampDesc(UUID executionId, Pageable pageable);

    void deleteByExecutionId(UUID executionId);
}
