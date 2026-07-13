package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.dto.ProjectResponse;
import com.openqa.dashboard.model.enums.ProjectRole;
import com.openqa.dashboard.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects() {
        return ResponseEntity.ok(projectService.listProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody Map<String, String> body) {
        String orgIdStr = body.get("organizationId");
        UUID orgId = orgIdStr != null ? UUID.fromString(orgIdStr) : null;
        return ResponseEntity.ok(projectService.createProject(
                body.get("name"), body.get("slug"), body.get("description"), orgId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable UUID id,
                                                          @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(projectService.updateProject(id, body.get("name"), body.get("description")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveProject(@PathVariable UUID id) {
        projectService.archiveProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<Map<String, Object>>> getMembers(@PathVariable UUID id) {
        var members = projectService.getMembers(id).stream().<Map<String, Object>>map(m -> {
            var userOpt = projectService.findUserById(m.getUserId());
            var map = new java.util.HashMap<String, Object>();
            map.put("userId", m.getUserId());
            map.put("role", m.getRole().name());
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                map.put("username", user.getUsername());
                map.put("email", user.getEmail());
                map.put("displayName", user.getDisplayName());
            }
            return (Map<String, Object>) map;
        }).toList();
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        projectService.addMember(id, UUID.fromString(body.get("userId")),
                ProjectRole.valueOf(body.getOrDefault("role", "AUTOMATION_ENGINEER").toUpperCase()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        projectService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> updateMemberRole(@PathVariable UUID id, @PathVariable UUID userId,
                                                  @RequestBody Map<String, String> body) {
        projectService.updateMemberRole(id, userId,
                ProjectRole.valueOf(body.get("role").toUpperCase()));
        return ResponseEntity.ok().build();
    }
}
