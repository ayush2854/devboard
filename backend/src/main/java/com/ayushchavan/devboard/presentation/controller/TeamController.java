package com.ayushchavan.devboard.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ayushchavan.devboard.application.dto.team.CreateTeamRequest;
import com.ayushchavan.devboard.application.dto.team.TeamResponse;
import com.ayushchavan.devboard.application.dto.team.UpdateTeamRequest;
import com.ayushchavan.devboard.application.exception.ForbiddenException;
import com.ayushchavan.devboard.application.service.TeamService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Team;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/teams")
public class TeamController {

    private final TeamService teamService;
    private final WorkspaceMembershipService membershipService;

    public TeamController(
            TeamService teamService,
            WorkspaceMembershipService membershipService
    ) {
        this.teamService = teamService;
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams(
            Authentication authentication,
            @PathVariable UUID workspaceId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        requireWorkspaceMember(
                workspaceId,
                authenticatedUserId
        );

        List<TeamResponse> response =
                teamService.findAllByWorkspaceId(workspaceId)
                        .stream()
                        .map(TeamResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID teamId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        requireWorkspaceMember(
                workspaceId,
                authenticatedUserId
        );

        Team team =
                teamService.findById(teamId);

        requireTeamBelongsToWorkspace(
                team,
                workspaceId
        );

        return ResponseEntity.ok(
                TeamResponse.from(team)
        );
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @RequestBody CreateTeamRequest request
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        Team team =
                teamService.createTeam(
                        workspaceId,
                        request.getName(),
                        request.getDescription()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TeamResponse.from(team));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID teamId,
            @RequestBody UpdateTeamRequest request
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        Team existingTeam =
                teamService.findById(teamId);

        requireTeamBelongsToWorkspace(
                existingTeam,
                workspaceId
        );

        Team team =
                teamService.updateTeam(
                        teamId,
                        request.getName(),
                        request.getDescription()
                );

        return ResponseEntity.ok(
                TeamResponse.from(team)
        );
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID teamId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        Team existingTeam =
                teamService.findById(teamId);

        requireTeamBelongsToWorkspace(
                existingTeam,
                workspaceId
        );

        teamService.deleteTeam(teamId);

        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId(
            Authentication authentication
    ) {
        Object principal =
                authentication.getPrincipal();

        if (principal instanceof UUID userId) {
            return userId;
        }

        if (principal instanceof UserDetails userDetails) {
            return UUID.fromString(
                    userDetails.getUsername()
            );
        }

        return UUID.fromString(
                authentication.getName()
        );
    }

    private WorkspaceRole getRequiredWorkspaceRole(
            UUID workspaceId,
            UUID userId
    ) {
        return membershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        userId
                )
                .map(WorkspaceMembership::getRole)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "User is not a member of this workspace"
                        )
                );
    }

    private void requireWorkspaceMember(
            UUID workspaceId,
            UUID userId
    ) {
        getRequiredWorkspaceRole(
                workspaceId,
                userId
        );
    }

    private void requireAdminOrOwner(
            WorkspaceRole role
    ) {
        if (role != WorkspaceRole.OWNER
                && role != WorkspaceRole.ADMIN) {

            throw new ForbiddenException(
                    "Insufficient workspace permissions"
            );
        }
    }

    private void requireTeamBelongsToWorkspace(
            Team team,
            UUID workspaceId
    ) {
        if (!team.getWorkspaceId().equals(workspaceId)) {
            throw new ForbiddenException(
                    "Team does not belong to this workspace"
            );
        }
    }
}