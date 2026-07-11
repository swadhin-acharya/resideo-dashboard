package com.resideo.dashboard.model.dto;

import com.resideo.dashboard.model.entity.ApiToken;

import java.time.Instant;
import java.util.UUID;

public class ApiTokenResponse {
    private UUID id;
    private String name;
    private String tokenPrefix;
    private boolean enabled;
    private Instant lastUsedAt;
    private Instant expiresAt;
    private Instant createdAt;
    private String fullToken;

    public static ApiTokenResponse from(ApiToken t) {
        ApiTokenResponse r = new ApiTokenResponse();
        r.id = t.getId();
        r.name = t.getName();
        r.tokenPrefix = t.getTokenPrefix();
        r.enabled = t.getEnabled();
        r.lastUsedAt = t.getLastUsedAt();
        r.expiresAt = t.getExpiresAt();
        r.createdAt = t.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getTokenPrefix() { return tokenPrefix; }
    public boolean isEnabled() { return enabled; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public String getFullToken() { return fullToken; }
    public void setFullToken(String fullToken) { this.fullToken = fullToken; }
}
