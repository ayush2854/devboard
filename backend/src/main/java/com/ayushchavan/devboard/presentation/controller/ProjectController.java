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

import com.ayushchavan.devboard.application.dto.project.CreateProjectRequest;
import com.ayushchavan.devboard.application.dto.project.ProjectResponse;
import com.ayushchavan.devboard.application.dto.project.UpdateProjectRequest;
import com.ayushchavan.devboard.application.exception.ForbiddenException;
import com.ayushchavan.devboard.application.service.ProjectService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final WorkspaceMembershipService membershipService;

    public ProjectController(
            ProjectService projectService,
            WorkspaceMembershipService membershipService
    ) {
        this.projectService = projectService;
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(
            Authentication authentication,
            @PathVariable UUID workspaceId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        requireWorkspaceMember(
                workspaceId,
                authenticatedUserId
        );

        List<ProjectResponse> response =
                projectService.findAllByWorkspaceId(workspaceId)
                        .stream()
                        .map(ProjectResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
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

        return ResponseEntity.ok(
                ProjectResponse.from(project)
        );
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateProjectRequest request
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
                projectService.createProject(
                        workspaceId,
                        request.getName(),
                        request.getDescription()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProjectResponse.from(project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        Project existingProject =
                projectService.findById(projectId);

        requireProjectBelongsToWorkspace(
                existingProject,
                workspaceId
        );

        Project project =
                projectService.updateProject(
                        projectId,
                        request.getName(),
                        request.getDescription()
                );

        return ResponseEntity.ok(
                ProjectResponse.from(project)
        );
    }

    @PutMapping("/{projectId}/archive")
    public ResponseEntity<ProjectResponse> archiveProject(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        Project existingProject =
                projectService.findById(projectId);

        requireProjectBelongsToWorkspace(
                existingProject,
                workspaceId
        );

        Project project =
                projectService.archiveProject(projectId);

        return ResponseEntity.ok(
                ProjectResponse.from(project)
        );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        Project existingProject =
                projectService.findById(projectId);

        requireProjectBelongsToWorkspace(
                existingProject,
                workspaceId
        );

        projectService.deleteProject(projectId);

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
