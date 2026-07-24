package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.Execution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExecutionRepository extends JpaRepository<Execution, String> {

    List<Execution> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT e FROM Execution e ORDER BY e.createdAt DESC")
    Page<Execution> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(e) FROM Execution e WHERE e.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(e.passCount), 0) FROM Execution e")
    long sumPassCount();

    @Query("SELECT COALESCE(SUM(e.failCount), 0) FROM Execution e")
    long sumFailCount();

    @Query("SELECT COALESCE(SUM(e.skipCount), 0) FROM Execution e")
    long sumSkipCount();

    @Query("SELECT COALESCE(AVG(e.durationMs), 0) FROM Execution e WHERE e.durationMs > 0")
    double avgDurationMs();

    @Query("SELECT e FROM Execution e WHERE e.status IN ('RUNNING', 'PENDING') ORDER BY e.createdAt DESC")
    List<Execution> findRunningOrPending();

    @Query("SELECT e FROM Execution e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<Execution> findPending();

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as total, " +
           "SUM(CASE WHEN status = 'PASSED' THEN 1 ELSE 0 END) as passed, " +
           "SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed " +
           "FROM executions GROUP BY DATE(created_at) ORDER BY DATE(created_at)", nativeQuery = true)
    List<Object[]> dailyTrend();
}
