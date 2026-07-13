package com.openqa.dashboard.service;

import com.openqa.dashboard.model.dto.ProjectResponse;
import com.openqa.dashboard.model.entity.Organization;
import com.openqa.dashboard.model.entity.Project;
import com.openqa.dashboard.model.entity.ProjectMembership;
import com.openqa.dashboard.model.entity.UserEntity;
import com.openqa.dashboard.model.enums.ProjectRole;
import com.openqa.dashboard.repository.OrganizationRepository;
import com.openqa.dashboard.repository.ProjectMembershipRepository;
import com.openqa.dashboard.repository.ProjectRepository;
import com.openqa.dashboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public ProjectService(OrganizationRepository organizationRepository,
                          ProjectRepository projectRepository,
                          ProjectMembershipRepository membershipRepository,
                          UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectResponse> listProjects() {
        return projectRepository.findByArchivedFalse().stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> listProjectsByOrganization(UUID organizationId) {
        return projectRepository.findByOrganizationId(organizationId).stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(UUID id) {
        return projectRepository.findById(id)
                .map(ProjectResponse::from)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public ProjectResponse createProject(String name, String slug, String description, UUID organizationId) {
        if (organizationId == null) {
            List<Organization> orgs = organizationRepository.findAll();
            if (orgs.isEmpty()) {
                throw new RuntimeException("No organization exists. Create an organization first.");
            }
            organizationId = orgs.get(0).getId();
        }

        Project project = new Project();
        project.setOrganizationId(organizationId);
        project.setName(name);
        project.setSlug(slug);
        project.setDescription(description);
        project = projectRepository.save(project);

        return ProjectResponse.from(project);
    }

    public ProjectResponse updateProject(UUID id, String name, String description) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (name != null) project.setName(name);
        if (description != null) project.setDescription(description);
        project = projectRepository.save(project);
        return ProjectResponse.from(project);
    }

    public void archiveProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setArchived(true);
        projectRepository.save(project);
    }

    public void addMember(UUID projectId, UUID userId, ProjectRole role) {
        if (membershipRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new RuntimeException("User is already a member of this project");
        }
        ProjectMembership membership = new ProjectMembership();
        membership.setProjectId(projectId);
        membership.setUserId(userId);
        membership.setRole(role);
        membershipRepository.save(membership);
    }

    public void removeMember(UUID projectId, UUID userId) {
        membershipRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    public void updateMemberRole(UUID projectId, UUID userId, ProjectRole role) {
        ProjectMembership membership = membershipRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));
        membership.setRole(role);
        membershipRepository.save(membership);
    }

    public List<ProjectMembership> getMembers(UUID projectId) {
        return membershipRepository.findByProjectId(projectId);
    }

    public java.util.Optional<UserEntity> findUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public List<ProjectResponse> getUserProjects(UUID userId) {
        List<ProjectMembership> memberships = membershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(m -> projectRepository.findById(m.getProjectId()).orElse(null))
                .filter(p -> p != null && !p.getArchived())
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }
}
