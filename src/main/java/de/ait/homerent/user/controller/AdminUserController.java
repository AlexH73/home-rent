package de.ait.homerent.user.controller;

import de.ait.homerent.user.dto.UpdateRolesRequest;
import de.ait.homerent.user.dto.UserCreateRequest;
import de.ait.homerent.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(
        name = "Admin User Management",
        description = """
        Administrative endpoints for managing application users.
        
        Provides functionality to:
        • retrieve all registered users
        • manually create users
        • assign and update user roles
        
        Access is restricted to users with ADMIN role.
        """
)

public class AdminUserController {

    private final UserService userService;

    @Operation(
            summary = "Retrieve all users",
            description = """
        Returns a list of all registered users in the system.
        
        The response includes:
        • user identifier
        • username
        • email address
        • enabled/disabled status
        • assigned roles
        
        Only accessible to administrators.
        """
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserCreateRequest>> getAllUsers() {
        log.info("Admin requested all users list");
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(
            summary = "Create a new user",
            description = """
        Allows an administrator to manually create a new user account.
        
        The user will be:
        • created with provided username, email, and password
        • password will be securely encoded
        • account will be enabled by default
        
        Roles may be assigned later using the role assignment endpoint.
        """
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserCreateRequest> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Admin creating new user: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(request));
    }

    @Operation(
            summary = "Assign roles to user",
            description = """
        Updates the set of roles assigned to a specific user.
        
        • Replaces all existing roles with the provided list
        • Role names must match existing system roles
        • Invalid role names will result in a validation error
        
        Example roles:
        ROLE_TENANT, ROLE_OWNER, ROLE_OPERATOR, ROLE_ADMIN
        
        Accessible only to administrators.
        """
    )
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolesRequest request
    ) {

        log.info("Admin changing roles for user id {} to {}", id, request.getRoles());
        userService.updateRoles(id, request.getRoles());
        return ResponseEntity.ok().build();
    }
}