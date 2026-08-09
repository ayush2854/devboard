package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.application.exception.ConflictException;
import com.ayushchavan.devboard.application.exception.ResourceNotFoundException;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;
import com.ayushchavan.devboard.domain.repository.WorkspaceMembershipRepository;

@Service
public class WorkspaceMembershipService {

    private final WorkspaceMembershipRepository membershipRepository;

    public WorkspaceMembershipService(
            WorkspaceMembershipRepository membershipRepository
    ) {
        this.membershipRepository = membershipRepository;
    }

    public Optional<WorkspaceMembership> findByWorkspaceAndUser(
            UUID workspaceId,
            UUID userId
    ) {
        return membershipRepository.findByWorkspaceIdAndUserId(
                workspaceId,
                userId
        );
    }

    public boolean isMember(
            UUID workspaceId,
            UUID userId
    ) {
        return membershipRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                userId
        );
    }

    public WorkspaceMembership createMembership(
            UUID workspaceId,
            UUID userId,
            WorkspaceRole role
    ) {
        if (membershipRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )) {
            throw new ConflictException(
                    "User is already a member of this workspace"
            );
        }

        if (role == null) {
            throw new IllegalArgumentException(
                    "Workspace role is required"
            );
        }

        WorkspaceMembership membership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        userId,
                        role,
                        Instant.now()
                );

        return membershipRepository.save(membership);
    }

    public void deleteMembership(
            UUID workspaceId,
            UUID userId
    ) {
        WorkspaceMembership membership =
                membershipRepository
                        .findByWorkspaceIdAndUserId(
                                workspaceId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Workspace membership not found"
                                )
                        );

        membershipRepository.delete(membership);
    }
}
