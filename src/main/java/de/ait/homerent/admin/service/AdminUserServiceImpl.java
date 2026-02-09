package de.ait.homerent.admin.service;

import de.ait.homerent.admin.dto.CreateUserRequest;
import de.ait.homerent.admin.dto.UserAdminResponse;
import de.ait.homerent.admin.mapper.AdminUserMapper;
import de.ait.homerent.exception.CompleteExceptions.*;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserMapper mapper;

    // ---------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------
    @Override
    public List<UserAdminResponse> getAllUsers() {
        log.info("ADMIN requested list of all users");
        return userRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------
    @Override
    public UserAdminResponse getUserById(Long id) {
        log.info("ADMIN requested user with ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException("User not found: " + id);
                });

        return mapper.toResponse(user);
    }

    // ---------------------------------------------------------
    // CREATE USER
    // ---------------------------------------------------------
    @Override
    public UserAdminResponse createUser(CreateUserRequest request) {

        log.info("ADMIN creating new user '{}'", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Cannot create user: username '{}' already exists", request.getUsername());
            throw new ConflictException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Cannot create user: email '{}' already exists", request.getEmail());
            throw new ConflictException("Email already exists");
        }

        // ROLE VALIDATION
        RoleName roleEnum;
        try {
            roleEnum = RoleName.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role name '{}'", request.getRole());
            throw new ValidationException("Invalid role name: " + request.getRole());
        }

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> {
                    log.warn("Role '{}' does not exist in database", roleEnum);
                    return new ValidationException("Role does not exist: " + request.getRole());
                });

        // PASSWORD VALIDATION
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            log.warn("Password too short for new user '{}'", request.getUsername());
            throw new ValidationException("Password must be at least 6 characters long");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(role))
                .build();

        userRepository.save(user);

        log.info("ADMIN successfully created user '{}'", user.getUsername());

        return mapper.toResponse(user);
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------
    @Override
    public void deleteUser(Long id) {
        log.info("ADMIN deleting user with ID {}", id);

        if (!userRepository.existsById(id)) {
            log.error("Cannot delete user: ID {} not found", id);
            throw new NotFoundException("User not found: " + id);
        }

        userRepository.deleteById(id);

        log.warn("ADMIN deleted user with ID {}", id);
    }

    // ---------------------------------------------------------
    // UPDATE USER STATUS
    // ---------------------------------------------------------
    @Override
    public UserAdminResponse updateUserStatus(Long id, boolean enabled) {
        log.info("ADMIN updating status of user {} to {}", id, enabled);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update status: user {} not found", id);
                    return new NotFoundException("User not found: " + id);
                });

        if (user.isEnabled() == enabled) {
            log.warn("User {} already has status '{}'", id, enabled);
            throw new ConflictException("User already has status: " + enabled);
        }

        user.setEnabled(enabled);
        userRepository.save(user);

        log.info("ADMIN successfully updated status of user {} to {}", id, enabled);

        return mapper.toResponse(user);
    }

    // ---------------------------------------------------------
    // UPDATE USER ROLE
    // ---------------------------------------------------------
    @Override
    public UserAdminResponse updateUserRole(Long id, String roleName) {
        log.info("ADMIN updating role of user {} to '{}'", id, roleName);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update role: user {} not found", id);
                    return new NotFoundException("User not found: " + id);
                });

        // ROLE VALIDATION
        RoleName roleEnum;
        try {
            roleEnum = RoleName.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role name '{}'", roleName);
            throw new ValidationException("Invalid role name: " + roleName);
        }

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> {
                    log.warn("Role '{}' does not exist in database", roleEnum);
                    return new ValidationException("Role does not exist: " + roleName);
                });

        if (user.getRoles().contains(role)) {
            log.warn("User {} already has role '{}'", id, roleName);
            throw new ConflictException("User already has role: " + roleName);
        }

        user.setRoles(Set.of(role));
        userRepository.save(user);

        log.info("ADMIN successfully updated role of user {} to '{}'", id, roleName);

        return mapper.toResponse(user);
    }
}





