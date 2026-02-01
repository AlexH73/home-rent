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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User with this username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        // Assign ROLE_TENANT ONLY (строго по ТЗ)
        Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT)
                .orElseThrow(() -> new RuntimeException("ROLE_TENANT role not found"));
        user.setRoles(new HashSet<>());
        user.getRoles().add(tenantRole);

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .message("Registration successful")
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .message("Login successful")
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
