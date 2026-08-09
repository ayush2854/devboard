package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.ayushchavan.devboard.domain.entity.Team;
import com.ayushchavan.devboard.domain.repository.TeamRepository;
import com.ayushchavan.devboard.domain.repository.WorkspaceRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private TeamService teamService;

    private UUID workspaceId;
    private UUID teamId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        teamId = UUID.randomUUID();
    }

    @Test
    void findAllByWorkspaceId_shouldReturnTeams() {
        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Backend development team"
        );

        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(true);

        when(teamRepository.findAllByWorkspaceId(workspaceId))
                .thenReturn(List.of(team));

        List<Team> result =
                teamService.findAllByWorkspaceId(workspaceId);

        assertEquals(1, result.size());
        assertEquals(teamId, result.get(0).getId());
        assertEquals(
                "Backend Team",
                result.get(0).getName()
        );

        verify(workspaceRepository)
                .existsById(workspaceId);

        verify(teamRepository)
                .findAllByWorkspaceId(workspaceId);
    }

    @Test
    void findAllByWorkspaceId_shouldRejectUnknownWorkspace() {
        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> teamService.findAllByWorkspaceId(
                        workspaceId
                )
        );

        verify(teamRepository, never())
                .findAllByWorkspaceId(workspaceId);
    }

    @Test
    void findById_shouldReturnTeam() {
        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Backend development team"
        );

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));

        Team result = teamService.findById(teamId);

        assertEquals(teamId, result.getId());
        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals(
                "Backend Team",
                result.getName()
        );
    }

    @Test
    void findById_shouldThrowWhenTeamDoesNotExist() {
        when(teamRepository.findById(teamId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> teamService.findById(teamId)
        );
    }

    @Test
    void createTeam_shouldCreateTeam() {
        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(true);

        when(teamRepository.existsByWorkspaceIdAndName(
                workspaceId,
                "Backend Team"
        )).thenReturn(false);

        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Team result = teamService.createTeam(
                workspaceId,
                "Backend Team",
                "Backend development team"
        );

        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals(
                "Backend Team",
                result.getName()
        );
        assertEquals(
                "Backend development team",
                result.getDescription()
        );

        verify(workspaceRepository)
                .existsById(workspaceId);

        verify(teamRepository)
                .existsByWorkspaceIdAndName(
                        workspaceId,
                        "Backend Team"
                );

        verify(teamRepository)
                .save(any(Team.class));
    }

    @Test
    void createTeam_shouldRejectUnknownWorkspace() {
        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> teamService.createTeam(
                        workspaceId,
                        "Backend Team",
                        "Backend development team"
                )
        );

        verify(teamRepository, never())
                .save(any(Team.class));
    }

    @Test
    void createTeam_shouldRejectDuplicateName() {
        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(true);

        when(teamRepository.existsByWorkspaceIdAndName(
                workspaceId,
                "Backend Team"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> teamService.createTeam(
                        workspaceId,
                        "Backend Team",
                        "Backend development team"
                )
        );

        verify(teamRepository, never())
                .save(any(Team.class));
    }

    @Test
    void updateTeam_shouldUpdateTeam() {
        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Old description"
        );

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));

        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Team result = teamService.updateTeam(
                teamId,
                "Backend Engineering",
                "Updated description"
        );

        assertEquals(
                "Backend Engineering",
                result.getName()
        );
        assertEquals(
                "Updated description",
                result.getDescription()
        );

        verify(teamRepository)
                .findById(teamId);

        verify(teamRepository)
                .save(team);
    }

    @Test
    void updateTeam_shouldRejectDuplicateName() {
        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Old description"
        );

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));

        when(teamRepository.existsByWorkspaceIdAndName(
                workspaceId,
                "Frontend Team"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> teamService.updateTeam(
                        teamId,
                        "Frontend Team",
                        "Updated description"
                )
        );

        verify(teamRepository, never())
                .save(any(Team.class));
    }

    @Test
    void updateTeam_shouldAllowSameName() {
        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Old description"
        );

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));

        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Team result = teamService.updateTeam(
                teamId,
                "Backend Team",
                "Updated description"
        );

        assertEquals(
                "Backend Team",
                result.getName()
        );
        assertEquals(
                "Updated description",
                result.getDescription()
        );

        verify(teamRepository, never())
                .existsByWorkspaceIdAndName(
                        workspaceId,
                        "Backend Team"
                );

        verify(teamRepository)
                .save(team);
    }

    @Test
    void deleteTeam_shouldDeleteTeam() {
        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Backend development team"
        );

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));

        teamService.deleteTeam(teamId);

        verify(teamRepository)
                .findById(teamId);

        verify(teamRepository)
                .delete(team);
    }

    @Test
    void deleteTeam_shouldThrowWhenTeamDoesNotExist() {
        when(teamRepository.findById(teamId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> teamService.deleteTeam(teamId)
        );

        verify(teamRepository, never())
                .delete(any(Team.class));
    }

    private Team createTeam(
            UUID teamId,
            UUID workspaceId,
            String name,
            String description
    ) {
        Instant now = Instant.now();

        return new Team(
                teamId,
                workspaceId,
                name,
                description,
                now,
                now
        );
    }
}