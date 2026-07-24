package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, String> {
    List<Report> findByExecutionId(String executionId);
    List<Report> findAllByOrderByCreatedAtDesc();
}
