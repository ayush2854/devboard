package com.ayushchavan.devboard.presentation.controller;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.WorkspaceService;
import com.ayushchavan.devboard.domain.entity.Workspace;

@WebMvcTest(WorkspaceController.class)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createWorkspace_shouldReturnCreatedWorkspace() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        Instant now = Instant.now();

        Workspace workspace = new Workspace(
                workspaceId,
                "DevBoard",
                "Project management workspace",
                now,
                now
        );

        when(workspaceService.createWorkspace(
                "DevBoard",
                "Project management workspace"
        )).thenReturn(workspace);

        mockMvc.perform(
        post("/api/workspaces")
                .with(user("test-user"))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "DevBoard",
                            "description": "Project management workspace"
                        }
                        """)
	)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(workspaceId.toString())))
        .andExpect(jsonPath("$.name", is("DevBoard")))
        .andExpect(jsonPath(
                "$.description",
                is("Project management workspace")
        ));
    }

    @Test
    void getWorkspace_shouldReturnWorkspace() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        Instant now = Instant.now();

        Workspace workspace = new Workspace(
                workspaceId,
                "DevBoard",
                "Project management workspace",
                now,
                now
        );

        when(workspaceService.findById(workspaceId))
                .thenReturn(java.util.Optional.of(workspace));

        mockMvc.perform(
                get("/api/workspaces/{workspaceId}", workspaceId)
		.with(user("test-user"))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(workspaceId.toString())))
        .andExpect(jsonPath("$.name", is("DevBoard")))
        .andExpect(jsonPath(
                "$.description",
                is("Project management workspace")
        ));
    }

    @Test
    void updateWorkspace_shouldReturnUpdatedWorkspace() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        Instant now = Instant.now();

        Workspace workspace = new Workspace(
                workspaceId,
                "Updated DevBoard",
                "Updated description",
                now,
                now
        );

        when(workspaceService.updateWorkspace(
                workspaceId,
                "Updated DevBoard",
                "Updated description"
        )).thenReturn(workspace);

        mockMvc.perform(
                put("/api/workspaces/{workspaceId}", workspaceId)
			.with(user("test-user"))
			.with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "Updated DevBoard",
                                    "description": "Updated description"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.name",
                is("Updated DevBoard")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Updated description")
        ));
    }

    @Test
    void deleteWorkspace_shouldReturnNoContent() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        doNothing().when(workspaceService)
                .deleteWorkspace(workspaceId);

        mockMvc.perform(
                delete("/api/workspaces/{workspaceId}", workspaceId)
		.with(user("test-user"))
		.with(csrf())
        )
        .andExpect(status().isNoContent());
    }
}