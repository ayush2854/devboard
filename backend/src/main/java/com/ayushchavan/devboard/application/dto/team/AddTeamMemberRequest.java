package com.ayushchavan.devboard.application.dto.team;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddTeamMemberRequest(

@NotNull(message = "User ID is required")
UUID userId

) {

public UUID getUserId() {
    return userId;
}

}
