package com.ayushchavan.devboard.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayushchavan.devboard.domain.entity.TeamMembership;

public interface TeamMembershipRepository
        extends JpaRepository<TeamMembership, UUID> {

    List<TeamMembership> findAllByTeamId(UUID teamId);

    Optional<TeamMembership> findByTeamIdAndUserId(
            UUID teamId,
            UUID userId
    );

    boolean existsByTeamIdAndUserId(
            UUID teamId,
            UUID userId
    );
}