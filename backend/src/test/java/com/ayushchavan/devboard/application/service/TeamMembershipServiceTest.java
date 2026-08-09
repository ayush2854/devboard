package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ayushchavan.devboard.application.exception.ConflictException;
import com.ayushchavan.devboard.application.exception.ResourceNotFoundException;
import com.ayushchavan.devboard.domain.entity.TeamMembership;
import com.ayushchavan.devboard.domain.repository.TeamMembershipRepository;
import com.ayushchavan.devboard.domain.repository.TeamRepository;
import com.ayushchavan.devboard.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TeamMembershipServiceTest {

    @Mock
    private TeamMembershipRepository membershipRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamMembershipService membershipService;

    private UUID teamId;
    private UUID userId;
    private UUID membershipId;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        userId = UUID.randomUUID();
        membershipId = UUID.randomUUID();
    }

    @Test
    void findAllByTeamId_shouldReturnMemberships() {
        TeamMembership membership =
                createMembership(
                        membershipId,
                        teamId,
                        userId
                );

        when(teamRepository.existsById(teamId))
                .thenReturn(true);

        when(membershipRepository.findAllByTeamId(teamId))
                .thenReturn(List.of(membership));

        List<TeamMembership> result =
                membershipService.findAllByTeamId(teamId);

        assertEquals(1, result.size());
        assertEquals(membershipId, result.get(0).getId());

        verify(teamRepository)
                .existsById(teamId);

        verify(membershipRepository)
                .findAllByTeamId(teamId);
    }

    @Test
    void findAllByTeamId_shouldRejectUnknownTeam() {
        when(teamRepository.existsById(teamId))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> membershipService
                                .findAllByTeamId(teamId)
                );

        assertEquals(
                "Team not found",
                exception.getMessage()
        );

        verify(membershipRepository, never())
                .findAllByTeamId(teamId);
    }

    @Test
    void findById_shouldReturnMembership() {
        TeamMembership membership =
                createMembership(
                        membershipId,
                        teamId,
                        userId
                );

        when(membershipRepository.findById(membershipId))
                .thenReturn(Optional.of(membership));

        TeamMembership result =
                membershipService.findById(membershipId);

        assertNotNull(result);
        assertEquals(
                membershipId,
                result.getId()
        );
    }

    @Test
    void findById_shouldRejectUnknownMembership() {
        when(membershipRepository.findById(membershipId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> membershipService
                                .findById(membershipId)
                );

        assertEquals(
                "Team membership not found",
                exception.getMessage()
        );
    }

    @Test
    void findByTeamAndUser_shouldReturnMembership() {
        TeamMembership membership =
                createMembership(
                        membershipId,
                        teamId,
                        userId
                );

        when(
                membershipRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                userId
                        )
        ).thenReturn(Optional.of(membership));

        TeamMembership result =
                membershipService.findByTeamAndUser(
                        teamId,
                        userId
                );

        assertNotNull(result);
        assertEquals(
                membershipId,
                result.getId()
        );
    }

    @Test
    void findByTeamAndUser_shouldRejectUnknownMembership() {
        when(
                membershipRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                userId
                        )
        ).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> membershipService
                                .findByTeamAndUser(
                                        teamId,
                                        userId
                                )
                );

        assertEquals(
                "Team membership not found",
                exception.getMessage()
        );
    }

    @Test
    void addMember_shouldCreateMembership() {
        when(teamRepository.existsById(teamId))
                .thenReturn(true);

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(
                membershipRepository
                        .existsByTeamIdAndUserId(
                                teamId,
                                userId
                        )
        ).thenReturn(false);

        when(membershipRepository.save(any(
                TeamMembership.class
        ))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        TeamMembership result =
                membershipService.addMember(
                        teamId,
                        userId
                );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(
                teamId,
                result.getTeamId()
        );
        assertEquals(
                userId,
                result.getUserId()
        );
        assertNotNull(result.getJoinedAt());

        verify(membershipRepository)
                .save(any(TeamMembership.class));
    }

    @Test
    void addMember_shouldRejectUnknownTeam() {
        when(teamRepository.existsById(teamId))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> membershipService
                                .addMember(
                                        teamId,
                                        userId
                                )
                );

        assertEquals(
                "Team not found",
                exception.getMessage()
        );

        verify(userRepository, never())
                .existsById(userId);

        verify(membershipRepository, never())
                .save(any(TeamMembership.class));
    }

    @Test
    void addMember_shouldRejectUnknownUser() {
        when(teamRepository.existsById(teamId))
                .thenReturn(true);

        when(userRepository.existsById(userId))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> membershipService
                                .addMember(
                                        teamId,
                                        userId
                                )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(membershipRepository, never())
                .save(any(TeamMembership.class));
    }

    @Test
    void addMember_shouldRejectDuplicateMembership() {
        when(teamRepository.existsById(teamId))
                .thenReturn(true);

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(
                membershipRepository
                        .existsByTeamIdAndUserId(
                                teamId,
                                userId
                        )
        ).thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> membershipService
                                .addMember(
                                        teamId,
                                        userId
                                )
                );

        assertEquals(
                "User is already a member of this team",
                exception.getMessage()
        );

        verify(membershipRepository, never())
                .save(any(TeamMembership.class));
    }

    @Test
    void removeMember_shouldDeleteMembership() {
        TeamMembership membership =
                createMembership(
                        membershipId,
                        teamId,
                        userId
                );

        when(
                membershipRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                userId
                        )
        ).thenReturn(Optional.of(membership));

        membershipService.removeMember(
                teamId,
                userId
        );

        verify(membershipRepository)
                .delete(membership);
    }

    @Test
    void removeMember_shouldRejectUnknownMembership() {
        when(
                membershipRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                userId
                        )
        ).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> membershipService
                                .removeMember(
                                        teamId,
                                        userId
                                )
                );

        assertEquals(
                "Team membership not found",
                exception.getMessage()
        );

        verify(membershipRepository, never())
                .delete(any(TeamMembership.class));
    }

    private TeamMembership createMembership(
            UUID membershipId,
            UUID teamId,
            UUID userId
    ) {
        return new TeamMembership(
                membershipId,
                teamId,
                userId,
                Instant.now()
        );
    }
}
