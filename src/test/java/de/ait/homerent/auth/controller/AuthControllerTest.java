package de.ait.homerent.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.auth.dto.AuthResponse;
import de.ait.homerent.auth.dto.LoginRequest;
import de.ait.homerent.auth.dto.RegisterRequest;
import de.ait.homerent.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.security.TestSecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test-security")
class AuthControllerTest {


    private static final Logger log = LoggerFactory.getLogger(AuthControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // REGISTER TESTS


    @Test
    void register_success() throws Exception {
        log.info("Running test: register_success");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("secret123");

        AuthResponse response = new AuthResponse();
        response.setMessage("Registration successful");
        response.setUsername("john");
        response.setEmail("john@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        log.info("Test register_success completed");
    }

    @Test
    void register_validationError() throws Exception {
        log.info("Running test: register_validationError");

        RegisterRequest request = new RegisterRequest();
        request.setUsername(""); // invalid
        request.setEmail("invalid-email");
        request.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        log.info("Test register_validationError completed");
    }

    // LOGIN TESTS


    @Test
    void login_success() throws Exception {
        log.info("Running test: login_success");

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("secret123");

        AuthResponse response = new AuthResponse();
        response.setMessage("Login successful");
        response.setUsername("john");
        response.setEmail("john@example.com");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        log.info("Test login_success completed");
    }

    @Test
    void login_validationError() throws Exception {
        log.info("Running test: login_validationError");

        LoginRequest request = new LoginRequest();
        request.setUsername(""); // invalid
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        log.info("Test login_validationError completed");
    }
}

