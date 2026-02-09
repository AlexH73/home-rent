package de.ait.homerent.user.controller;

import de.ait.homerent.admin.dto.CreateUserRequest;
import de.ait.homerent.admin.dto.UpdateUserRoleRequest;
import de.ait.homerent.admin.dto.UpdateUserStatusRequest;
import de.ait.homerent.admin.dto.UserAdminResponse;
import de.ait.homerent.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Goette
 * Created : 10.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Admin User Management",
        description = """
        Administrative operations for managing user accounts.
        Only users with the ROLE_ADMIN role may access these endpoints.
        """
)
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Returns a list of all registered users.")
    @ApiResponse(responseCode = "200", description = "List of users returned successfully")
    public ResponseEntity<List<UserAdminResponse>> getAllUsers() {
        log.info("ADMIN requested all users");
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Returns a single user by ID.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserAdminResponse> getUserById(@PathVariable Long id) {
        log.info("ADMIN requested user with ID {}", id);
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user", description = "Admin can create a new user with any role.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    public ResponseEntity<UserAdminResponse> createUser(@RequestBody CreateUserRequest request) {
        log.info("ADMIN creating new user: {}", request.getUsername());
        return ResponseEntity.status(201).body(adminUserService.createUser(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user by ID.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("ADMIN deleting user with ID {}", id);
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable or disable user", description = "Changes the 'enabled' status of a user.")
    @ApiResponse(responseCode = "200", description = "User status updated")
    public ResponseEntity<UserAdminResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusRequest request
    ) {
        log.info("ADMIN updating status of user {} to {}", id, request.isEnabled());
        return ResponseEntity.ok(adminUserService.updateUserStatus(id, request.isEnabled()));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role", description = "Assigns a new role to a user.")
    @ApiResponse(responseCode = "200", description = "User role updated")
    public ResponseEntity<UserAdminResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleRequest request
    ) {
        log.info("ADMIN updating role of user {} to {}", id, request.getRole());
        return ResponseEntity.ok(adminUserService.updateUserRole(id, request.getRole()));
    }
}


