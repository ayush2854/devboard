package com.ayushchavan.devboard.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;

public interface WorkspaceMembershipRepository
        extends JpaRepository<WorkspaceMembership, UUID> {

    Optional<WorkspaceMembership> findByWorkspaceIdAndUserId(
            UUID workspaceId,
            UUID userId
    );

    boolean existsByWorkspaceIdAndUserId(
            UUID workspaceId,
            UUID userId
    );
}