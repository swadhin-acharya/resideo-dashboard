package com.openqa.dashboard.model.dto;

import com.openqa.dashboard.model.entity.Project;

import java.time.Instant;
import java.util.UUID;

public class ProjectResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String slug;
    private String description;
    private boolean archived;
    private Instant createdAt;

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id = p.getId();
        r.organizationId = p.getOrganizationId();
        r.name = p.getName();
        r.slug = p.getSlug();
        r.description = p.getDescription();
        r.archived = p.getArchived();
        r.createdAt = p.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
}
