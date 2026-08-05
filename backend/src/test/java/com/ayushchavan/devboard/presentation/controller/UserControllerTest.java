package com.ayushchavan.devboard.presentation.controller;

import com.ayushchavan.devboard.application.service.UserService;
import com.ayushchavan.devboard.config.SecurityConfig;
import com.ayushchavan.devboard.domain.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
@Autowired
private MockMvc mockMvc;

@MockitoBean
private UserService userService;

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
            "Ayush Chavan",
            "ayush@example.com",
            "plain-password"
    )).thenReturn(user);

    mockMvc.perform(
            post("/api/users")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Ayush Chavan",
                                "email": "ayush@example.com",
                                "password": "plain-password"
                            }
                            """)
    )
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.id").value(userId.toString()))
    .andExpect(jsonPath("$.name").value("Ayush Chavan"))
    .andExpect(jsonPath("$.email").value("ayush@example.com"))
    .andExpect(jsonPath("$.createdAt").exists())
    .andExpect(jsonPath("$.updatedAt").exists())
    .andExpect(jsonPath("$.passwordHash").doesNotExist());
}
}
