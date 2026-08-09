package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.domain.entity.ProjectMembership;
import com.ayushchavan.devboard.domain.repository.ProjectMembershipRepository;

@Service
public class ProjectMembershipService {

    private final ProjectMembershipRepository projectMembershipRepository;

    public ProjectMembershipService(
            ProjectMembershipRepository projectMembershipRepository
    ) {
        this.projectMembershipRepository = projectMembershipRepository;
    }

    public List<ProjectMembership> findAllByProjectId(
            UUID projectId
    ) {
        return projectMembershipRepository
                .findAllByProjectId(projectId);
    }

    public ProjectMembership findByProjectAndUser(
            UUID projectId,
            UUID userId
    ) {
        return projectMembershipRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project membership not found"
                        )
                );
    }

    public ProjectMembership addMember(
            UUID projectId,
            UUID userId
    ) {
        requireUniqueMembership(
                projectId,
                userId
        );

        ProjectMembership membership =
                new ProjectMembership(
                        UUID.randomUUID(),
                        projectId,
                        userId,
                        Instant.now()
                );

        return projectMembershipRepository.save(
                membership
        );
    }

    public void removeMember(
            UUID projectId,
            UUID userId
    ) {
        ProjectMembership membership =
                findByProjectAndUser(
                        projectId,
                        userId
                );

        projectMembershipRepository.delete(
                membership
        );
    }

    private void requireUniqueMembership(
            UUID projectId,
            UUID userId
    ) {
        if (projectMembershipRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        userId
                )) {

            throw new IllegalArgumentException(
                    "User is already a member of this project"
            );
        }
    }
}