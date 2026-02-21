package de.ait.homerent.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.auth.dto.AuthResponse;
import de.ait.homerent.auth.dto.LoginRequest;
import de.ait.homerent.auth.dto.RegisterRequest;
import de.ait.homerent.user.model.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 15.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration tests for AuthController")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String registerUrl = "/api/auth/register";
    private final String loginUrl = "/api/auth/login";

    @Test
    @DisplayName("POST /api/auth/register – should register new tenant")
    void register_ValidRequest_ReturnsCreated() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newtenant")
                .email("newtenant@example.com")
                .password("password123")
                .build();

        String responseBody = mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.username").value("newtenant"))
                .andExpect(jsonPath("$.email").value("newtenant@example.com"))
                .andExpect(jsonPath("$.roles[0].name").value(RoleName.ROLE_TENANT.name()))
                .andReturn().getResponse().getContentAsString();

        AuthResponse response = objectMapper.readValue(responseBody, AuthResponse.class);
        assertThat(response.getRoles()).hasSize(1);
        assertThat(response.getRoles().iterator().next().getName()).isEqualTo(RoleName.ROLE_TENANT);
    }

    @Test
    @DisplayName("POST /api/auth/register – should return 400 when username already exists")
    void register_DuplicateUsername_ReturnsBadRequest() throws Exception {
        // We assume that the user tenant1 is already in the test data.
        RegisterRequest request = RegisterRequest.builder()
                .username("tenant1")
                .email("new@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register – should return 400 when email already exists")
    void register_DuplicateEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("tenant1@example.com") // existing email address
                .password("password123")
                .build();

        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register – should return 400 when validation fails")
    void register_InvalidData_ReturnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("") // empty username
                .email("invalid-email")
                .password("123") // too short
                .build();

        mockMvc.perform(post(registerUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login – should authenticate and return 200")
    void login_ValidCredentials_ReturnsOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("tenant1")
                .password("tenant123")
                .build();

        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.username").value("tenant1"))
                .andExpect(jsonPath("$.email").value("tenant1@example.com"))
                .andExpect(jsonPath("$.roles[0].name").value(RoleName.ROLE_TENANT.name()));
    }

    @Test
    @DisplayName("POST /api/auth/login – should return 401 when password wrong")
    void login_WrongPassword_ReturnsUnauthorized() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("tenant1")
                .password("wrongpass")
                .build();

        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login – should return 401 when user not exists")
    void login_UserNotFound_ReturnsUnauthorized() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("anypass")
                .build();

        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login – should return 400 when validation fails")
    void login_InvalidData_ReturnsBadRequest() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("") // empty
                .password("")
                .build();

        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
