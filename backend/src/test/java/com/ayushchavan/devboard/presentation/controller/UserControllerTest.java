package com.ayushchavan.devboard.presentation.controller;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.UserService;
import com.ayushchavan.devboard.domain.entity.User;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {

        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        User user = new User(
                userId,
                "Ayush Chavan",
                "ayush@example.com",
                "hashed-password",
                now,
                now
        );

        when(userService.createUser(
                any(String.class),
                any(String.class),
                any(String.class)
        )).thenReturn(user);

        String requestBody = """
                {
                    "name": "Ayush Chavan",
                    "email": "ayush@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Ayush Chavan"))
                .andExpect(jsonPath("$.email").value("ayush@example.com"));
    }
}
