package com.resideo.dashboard.repository;

import com.resideo.dashboard.model.entity.Execution;
import com.resideo.dashboard.model.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, UUID> {

    Page<Execution> findByStatusOrderByCreatedAtDesc(ExecutionStatus status, Pageable pageable);

    Page<Execution> findByPlatformOrderByCreatedAtDesc(String platform, Pageable pageable);

    List<Execution> findByStatus(ExecutionStatus status);

    long countByStatus(ExecutionStatus status);

    long countByProjectId(UUID projectId);

    long countByProjectIdAndStatus(UUID projectId, ExecutionStatus status);

    List<Execution> findByProjectIdAndStatus(UUID projectId, ExecutionStatus status);

    Page<Execution> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

    List<Execution> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Execution> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, ExecutionStatus status);

    Optional<Execution> findByIdAndProjectId(UUID id, UUID projectId);

    @Query("SELECT e FROM Execution e WHERE " +
           "(:projectId IS NULL OR e.projectId = :projectId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:platform IS NULL OR e.platform = :platform) AND " +
           "(:environment IS NULL OR e.environment = :environment) AND " +
           "(:firmware IS NULL OR e.firmwareVersion = :firmware) AND " +
           "(:appVersion IS NULL OR e.appVersion = :appVersion) AND " +
           "(e.visibility IN :visibilities) " +
           "ORDER BY e.createdAt DESC")
    Page<Execution> search(@Param("projectId") UUID projectId,
                           @Param("status") ExecutionStatus status,
                           @Param("platform") String platform,
                           @Param("environment") String environment,
                           @Param("firmware") String firmware,
                           @Param("appVersion") String appVersion,
                           @Param("visibilities") List<String> visibilities,
                           Pageable pageable);

    @Query("SELECT e FROM Execution e WHERE (e.reportPath IS NOT NULL " +
           "OR e.status IN ('PASSED','FAILED','ABORTED','TIMEOUT')) " +
           "AND (:projectId IS NULL OR e.projectId = :projectId) " +
           "ORDER BY e.createdAt DESC")
    List<Execution> findWithReports(@Param("projectId") UUID projectId);

    @Query("SELECT e FROM Execution e WHERE e.createdAt BETWEEN :from AND :to " +
           "AND (:projectId IS NULL OR e.projectId = :projectId) " +
           "ORDER BY e.createdAt ASC")
    List<Execution> findByDateRange(@Param("from") Instant from,
                                    @Param("to") Instant to,
                                    @Param("projectId") UUID projectId);
}
