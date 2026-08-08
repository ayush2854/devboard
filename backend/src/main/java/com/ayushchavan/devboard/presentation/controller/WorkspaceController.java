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

import com.ayushchavan.devboard.application.dto.workspace.CreateWorkspaceRequest;
import com.ayushchavan.devboard.application.dto.workspace.UpdateWorkspaceRequest;
import com.ayushchavan.devboard.application.dto.workspace.WorkspaceResponse;
import com.ayushchavan.devboard.application.service.WorkspaceService;
import com.ayushchavan.devboard.domain.entity.Workspace;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            Authentication authentication,
            @RequestBody CreateWorkspaceRequest request
    ) {
        Workspace workspace = workspaceService.createWorkspace(
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
        Workspace workspace = workspaceService.findById(workspaceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workspace not found")
                );

        return ResponseEntity.ok(WorkspaceResponse.from(workspace));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @RequestBody UpdateWorkspaceRequest request
    ) {
        Workspace workspace = workspaceService.updateWorkspace(
                workspaceId,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.ok(WorkspaceResponse.from(workspace));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(
            Authentication authentication,
            @PathVariable UUID workspaceId
    ) {
        workspaceService.deleteWorkspace(workspaceId);

        return ResponseEntity.noContent().build();
    }
}