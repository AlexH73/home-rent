package de.ait.homerent.auth;

import de.ait.homerent.auth.controller.AuthController;
import de.ait.homerent.auth.dto.AuthResponse;
import de.ait.homerent.auth.dto.LoginRequest;
import de.ait.homerent.auth.dto.RegisterRequest;
import de.ait.homerent.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AuthControllerTest.class);

    private MockMvc mockMvc;
    private AuthService authService;


    // Lokaler ExceptionHandler NUR für diesen Test

    @RestControllerAdvice
    static class TestExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(TestExceptionHandler.class);

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
            log.error("Validation failed: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Validation failed"));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
            log.error("IllegalArgumentException: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
            log.error("IllegalStateException: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @BeforeEach
    void setup() {
        log.info("Setting up MockMvc and mocking AuthService…");

        authService = Mockito.mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .alwaysDo(result -> {
                    log.info("Response Status: {}", result.getResponse().getStatus());
                    log.info("Response Body: {}", result.getResponse().getContentAsString());
                })
                .build();
    }

    // REGISTER TESTS

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("should register user and return 201")
        void register_shouldReturn201() throws Exception {

            log.info("Testing successful registration…");

            AuthResponse mockResponse = new AuthResponse(
                    "Registration successful",
                    "john_doe",
                    "john@example.com",
                    null
            );

            Mockito.when(authService.register(any(RegisterRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "john_doe",
                                      "email": "john@example.com",
                                      "password": "secret123"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Registration successful"));
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void register_shouldReturn400_onValidationError() throws Exception {

            log.info("Testing registration validation error…");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "",
                                      "email": "invalid",
                                      "password": ""
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @DisplayName("should return 400 when user already exists")
        void register_shouldReturn400_whenUserExists() throws Exception {

            log.info("Testing registration when user already exists…");

            Mockito.when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new IllegalArgumentException("Username already exists"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "john_doe",
                                      "email": "john@example.com",
                                      "password": "secret123"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Username already exists"));
        }
    }

    // LOGIN TESTS

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("should authenticate user and return 200")
        void login_shouldReturn200() throws Exception {

            log.info("Testing successful login…");

            AuthResponse mockResponse = new AuthResponse(
                    "Login successful",
                    "john_doe",
                    "john@example.com",
                    null
            );

            Mockito.when(authService.login(any(LoginRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "john_doe",
                                      "password": "secret123"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Login successful"));
        }

        @Test
        @DisplayName("should return 400 when login request is invalid")
        void login_shouldReturn400_onValidationError() throws Exception {

            log.info("Testing login validation error…");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "",
                                      "password": ""
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }

        @Test
        @DisplayName("should return 401 when credentials are invalid")
        void login_shouldReturn401_onBadCredentials() throws Exception {

            log.info("Testing login with bad credentials…");

            Mockito.when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new IllegalStateException("Bad credentials"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "john_doe",
                                      "password": "wrong"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Bad credentials"));
        }
    }
}





