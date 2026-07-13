package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.ProjectMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {
    List<ProjectMembership> findByProjectId(UUID projectId);
    List<ProjectMembership> findByUserId(UUID userId);
    Optional<ProjectMembership> findByProjectIdAndUserId(UUID projectId, UUID userId);
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
}
