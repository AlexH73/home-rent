package de.ait.homerent.auth.service;

import de.ait.homerent.auth.dto.AuthResponse;
import de.ait.homerent.auth.dto.LoginRequest;
import de.ait.homerent.auth.dto.RegisterRequest;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());

        // Check if user exists
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username {} already exists", request.getUsername());
            throw new RuntimeException("User with this username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email {} already exists", request.getEmail());
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        // Assign ROLE_TENANT ONLY (strictly according to requirements)
        Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT)
                .orElseThrow(() -> {
                    log.error("ROLE_TENANT role not found in database");
                    return new RuntimeException("ROLE_TENANT role not found");
                });
        user.setRoles(new HashSet<>());
        user.getRoles().add(tenantRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return AuthResponse.builder()
                .message("Registration successful")
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user: {}", request.getUsername());

        try {
            // Authenticate user using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            log.debug("Authentication successful for user: {}", request.getUsername());

            // Get user details from repository
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        log.error("User not found after successful authentication: {}", request.getUsername());
                        return new RuntimeException("User not found");
                    });

            // Extract role names from authorities
            Set<String> roleNames = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            log.info("Login successful for user: {} with roles: {}", request.getUsername(), roleNames);

            return AuthResponse.builder()
                    .message("Login successful")
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles())
                    .build();

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {}", request.getUsername(), e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }
}
