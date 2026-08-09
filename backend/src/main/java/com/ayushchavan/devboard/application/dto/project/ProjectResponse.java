package com.ayushchavan.devboard.application.dto.project;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.ProjectStatus;

public class ProjectResponse {

    private UUID id;
    private UUID workspaceId;
    private String name;
    private String description;
    private ProjectStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant archivedAt;

    public ProjectResponse(
            UUID id,
            UUID workspaceId,
            String name,
            String description,
            ProjectStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant archivedAt
    ) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
    }

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getWorkspaceId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getArchivedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }
}