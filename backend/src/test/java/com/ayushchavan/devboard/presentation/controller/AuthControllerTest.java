package com.ayushchavan.devboard.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ayushchavan.devboard.application.dto.auth.LoginResponse;
import com.ayushchavan.devboard.application.dto.user.UserResponse;
import com.ayushchavan.devboard.application.service.AuthService;
import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.domain.entity.User;

@WebMvcTest(
        controllers = AuthController.class
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void login_shouldReturnTokenAndUser_whenCredentialsAreValid()
            throws Exception {

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

        UserResponse userResponse = UserResponse.from(user);

        LoginResponse loginResponse = new LoginResponse(
                "test-jwt-token",
                userResponse
        );

        when(authService.login(any()))
                .thenReturn(loginResponse);

        String requestBody = """
                {
                    "email": "ayush@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
        post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
	)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("test-jwt-token"))
                .andExpect(jsonPath("$.user.id")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.user.name")
                        .value("Ayush Chavan"))
                .andExpect(jsonPath("$.user.email")
                        .value("ayush@example.com"));
    }
}
