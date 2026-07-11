package com.resideo.dashboard.model.dto;

import com.resideo.dashboard.model.entity.UserEntity;

import java.time.Instant;
import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String globalRole;
    private boolean enabled;
    private Instant lastLoginAt;
    private Instant createdAt;

    public static UserResponse from(UserEntity u) {
        UserResponse r = new UserResponse();
        r.id = u.getId();
        r.username = u.getUsername();
        r.email = u.getEmail();
        r.displayName = u.getDisplayName();
        r.globalRole = u.getGlobalRole().name();
        r.enabled = u.getEnabled();
        r.lastLoginAt = u.getLastLoginAt();
        r.createdAt = u.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getGlobalRole() { return globalRole; }
    public boolean isEnabled() { return enabled; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public Instant getCreatedAt() { return createdAt; }
}
