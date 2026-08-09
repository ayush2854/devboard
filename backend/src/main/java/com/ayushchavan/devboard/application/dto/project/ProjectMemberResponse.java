package com.ayushchavan.devboard.application.dto.project;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.ProjectMembership;

public class ProjectMemberResponse {

    private UUID id;
    private UUID projectId;
    private UUID userId;
    private Instant joinedAt;

    public ProjectMemberResponse(
            UUID id,
            UUID projectId,
            UUID userId,
            Instant joinedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public static ProjectMemberResponse from(
            ProjectMembership membership
    ) {
        return new ProjectMemberResponse(
                membership.getId(),
                membership.getProjectId(),
                membership.getUserId(),
                membership.getJoinedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}