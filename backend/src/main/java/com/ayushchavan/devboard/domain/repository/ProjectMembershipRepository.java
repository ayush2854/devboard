package com.ayushchavan.devboard.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayushchavan.devboard.domain.entity.ProjectMembership;

public interface ProjectMembershipRepository
        extends JpaRepository<ProjectMembership, UUID> {

    List<ProjectMembership> findAllByProjectId(UUID projectId);

    Optional<ProjectMembership> findByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );

    boolean existsByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );
}
