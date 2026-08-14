package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.ProjectStatus;
import com.ayushchavan.devboard.domain.repository.ProjectRepository;
import com.ayushchavan.devboard.domain.repository.WorkspaceRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID workspaceId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    @Test
    void findAllByWorkspaceId_shouldReturnProjects() {

        Project project1 = createActiveProject(
                UUID.randomUUID(),
                "Project Alpha",
                "First project"
        );

        Project project2 = createActiveProject(
                UUID.randomUUID(),
                "Project Beta",
                "Second project"
        );

        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(true);

        when(projectRepository.findAllByWorkspaceId(workspaceId))
                .thenReturn(List.of(project1, project2));

        List<Project> result =
                projectService.findAllByWorkspaceId(workspaceId);

        assertEquals(2, result.size());
        assertEquals("Project Alpha", result.get(0).getName());
        assertEquals("Project Beta", result.get(1).getName());

        verify(workspaceRepository)
                .existsById(workspaceId);

        verify(projectRepository)
                .findAllByWorkspaceId(workspaceId);
    }

    @Test
    void findAllByWorkspaceId_shouldThrowWhenWorkspaceDoesNotExist() {

        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> projectService.findAllByWorkspaceId(
                                workspaceId
                        )
                );

        assertEquals(
                "Workspace not found",
                exception.getMessage()
        );

        verify(projectRepository, never())
                .findAllByWorkspaceId(workspaceId);
    }

    @Test
    void findById_shouldReturnProject() {

        Project project = createActiveProject(
                projectId,
                "Backend Project",
                "Backend project"
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        Project result =
                projectService.findById(projectId);

        assertNotNull(result);
        assertEquals(projectId, result.getId());
        assertEquals(
                "Backend Project",
                result.getName()
        );
    }

    @Test
    void findById_shouldThrowWhenProjectDoesNotExist() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> projectService.findById(projectId)
                );

        assertEquals(
                "Project not found",
                exception.getMessage()
        );
    }

    @Test
    void createProject_shouldCreateActiveProject() {

        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(true);

        when(projectRepository.existsByWorkspaceIdAndName(
                workspaceId,
                "Backend Project"
        )).thenReturn(false);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Project result =
                projectService.createProject(
                        workspaceId,
                        "Backend Project",
                        "Backend development project"
                );

        assertNotNull(result.getId());

        assertEquals(
                workspaceId,
                result.getWorkspaceId()
        );

        assertEquals(
                "Backend Project",
                result.getName()
        );

        assertEquals(
                "Backend development project",
                result.getDescription()
        );

        assertEquals(
                ProjectStatus.ACTIVE,
                result.getStatus()
        );

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertNull(result.getArchivedAt());

        verify(projectRepository)
                .save(any(Project.class));
    }

    @Test
    void createProject_shouldThrowWhenWorkspaceDoesNotExist() {

        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> projectService.createProject(
                                workspaceId,
                                "Backend Project",
                                "Description"
                        )
                );

        assertEquals(
                "Workspace not found",
                exception.getMessage()
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void createProject_shouldRejectDuplicateName() {

        when(workspaceRepository.existsById(workspaceId))
                .thenReturn(true);

        when(projectRepository.existsByWorkspaceIdAndName(
                workspaceId,
                "Backend Project"
        )).thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> projectService.createProject(
                                workspaceId,
                                "Backend Project",
                                "Description"
                        )
                );

        assertEquals(
                "Project name already exists in this workspace",
                exception.getMessage()
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void updateProject_shouldUpdateProject() {

        Project project = createActiveProject(
                projectId,
                "Old Name",
                "Old description"
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.existsByWorkspaceIdAndName(
                workspaceId,
                "New Name"
        )).thenReturn(false);

        when(projectRepository.save(project))
                .thenReturn(project);

        Project result =
                projectService.updateProject(
                        projectId,
                        "New Name",
                        "New description"
                );

        assertEquals(
                "New Name",
                result.getName()
        );

        assertEquals(
                "New description",
                result.getDescription()
        );

        verify(projectRepository)
                .save(project);
    }

    @Test
    void updateProject_shouldRejectDuplicateName() {

        Project project = createActiveProject(
                projectId,
                "Old Name",
                "Description"
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.existsByWorkspaceIdAndName(
                workspaceId,
                "Existing Name"
        )).thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> projectService.updateProject(
                                projectId,
                                "Existing Name",
                                "Description"
                        )
                );

        assertEquals(
                "Project name already exists in this workspace",
                exception.getMessage()
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void updateProject_shouldRejectArchivedProject() {

        Project project = createArchivedProject(
                projectId,
                "Archived Project",
                "Archived description"
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> projectService.updateProject(
                                projectId,
                                "Updated Name",
                                "Updated description"
                        )
                );

        assertEquals(
                "Archived project cannot be updated",
                exception.getMessage()
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void archiveProject_shouldArchiveActiveProject() {

        Project project = createActiveProject(
                projectId,
                "Backend Project",
                "Backend project"
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(project))
                .thenReturn(project);

        Project result =
                projectService.archiveProject(projectId);

        assertEquals(
                ProjectStatus.ARCHIVED,
                result.getStatus()
        );

        assertNotNull(result.getArchivedAt());
        assertNotNull(result.getUpdatedAt());

        verify(projectRepository)
                .save(project);
    }

    @Test
    void archiveProject_shouldRejectAlreadyArchivedProject() {

        Project project = createArchivedProject(
                projectId,
                "Archived Project",
                "Archived description"
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> projectService.archiveProject(projectId)
                );

        assertEquals(
                "Project is already archived",
                exception.getMessage()
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void deleteProject_shouldDeleteProject() {

        Project project = createActiveProject(
                projectId,
                "Backend Project",
                "Backend project"
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        projectService.deleteProject(projectId);

        verify(projectRepository)
                .delete(project);
    }

    @Test
    void deleteProject_shouldThrowWhenProjectDoesNotExist() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> projectService.deleteProject(projectId)
                );

        assertEquals(
                "Project not found",
                exception.getMessage()
        );

        verify(projectRepository, never())
                .delete(any(Project.class));
    }

    private Project createActiveProject(
            UUID id,
            String name,
            String description
    ) {
        Instant now = Instant.now();

        return new Project(
                id,
                workspaceId,
                name,
                description,
                ProjectStatus.ACTIVE,
                now,
                now,
                null
        );
    }

    private Project createArchivedProject(
            UUID id,
            String name,
            String description
    ) {
        Instant now = Instant.now();

        return new Project(
                id,
                workspaceId,
                name,
                description,
                ProjectStatus.ARCHIVED,
                now,
                now,
                now
        );
    }
}
