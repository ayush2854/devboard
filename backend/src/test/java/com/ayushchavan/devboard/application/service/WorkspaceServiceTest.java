package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ayushchavan.devboard.domain.entity.Workspace;
import com.ayushchavan.devboard.domain.repository.WorkspaceRepository;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    void findById_shouldReturnWorkspaceWhenWorkspaceExists() {
        UUID workspaceId = UUID.randomUUID();

        Workspace workspace = new Workspace(
                workspaceId,
                "DevBoard",
                "Project management workspace",
                Instant.now(),
                Instant.now()
        );

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        Optional<Workspace> result =
                workspaceService.findById(workspaceId);

        assertTrue(result.isPresent());
        assertEquals(workspaceId, result.get().getId());
        assertEquals("DevBoard", result.get().getName());
        assertEquals(
                "Project management workspace",
                result.get().getDescription()
        );

        verify(workspaceRepository).findById(workspaceId);
    }

    @Test
    void findById_shouldReturnEmptyWhenWorkspaceDoesNotExist() {
        UUID workspaceId = UUID.randomUUID();

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.empty());

        Optional<Workspace> result =
                workspaceService.findById(workspaceId);

        assertTrue(result.isEmpty());

        verify(workspaceRepository).findById(workspaceId);
    }

    @Test
    void createWorkspace_shouldCreateAndSaveWorkspace() {
        String name = "DevBoard";
        String description = "Project management workspace";

        when(workspaceRepository.save(any(Workspace.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workspace result = workspaceService.createWorkspace(
                name,
                description
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void updateWorkspace_shouldUpdateAndSaveWorkspace() {
        UUID workspaceId = UUID.randomUUID();

        Workspace workspace = new Workspace(
                workspaceId,
                "Old Name",
                "Old description",
                Instant.now(),
                Instant.now()
        );

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        when(workspaceRepository.save(any(Workspace.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workspace result = workspaceService.updateWorkspace(
                workspaceId,
                "New Name",
                "New description"
        );

        assertEquals("New Name", result.getName());
        assertEquals("New description", result.getDescription());

        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void updateWorkspace_shouldRejectWhenWorkspaceDoesNotExist() {
        UUID workspaceId = UUID.randomUUID();

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workspaceService.updateWorkspace(
                        workspaceId,
                        "New Name",
                        "New description"
                )
        );

        assertEquals(
                "Workspace not found",
                exception.getMessage()
        );

        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository, never())
                .save(any(Workspace.class));
    }

    @Test
    void deleteWorkspace_shouldDeleteWorkspace() {
        UUID workspaceId = UUID.randomUUID();

        Workspace workspace = new Workspace(
                workspaceId,
                "DevBoard",
                "Project management workspace",
                Instant.now(),
                Instant.now()
        );

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        workspaceService.deleteWorkspace(workspaceId);

        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository).delete(workspace);
    }

    @Test
    void deleteWorkspace_shouldRejectWhenWorkspaceDoesNotExist() {
        UUID workspaceId = UUID.randomUUID();

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workspaceService.deleteWorkspace(workspaceId)
        );

        assertEquals(
                "Workspace not found",
                exception.getMessage()
        );

        verify(workspaceRepository).findById(workspaceId);
        verify(workspaceRepository, never())
                .delete(any(Workspace.class));
    }
}