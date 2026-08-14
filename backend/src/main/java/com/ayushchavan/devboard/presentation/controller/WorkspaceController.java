package com.ayushchavan.devboard.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ayushchavan.devboard.application.dto.workspace.AddWorkspaceMemberRequest;
import com.ayushchavan.devboard.application.dto.workspace.CreateWorkspaceRequest;
import com.ayushchavan.devboard.application.dto.workspace.UpdateWorkspaceRequest;
import com.ayushchavan.devboard.application.dto.workspace.WorkspaceMemberResponse;
import com.ayushchavan.devboard.application.dto.workspace.WorkspaceResponse;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.application.service.WorkspaceService;
import com.ayushchavan.devboard.domain.entity.Workspace;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final WorkspaceMembershipService membershipService;

    public WorkspaceController(
            WorkspaceService workspaceService,
            WorkspaceMembershipService membershipService
    ) {
        this.workspaceService = workspaceService;
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            Authentication authentication,
            @RequestBody CreateWorkspaceRequest request
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        Workspace workspace = workspaceService.createWorkspace(
                userId,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WorkspaceResponse.from(workspace));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            Authentication authentication,
            @PathVariable UUID workspaceId
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        requireWorkspaceMember(workspaceId, userId);

        Workspace workspace = workspaceService.findById(workspaceId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Workspace not found"
                        )
                );

        return ResponseEntity.ok(
                WorkspaceResponse.from(workspace)
        );
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @RequestBody UpdateWorkspaceRequest request
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(workspaceId, userId);

        requireAdminOrOwner(actorRole);

        Workspace workspace = workspaceService.updateWorkspace(
                workspaceId,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.ok(
                WorkspaceResponse.from(workspace)
        );
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(
            Authentication authentication,
            @PathVariable UUID workspaceId
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(workspaceId, userId);

        requireOwner(actorRole);

        workspaceService.deleteWorkspace(workspaceId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<WorkspaceMemberResponse> getWorkspaceMember(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        requireWorkspaceMember(
                workspaceId,
                authenticatedUserId
        );

        WorkspaceMembership membership =
                membershipService.findByWorkspaceAndUser(
                        workspaceId,
                        userId
                ).orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Workspace membership not found"
                        )
                );

        return ResponseEntity.ok(
                WorkspaceMemberResponse.from(membership)
        );
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<WorkspaceMemberResponse> addWorkspaceMember(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @RequestBody AddWorkspaceMemberRequest request
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(workspaceId, userId);

        requireAdminOrOwner(actorRole);

        WorkspaceMembership membership =
                membershipService.createMembership(
                        workspaceId,
                        request.getUserId(),
                        WorkspaceRole.MEMBER
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WorkspaceMemberResponse.from(membership));
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<Void> removeWorkspaceMember(
            Authentication authentication,
            @PathVariable UUID workspaceId,
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

        WorkspaceMembership target =
                membershipService.findByWorkspaceAndUser(
                        workspaceId,
                        userId
                ).orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Workspace membership not found"
                        )
                );

        if (target.getRole() == WorkspaceRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Workspace owner cannot be removed"
            );
        }

        membershipService.deleteMembership(
                workspaceId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId(
            Authentication authentication
    ) {
        return (UUID) authentication.getPrincipal();
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
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "User is not a member of this workspace"
                        )
                );
    }

    private void requireWorkspaceMember(
            UUID workspaceId,
            UUID userId
    ) {
        getRequiredWorkspaceRole(workspaceId, userId);
    }

    private void requireAdminOrOwner(
            WorkspaceRole role
    ) {
        if (role != WorkspaceRole.OWNER
                && role != WorkspaceRole.ADMIN) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Insufficient workspace permissions"
            );
        }
    }

    private void requireOwner(
            WorkspaceRole role
    ) {
        if (role != WorkspaceRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the workspace owner can perform this action"
            );
        }
    }
}