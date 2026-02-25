package de.ait.homerent.user.service;

import de.ait.homerent.user.dto.UserCreateRequest;
import de.ait.homerent.user.dto.UserDto;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("save(): when username already exists, throws 409 CONFLICT")
    void save_whenUsernameExists_throws409() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("u1");
        req.setEmail("u1@test.com");
        req.setPassword("pw");

        when(userRepository.existsByUsername("u1")).thenReturn(true);

        assertThatThrownBy(() -> userService.save(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("save(): when email already exists, throws 409 CONFLICT")
    void save_whenEmailExists_throws409() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("u1");
        req.setEmail("u1@test.com");
        req.setPassword("pw");

        when(userRepository.existsByUsername("u1")).thenReturn(false);
        when(userRepository.existsByEmail("u1@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.save(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("save(): when default ROLE_TENANT is missing, throws 500 INTERNAL_SERVER_ERROR")
    void save_whenDefaultRoleMissing_throws500() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("u1");
        req.setEmail("u1@test.com");
        req.setPassword("pw");

        when(userRepository.existsByUsername("u1")).thenReturn(false);
        when(userRepository.existsByEmail("u1@test.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.save(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("save(): success encodes password, enables user, and assigns ROLE_TENANT")
    void save_success_encodesPassword_setsEnabled_setsRole() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("u1");
        req.setEmail("u1@test.com");
        req.setPassword("pw");

        Role tenantRole = new Role();
        tenantRole.setName(RoleName.ROLE_TENANT);

        when(userRepository.existsByUsername("u1")).thenReturn(false);
        when(userRepository.existsByEmail("u1@test.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_TENANT)).thenReturn(Optional.of(tenantRole));
        when(passwordEncoder.encode("pw")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDto dto = userService.save(req);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("u1");
        assertThat(dto.getEmail()).isEqualTo("u1@test.com");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.getRoles()).contains(RoleName.ROLE_TENANT.name());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPassword()).isEqualTo("ENC");
        assertThat(captor.getValue().getRoles()).isEqualTo(Set.of(tenantRole));
    }

    @Test
    @DisplayName("updateRoles(): when role name is invalid, throws 400 BAD_REQUEST")
    void updateRoles_whenInvalidRoleName_throws400() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateRoles(1L, List.of("not-a-role")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("updateEnabledStatus(): when user not found, throws 404 NOT_FOUND")
    void updateEnabledStatus_whenUserNotFound_throws404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateEnabledStatus(1L, false))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
