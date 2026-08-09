package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.domain.entity.Team;
import com.ayushchavan.devboard.domain.repository.TeamRepository;
import com.ayushchavan.devboard.domain.repository.WorkspaceRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final WorkspaceRepository workspaceRepository;

    public TeamService(
            TeamRepository teamRepository,
            WorkspaceRepository workspaceRepository
    ) {
        this.teamRepository = teamRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public List<Team> findAllByWorkspaceId(UUID workspaceId) {
        requireWorkspaceExists(workspaceId);

        return teamRepository.findAllByWorkspaceId(workspaceId);
    }

    public Team findById(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Team not found"
                        )
                );
    }

    public Team createTeam(
            UUID workspaceId,
            String name,
            String description
    ) {
        requireWorkspaceExists(workspaceId);
        requireUniqueTeamName(workspaceId, name);

        Instant now = Instant.now();

        Team team = new Team(
                UUID.randomUUID(),
                workspaceId,
                name,
                description,
                now,
                now
        );

        return teamRepository.save(team);
    }

    public Team updateTeam(
            UUID teamId,
            String name,
            String description
    ) {
        Team team = findById(teamId);

        if (!team.getName().equals(name)
                && teamRepository.existsByWorkspaceIdAndName(
                        team.getWorkspaceId(),
                        name
                )) {

            throw new IllegalArgumentException(
                    "Team name already exists in this workspace"
            );
        }

        team.update(name, description);

        return teamRepository.save(team);
    }

    public void deleteTeam(UUID teamId) {
        Team team = findById(teamId);

        teamRepository.delete(team);
    }

    private void requireWorkspaceExists(UUID workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new IllegalArgumentException(
                    "Workspace not found"
            );
        }
    }

    private void requireUniqueTeamName(
            UUID workspaceId,
            String name
    ) {
        if (teamRepository.existsByWorkspaceIdAndName(
                workspaceId,
                name
        )) {
            throw new IllegalArgumentException(
                    "Team name already exists in this workspace"
            );
        }
    }
}
