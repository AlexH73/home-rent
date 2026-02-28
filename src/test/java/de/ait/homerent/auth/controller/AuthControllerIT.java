package de.ait.homerent.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.auth.dto.AuthResponse;
import de.ait.homerent.auth.dto.LoginRequest;
import de.ait.homerent.auth.dto.RegisterRequest;
import de.ait.homerent.auth.dto.RoleDto;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.repository.RentalContractRepository;
import de.ait.homerent.issue.repository.IssueReportRepository;
import de.ait.homerent.mail.EmailService;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RentalContractRepository rentalContractRepository;

    @Autowired
    private IssueReportRepository issueReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        rentalContractRepository.deleteAll();
        issueReportRepository.deleteAll();
        bookingRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register – should register new user and return 201")
    void register_NewUser_ShouldReturn201AndCreateUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.roles[0].name").value(RoleName.ROLE_TENANT.name()))
                .andReturn();

        AuthResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        assertEquals("Registration successful", response.getMessage());

        User savedUser = userRepository.findByUsername("newuser").orElseThrow();
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertEquals(1, savedUser.getRoles().size());
        assertEquals(RoleName.ROLE_TENANT, savedUser.getRoles().iterator().next().getName());
    }

    @Test
    @DisplayName("POST /api/auth/register – should return 400 when username already exists")
    void register_ExistingUsername_ShouldReturn400() throws Exception {
        User user = User.builder()
                .username("existing")
                .email("existing@example.com")
                .password(passwordEncoder.encode("pass"))
                .enabled(true)
                .build();
        userRepository.save(user);

        RegisterRequest request = RegisterRequest.builder()
                .username("existing")
                .email("new@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register – should return 400 when email already exists")
    void register_ExistingEmail_ShouldReturn400() throws Exception {
        User user = User.builder()
                .username("user1")
                .email("duplicate@example.com")
                .password(passwordEncoder.encode("pass"))
                .enabled(true)
                .build();
        userRepository.save(user);

        RegisterRequest request = RegisterRequest.builder()
                .username("user2")
                .email("duplicate@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login – should return 200 with valid credentials")
    void login_ValidCredentials_ShouldReturn200() throws Exception {

        Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT)
                .orElseThrow();

        User user = User.builder()
                .username("validuser")
                .email("valid@example.com")
                .password(passwordEncoder.encode("correctPassword"))
                .enabled(true)
                .roles(Set.of(tenantRole))
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .username("validuser")
                .password("correctPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("validuser"))
                .andExpect(jsonPath("$.email").value("valid@example.com"))
                .andExpect(jsonPath("$.roles[0].name").value(RoleName.ROLE_TENANT.name()));
    }

    @Test
    @DisplayName("POST /api/auth/login – should return 401 with invalid password")
    void login_InvalidCredentials_ShouldReturn401() throws Exception {
        User user = User.builder()
                .username("validuser")
                .email("valid@example.com")
                .password(passwordEncoder.encode("correctPassword"))
                .enabled(true)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .username("validuser")
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login – should return 401 when user does not exist")
    void login_UserNotFound_ShouldReturn401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent")
                .password("any")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/tenant/properties/available – should return 401 without authentication")
    void accessProtectedEndpoint_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tenant/properties/available"))
                .andExpect(status().isUnauthorized());
    }
}