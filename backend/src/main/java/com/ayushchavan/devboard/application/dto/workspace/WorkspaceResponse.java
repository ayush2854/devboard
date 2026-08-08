package com.ayushchavan.devboard.application.dto.workspace;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.Workspace;

public class WorkspaceResponse {

    private final UUID id;
    private final String name;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;

    public WorkspaceResponse(
            UUID id,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}