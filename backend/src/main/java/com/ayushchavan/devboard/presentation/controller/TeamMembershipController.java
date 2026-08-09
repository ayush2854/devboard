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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ayushchavan.devboard.application.dto.team.AddTeamMemberRequest;
import com.ayushchavan.devboard.application.dto.team.TeamMemberResponse;
import com.ayushchavan.devboard.application.exception.ForbiddenException;
import com.ayushchavan.devboard.application.service.TeamMembershipService;
import com.ayushchavan.devboard.application.service.TeamService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Team;
import com.ayushchavan.devboard.domain.entity.TeamMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/teams/{teamId}/members")
public class TeamMembershipController {

    private final TeamMembershipService teamMembershipService;
    private final TeamService teamService;
    private final WorkspaceMembershipService workspaceMembershipService;

    public TeamMembershipController(
            TeamMembershipService teamMembershipService,
            TeamService teamService,
            WorkspaceMembershipService workspaceMembershipService
    ) {
        this.teamMembershipService = teamMembershipService;
        this.teamService = teamService;
        this.workspaceMembershipService =
                workspaceMembershipService;
    }

    @GetMapping
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(
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

        List<TeamMemberResponse> response =
                teamMembershipService
                        .findAllByTeamId(teamId)
                        .stream()
                        .map(TeamMemberResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TeamMemberResponse> addTeamMember(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID teamId,
            @RequestBody AddTeamMemberRequest request
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
                teamService.findById(teamId);

        requireTeamBelongsToWorkspace(
                team,
                workspaceId
        );

        TeamMembership membership =
                teamMembershipService.addMember(
                        teamId,
                        request.getUserId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TeamMemberResponse.from(membership));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeTeamMember(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID teamId,
            @PathVariable UUID userId
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
                teamService.findById(teamId);

        requireTeamBelongsToWorkspace(
                team,
                workspaceId
        );

        teamMembershipService.removeMember(
                teamId,
                userId
        );

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
        return workspaceMembershipService
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