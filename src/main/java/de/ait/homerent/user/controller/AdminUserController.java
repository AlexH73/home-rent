package de.ait.homerent.user.controller;

import de.ait.homerent.user.dto.UserCreateRequest;
import de.ait.homerent.user.dto.UserDto;
import de.ait.homerent.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@Validated
@Tag(name = "Admin User Management", description = "Operations for managing users and roles (Admin access only)")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users with their associated roles")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Admin requested all users list");
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Create user", description = "Allows an administrator to manually create a new user")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Admin creating new user: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(request));
    }

    @Operation(summary = "Assign roles", description = "Updates the list of roles for a specific user by ID")
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRoles(
            @PathVariable Long id,
            @RequestBody @NotEmpty(message = "Role list cannot be empty") List<String> roles) {

        log.info("Admin changing roles for user id {} to {}", id, roles);
        userService.updateRoles(id, roles);
        return ResponseEntity.ok().build();
    }
}