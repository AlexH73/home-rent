package de.ait.homerent.auth.controller;

import de.ait.homerent.auth.dto.AuthResponse;
import de.ait.homerent.auth.dto.LoginRequest;
import de.ait.homerent.auth.dto.RegisterRequest;
import de.ait.homerent.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication & Authorization",
        description = """
        ### User account management
        
        These endpoints allow:
        - Registering new users
        - Authenticating in the system
        - Getting information about the current user
        
        **Important:** During registration, users automatically receive the **ROLE_TENANT** role.
        All authentication endpoints are public and do not require authentication.
        """
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = """
            ### Create a new tenant account
            
            **Registration features:**
            - All new users register with **ROLE_TENANT** role
            - Passwords are stored encrypted (BCrypt)
            - Email must be unique
            - Username must be unique
            
            **Data requirements:**
            - Username: 3-50 characters
            - Email: valid email address
            - Password: minimum 6 characters
            
            **Note:** This endpoint is public and does not require authentication.
            """,
            tags = {"Authentication & Authorization"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Registration success example",
                                    value = """
                        {
                          "message": "Registration successful",
                          "username": "john_doe",
                          "email": "john@example.com",
                          "roles": [
                            {
                              "id": 1,
                              "name": "ROLE_TENANT"
                            }
                          ]
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or user already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation error example",
                                    value = """
                        {
                          "timestamp": "2024-01-15T10:30:00.000Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Username already exists",
                          "path": "/api/auth/register"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Server error example",
                                    value = """
                        {
                          "timestamp": "2024-01-15T10:30:00.000Z",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "Database connection failed",
                          "path": "/api/auth/register"
                        }
                        """
                            )
                    )
            )
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(
                    description = "Registration data",
                    required = true,
                    schema = @Schema(implementation = RegisterRequest.class)
            )
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = """
            ### User login
            
            **Authentication mechanism:**
            - Uses HTTP Basic Authentication
            - After successful authentication, protected endpoints can be accessed
            - For subsequent requests, use the Authorization header
            
            **How to use Basic Auth:**
            ```
            Authorization: Basic base64(username:password)
            ```
            
            **Curl example:**
            ```
            curl -u username:password http://localhost:8080/api/...
            ```
            
            **Note:** This endpoint is public and does not require pre-authentication.
            """,
            tags = {"Authentication & Authorization"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Login success example",
                                    value = """
                        {
                          "message": "Login successful",
                          "username": "john_doe",
                          "email": "john@example.com",
                          "roles": [
                            {
                              "id": 1,
                              "name": "ROLE_TENANT"
                            }
                          ]
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Authentication error example",
                                    value = """
                        {
                          "timestamp": "2024-01-15T10:30:00.000Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Bad credentials",
                          "path": "/api/auth/login"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Bad request example",
                                    value = """
                        {
                          "timestamp": "2024-01-15T10:30:00.000Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Username is required",
                          "path": "/api/auth/login"
                        }
                        """
                            )
                    )
            )
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(
                    description = "Login credentials",
                    required = true,
                    schema = @Schema(implementation = LoginRequest.class)
            )
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
