package de.ait.homerent.user.service;

import de.ait.homerent.user.dto.UserCreateRequest;
import de.ait.homerent.user.dto.UserDto;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.repository.UserRepository;
import de.ait.homerent.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        log.info("Fetching all users from database");
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto save(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("New user created by admin: {}", savedUser.getEmail());
        return mapToDto(savedUser);
    }

    @Transactional
    public void updateRoles(Long id, List<String> roleNames) {
        log.info("Updating roles for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id));

        Set<Role> roles = roleNames.stream()
                .map(name -> {
                    try {
                        RoleName roleNameEnum = RoleName.valueOf(name.toUpperCase());
                        return roleRepository.findByName(roleNameEnum)
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Role not found in DB: " + name));
                    } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Invalid role name: " + name);
                    }
                })
                .collect(Collectors.toSet());

        user.setRoles(roles);
        log.info("Roles updated successfully for user: {}", user.getEmail());
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return dto;
    }
}