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

import com.ayushchavan.devboard.application.dto.project.AddProjectMemberRequest;
import com.ayushchavan.devboard.application.dto.project.ProjectMemberResponse;
import com.ayushchavan.devboard.application.exception.ForbiddenException;
import com.ayushchavan.devboard.application.service.ProjectMembershipService;
import com.ayushchavan.devboard.application.service.ProjectService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.ProjectMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/members")
public class ProjectMembershipController {

    private final ProjectMembershipService projectMembershipService;
    private final ProjectService projectService;
    private final WorkspaceMembershipService workspaceMembershipService;

    public ProjectMembershipController(
            ProjectMembershipService projectMembershipService,
            ProjectService projectService,
            WorkspaceMembershipService workspaceMembershipService
    ) {
        this.projectMembershipService = projectMembershipService;
        this.projectService = projectService;
        this.workspaceMembershipService = workspaceMembershipService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        requireWorkspaceMember(
                workspaceId,
                authenticatedUserId
        );

        Project project =
                projectService.findById(projectId);

        requireProjectBelongsToWorkspace(
                project,
                workspaceId
        );

        List<ProjectMemberResponse> response =
                projectMembershipService
                        .findAllByProjectId(projectId)
                        .stream()
                        .map(ProjectMemberResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> addMember(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @RequestBody AddProjectMemberRequest request
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        Project project =
                projectService.findById(projectId);

        requireProjectBelongsToWorkspace(
                project,
                workspaceId
        );

        ProjectMembership membership =
                projectMembershipService.addMember(
                        projectId,
                        request.getUserId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProjectMemberResponse.from(membership));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
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

        Project project =
                projectService.findById(projectId);

        requireProjectBelongsToWorkspace(
                project,
                workspaceId
        );

        projectMembershipService.removeMember(
                projectId,
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

    private void requireProjectBelongsToWorkspace(
            Project project,
            UUID workspaceId
    ) {
        if (!project.getWorkspaceId().equals(workspaceId)) {
            throw new ForbiddenException(
                    "Project does not belong to this workspace"
            );
        }
    }
}
