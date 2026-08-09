package com.ayushchavan.devboard.application.dto.team;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.TeamMembership;

public record TeamMemberResponse(
        UUID id,
        UUID teamId,
        UUID userId,
        Instant joinedAt
) {

    public static TeamMemberResponse from(
            TeamMembership membership
    ) {
        return new TeamMemberResponse(
                membership.getId(),
                membership.getTeamId(),
                membership.getUserId(),
                membership.getJoinedAt()
        );
    }
}