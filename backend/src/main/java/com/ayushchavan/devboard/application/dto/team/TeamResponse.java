package com.ayushchavan.devboard.application.dto.team;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.Team;

public class TeamResponse {

    private final UUID id;
    private final UUID workspaceId;
    private final String name;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;

    private TeamResponse(
            UUID id,
            UUID workspaceId,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getWorkspaceId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt(),
                team.getUpdatedAt()
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
