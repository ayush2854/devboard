package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.application.exception.ConflictException;
import com.ayushchavan.devboard.application.exception.ResourceNotFoundException;
import com.ayushchavan.devboard.domain.entity.ProjectMembership;
import com.ayushchavan.devboard.domain.repository.ProjectMembershipRepository;

@Service
public class ProjectMembershipService {

    private final ProjectMembershipRepository membershipRepository;

    public ProjectMembershipService(
            ProjectMembershipRepository membershipRepository
    ) {
        this.membershipRepository = membershipRepository;
    }

    public List<ProjectMembership> findAllByProjectId(
            UUID projectId
    ) {
        return membershipRepository.findAllByProjectId(projectId);
    }

    public ProjectMembership findByProjectAndUser(
            UUID projectId,
            UUID userId
    ) {
        return membershipRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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

        return membershipRepository.save(
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

        membershipRepository.delete(
                membership
        );
    }

    private void requireUniqueMembership(
            UUID projectId,
            UUID userId
    ) {
        if (membershipRepository.existsByProjectIdAndUserId(
                projectId,
                userId
        )) {
            throw new ConflictException(
                    "User is already a member of this project"
            );
        }
    }
}