package com.ayushchavan.devboard.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_memberships")
public class TeamMembership {

    @Id
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected TeamMembership() {
        // Required by JPA
    }

    public TeamMembership(
            UUID id,
            UUID teamId,
            UUID userId,
            Instant joinedAt
    ) {
        this.id = id;
        this.teamId = teamId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}