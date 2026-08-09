package com.ayushchavan.devboard.application.dto.workspace;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

public class WorkspaceMemberResponse {

    private final UUID id;
    private final UUID workspaceId;
    private final UUID userId;
    private final WorkspaceRole role;
    private final Instant joinedAt;

    public WorkspaceMemberResponse(
            UUID id,
            UUID workspaceId,
            UUID userId,
            WorkspaceRole role,
            Instant joinedAt
    ) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static WorkspaceMemberResponse from(
            WorkspaceMembership membership
    ) {
        return new WorkspaceMemberResponse(
                membership.getId(),
                membership.getWorkspaceId(),
                membership.getUserId(),
                membership.getRole(),
                membership.getJoinedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public WorkspaceRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}