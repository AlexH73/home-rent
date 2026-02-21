package de.ait.homerent.user.controller;

import de.ait.homerent.user.dto.ErrorResponseDto;
import de.ait.homerent.user.dto.UpdateRolesRequest;
import de.ait.homerent.user.dto.UpdateUserStatusRequest;
import de.ait.homerent.user.dto.UserCreateRequest;
import de.ait.homerent.user.dto.UserDto;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Administrative endpoints for managing users")

public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve all users", description = "Returns a list of all registered users with their details and roles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role", content = @Content())
    })
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Admin requested all users list");
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user", description = "Creates a new user account with default ROLE_TENANT. Username and email must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content())
    })
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody
            @Parameter(description = "User details", required = true)
            UserCreateRequest request) {
        log.info("Admin creating new user: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(request));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign roles to user", description = "Replaces all existing roles of a user with the provided list. Role names must be valid (ROLE_TENANT, ROLE_OWNER, ROLE_OPERATOR, ROLE_ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles updated successfully", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid role name or empty list", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User or role not found", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<UserDto> assignRoles(
            @Parameter(description = "User ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "List of role names", required = true) UpdateRolesRequest request) {
        log.info("Admin changing roles for user id {} to {}", id, request.getRoles());
        UserDto updatedUser = userService.updateRoles(id, request.getRoles());
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user enabled status", description = "Allows admin to enable or disable a user account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role", content = @Content()),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<UserDto> updateUserStatus(
            @Parameter(description = "User ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("Admin updating enabled status for user id {} to {}", id, request.getEnabled());
        UserDto updatedUser = userService.updateEnabledStatus(id, request.getEnabled());
        return ResponseEntity.ok(updatedUser);
    }
}
