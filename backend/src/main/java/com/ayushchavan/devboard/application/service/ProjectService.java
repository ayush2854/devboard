package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.application.exception.ConflictException;
import com.ayushchavan.devboard.application.exception.ResourceNotFoundException;
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.ProjectStatus;
import com.ayushchavan.devboard.domain.repository.ProjectRepository;
import com.ayushchavan.devboard.domain.repository.WorkspaceRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            WorkspaceRepository workspaceRepository
    ) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public List<Project> findAllByWorkspaceId(UUID workspaceId) {
        requireWorkspaceExists(workspaceId);

        return projectRepository.findAllByWorkspaceId(workspaceId);
    }

    public Project findById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found"
                        )
                );
    }

    public Project createProject(
            UUID workspaceId,
            String name,
            String description
    ) {
        requireWorkspaceExists(workspaceId);
        requireUniqueProjectName(workspaceId, name);

        Instant now = Instant.now();

        Project project = new Project(
                UUID.randomUUID(),
                workspaceId,
                name,
                description,
                ProjectStatus.ACTIVE,
                now,
                now,
                null
        );

        return projectRepository.save(project);
    }

    public Project updateProject(
            UUID projectId,
            String name,
            String description
    ) {
        Project project = findById(projectId);

        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new IllegalArgumentException(
                    "Archived project cannot be updated"
            );
        }

        if (!project.getName().equals(name)
                && projectRepository.existsByWorkspaceIdAndName(
                        project.getWorkspaceId(),
                        name
                )) {

            throw new ConflictException(
                    "Project name already exists in this workspace"
            );
        }

        project.update(name, description);

        return projectRepository.save(project);
    }

    public Project archiveProject(UUID projectId) {
        Project project = findById(projectId);

        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new IllegalArgumentException(
                    "Project is already archived"
            );
        }

        project.archive();

        return projectRepository.save(project);
    }

    public void deleteProject(UUID projectId) {
        Project project = findById(projectId);

        projectRepository.delete(project);
    }

    private void requireWorkspaceExists(UUID workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException(
                    "Workspace not found"
            );
        }
    }

    private void requireUniqueProjectName(
            UUID workspaceId,
            String name
    ) {
        if (projectRepository.existsByWorkspaceIdAndName(
                workspaceId,
                name
        )) {
            throw new ConflictException(
                    "Project name already exists in this workspace"
            );
        }
    }
}