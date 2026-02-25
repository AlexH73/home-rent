package de.ait.homerent.auth.service;

import de.ait.homerent.auth.dto.AuthResponse;
import de.ait.homerent.auth.dto.LoginRequest;
import de.ait.homerent.auth.dto.RegisterRequest;
import de.ait.homerent.auth.dto.RoleDto;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import de.ait.homerent.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 24.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest { 

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;
    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role tenantRole;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("rawPassword")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("rawPassword")
                .build();

        tenantRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_TENANT)
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .roles(Set.of(tenantRole))
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user with ROLE_TENANT")
    void register_Success() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_TENANT)).thenReturn(Optional.of(tenantRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("Registration successful", response.getMessage());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(1, response.getRoles().size());
        RoleDto roleDto = response.getRoles().iterator().next();
        assertEquals(tenantRole.getId(), roleDto.getId());
        assertEquals(tenantRole.getName(), roleDto.getName());

        verify(userRepository).save(argThat(u ->
                u.getUsername().equals(registerRequest.getUsername()) &&
                        u.getEmail().equals(registerRequest.getEmail()) &&
                        u.getPassword().equals("encodedPassword") &&
                        u.isEnabled() &&
                        u.getRoles().contains(tenantRole)
        ));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void register_UsernameAlreadyExists_ThrowsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(registerRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("username already exists"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(registerRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("email already exists"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when ROLE_TENANT is not found in database")
    void register_RoleNotFound_ThrowsException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_TENANT)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));
        assertEquals("ROLE_TENANT role not found", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(1, response.getRoles().size());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw exception when login with invalid credentials")
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(loginRequest));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid username or password"));

        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("login(): happy path authenticates and returns AuthResponse")
    void login_happyPath_returnsAuthResponse() {
        LoginRequest req = new LoginRequest();
        req.setUsername("u1");
        req.setPassword("pw");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "u1",
                "pw",
                Set.of(new SimpleGrantedAuthority("ROLE_TENANT"))
        );

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

        Role tenantRole = new Role();
        tenantRole.setId(1L);
        tenantRole.setName(RoleName.ROLE_TENANT);

        User user = new User();
        user.setUsername("u1");
        user.setEmail("u1@test.com");
        user.setRoles(Set.of(tenantRole));

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));

        var resp = authService.login(req);

        assertThat(resp.getMessage()).isEqualTo("Login successful");
        assertThat(resp.getUsername()).isEqualTo("u1");
        assertThat(resp.getEmail()).isEqualTo("u1@test.com");
        assertThat(resp.getRoles()).isNotEmpty();
    }

    @Test
    @DisplayName("login(): executes service method (smoke test)")
    void login_smoke_executesServiceMethod() {
        LoginRequest req = LoginRequest.builder()
                .username("smoke")
                .password("pw")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "smoke", "pw", Set.of(new SimpleGrantedAuthority("ROLE_TENANT"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByUsername("smoke")).thenReturn(Optional.of(user));

        AuthResponse resp = authService.login(req);

        assertThat(resp.getUsername()).isEqualTo("testuser"); // from user field in setUp()
    }

    @Test
    @DisplayName("updateRoles(): when user not found, throws 404 NOT_FOUND")
    void updateRoles_whenUserNotFound_throws404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateRoles(1L, List.of("ROLE_OWNER")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("updateRoles(): when role enum is valid but role not found in DB, throws 404 NOT_FOUND")
    void updateRoles_whenRoleNotFoundInDb_throws404() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(roleRepository.findByName(RoleName.ROLE_OWNER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateRoles(1L, List.of("ROLE_OWNER")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("login(): when authentication succeeds but user not found, throws RuntimeException")
    void login_whenUserNotFoundAfterAuth_throws() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "rawPassword",
                Set.of(new SimpleGrantedAuthority("ROLE_TENANT"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("User not found", ex.getMessage());
    }
}