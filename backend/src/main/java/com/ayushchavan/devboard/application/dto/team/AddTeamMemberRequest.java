package com.ayushchavan.devboard.application.dto.team;

import java.util.UUID;

public record AddTeamMemberRequest(
        UUID userId
) {

    public UUID getUserId() {
        return userId;
    }
}