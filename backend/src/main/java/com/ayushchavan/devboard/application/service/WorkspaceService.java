package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.domain.entity.Workspace;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;
import com.ayushchavan.devboard.domain.repository.WorkspaceRepository;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipService membershipService;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            WorkspaceMembershipService membershipService
    ) {
        this.workspaceRepository = workspaceRepository;
        this.membershipService = membershipService;
    }

    public Optional<Workspace> findById(UUID id) {
        return workspaceRepository.findById(id);
    }

    public Workspace createWorkspace(
            UUID ownerId,
            String name,
            String description
    ) {
        Instant now = Instant.now();

        Workspace workspace = new Workspace(
                UUID.randomUUID(),
                name,
                description,
                now,
                now
        );

        Workspace savedWorkspace =
                workspaceRepository.save(workspace);

        membershipService.createMembership(
                savedWorkspace.getId(),
                ownerId,
                WorkspaceRole.OWNER
        );

        return savedWorkspace;
    }

    public Workspace updateWorkspace(
            UUID workspaceId,
            String name,
            String description
    ) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workspace not found")
                );

        workspace.update(name, description);

        return workspaceRepository.save(workspace);
    }

    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workspace not found")
                );

        workspaceRepository.delete(workspace);
    }
}
