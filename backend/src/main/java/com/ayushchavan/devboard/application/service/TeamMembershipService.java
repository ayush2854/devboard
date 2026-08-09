package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.domain.entity.TeamMembership;
import com.ayushchavan.devboard.domain.repository.TeamMembershipRepository;
import com.ayushchavan.devboard.domain.repository.TeamRepository;
import com.ayushchavan.devboard.domain.repository.UserRepository;

@Service
public class TeamMembershipService {

    private final TeamMembershipRepository membershipRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamMembershipService(
            TeamMembershipRepository membershipRepository,
            TeamRepository teamRepository,
            UserRepository userRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public List<TeamMembership> findAllByTeamId(UUID teamId) {
        requireTeamExists(teamId);

        return membershipRepository.findAllByTeamId(teamId);
    }

    public TeamMembership findById(UUID membershipId) {
        return membershipRepository.findById(membershipId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Team membership not found"
                        )
                );
    }

    public TeamMembership findByTeamAndUser(
            UUID teamId,
            UUID userId
    ) {
        return membershipRepository
                .findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Team membership not found"
                        )
                );
    }

    public TeamMembership addMember(
            UUID teamId,
            UUID userId
    ) {
        requireTeamExists(teamId);
        requireUserExists(userId);
        requireNotAlreadyMember(teamId, userId);

        TeamMembership membership = new TeamMembership(
                UUID.randomUUID(),
                teamId,
                userId,
                Instant.now()
        );

        return membershipRepository.save(membership);
    }

    public void removeMember(
            UUID teamId,
            UUID userId
    ) {
        TeamMembership membership =
                findByTeamAndUser(teamId, userId);

        membershipRepository.delete(membership);
    }

    private void requireTeamExists(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException(
                    "Team not found"
            );
        }
    }

    private void requireUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }
    }

    private void requireNotAlreadyMember(
            UUID teamId,
            UUID userId
    ) {
        if (membershipRepository.existsByTeamIdAndUserId(
                teamId,
                userId
        )) {
            throw new IllegalArgumentException(
                    "User is already a member of this team"
            );
        }
    }
}