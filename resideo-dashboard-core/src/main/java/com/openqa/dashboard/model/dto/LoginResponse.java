package com.openqa.dashboard.model.dto;

import java.util.List;
import java.util.UUID;

public class LoginResponse {
    private String token;
    private UUID userId;
    private String username;
    private String displayName;
    private String globalRole;
    private List<ProjectInfo> projects;

    public static class ProjectInfo {
        private UUID id;
        private String name;
        private String slug;
        private String role;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getGlobalRole() { return globalRole; }
    public void setGlobalRole(String globalRole) { this.globalRole = globalRole; }
    public List<ProjectInfo> getProjects() { return projects; }
    public void setProjects(List<ProjectInfo> projects) { this.projects = projects; }
}
