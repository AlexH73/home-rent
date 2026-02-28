package de.ait.homerent.booking;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.mail.EmailService;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingAvailabilityIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    EmailService emailService;

    Long propertyId;

    @BeforeEach
    void setup() {

        bookingRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();

        Role ownerRole = roleRepository.findByName(RoleName.ROLE_OWNER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_OWNER).build()));
        Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_TENANT).build()));
        Role operatorRole = roleRepository.findByName(RoleName.ROLE_OPERATOR)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_OPERATOR).build()));

        User owner = User.builder()
                .username("owner1")
                .email("owner1@test.com")
                .password(passwordEncoder.encode("owner123"))
                .enabled(true)
                .roles(Set.of(ownerRole))
                .build();
        userRepository.save(owner);

        User tenant = User.builder()
                .username("tenant1")
                .email("tenant1@test.com")
                .password(passwordEncoder.encode("tenant123"))
                .enabled(true)
                .roles(Set.of(tenantRole))
                .build();
        userRepository.save(tenant);

        User operator = User.builder()
                .username("operator1")
                .email("operator1@test.com")
                .password(passwordEncoder.encode("operator123"))
                .enabled(true)
                .roles(Set.of(operatorRole))
                .build();
        userRepository.save(operator);

        // Создаём доступное свойство
        Property property = Property.builder()
                .title("Test Property")
                .address("Test Address")
                .description("Nice place")
                .pricePerDay(100)
                .status(PropertyStatus.AVAILABLE)
                .owner(owner)
                .availableFrom(LocalDate.of(2026, 1, 1).atStartOfDay())
                .availableTo(LocalDate.of(2026, 12, 31).atStartOfDay())
                .build();
        propertyRepository.save(property);
        propertyId = property.getId();
    }

    // =========================================================
    // REQUESTED does NOT block
    // =========================================================

    @Test
    @DisplayName("REQUESTED booking does NOT block another REQUESTED")
    void requestedDoesNotBlock() throws Exception {
        createBooking("2026-04-01", "2026-04-05")
                .andExpect(status().isCreated());
        createBooking("2026-04-02", "2026-04-06")
                .andExpect(status().isCreated());
    }

    // =========================================================
    // APPROVED blocks
    // =========================================================

    @Test
    @DisplayName("APPROVED booking blocks overlapping booking")
    void approvedBlocks() throws Exception {
        String responseJson = createBooking("2026-05-01", "2026-05-05")
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookingResponse bookingResponse = objectMapper.readValue(responseJson, BookingResponse.class);
        Long bookingId = bookingResponse.getId();

        approveBooking(bookingId);

        createBooking("2026-05-02", "2026-05-06")
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // ACTIVE blocks
    // =========================================================

    @Test
    @DisplayName("ACTIVE booking blocks overlapping booking")
    void activeBlocks() throws Exception {
        String responseJson = createBooking("2026-06-01", "2026-06-05")
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookingResponse bookingResponse = objectMapper.readValue(responseJson, BookingResponse.class);
        Long bookingId = bookingResponse.getId();

        approveBooking(bookingId);
        uploadContract(bookingId);
        activateBooking(bookingId);

        createBooking("2026-06-02", "2026-06-06")
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // REJECTED does NOT block
    // =========================================================

    @Test
    @DisplayName("REJECTED booking does NOT block")
    void rejectedDoesNotBlock() throws Exception {
        String responseJson = createBooking("2026-07-01", "2026-07-05")
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookingResponse bookingResponse = objectMapper.readValue(responseJson, BookingResponse.class);
        Long bookingId = bookingResponse.getId();

        rejectBooking(bookingId);

        createBooking("2026-07-02", "2026-07-06")
                .andExpect(status().isCreated());
    }

    // =========================================================
    // Display my bookings
    // =========================================================

    @Test
    @DisplayName("Display returns list of tenant's bookings")
    void getMyBookings_returnsListOfUserBookings() throws Exception {
        createBooking("2026-09-01", "2026-09-05")
                .andExpect(status().isCreated());
        createBooking("2026-09-10", "2026-09-15")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tenant/bookings/my")
                        .with(httpBasic("tenant1", "tenant123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].propertyTitle").value("Test Property"))
                .andExpect(jsonPath("$[1].propertyTitle").value("Test Property"));
    }

    // =========================================================
    // Helpers
    // =========================================================

    private ResultActions createBooking(String start, String end) throws Exception {
        String json = String.format("""
                {
                    "propertyId": %d,
                    "startDate": "%s",
                    "endDate": "%s"
                }
                """, propertyId, start, end);

        return mockMvc.perform(
                post("/api/tenant/bookings")
                        .with(httpBasic("tenant1", "tenant123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
    }

    private void uploadContract(Long id) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );
        mockMvc.perform(multipart("/api/tenant/bookings/{id}/upload-contract", id)
                        .file(file)
                        .with(httpBasic("tenant1", "tenant123")))
                .andExpect(status().isOk());
    }

    private void approveBooking(Long id) throws Exception {
        mockMvc.perform(
                post("/api/owner/bookings/" + id + "/approve")
                        .with(httpBasic("owner1", "owner123"))
        ).andExpect(status().isOk());
    }

    private void activateBooking(Long id) throws Exception {
        mockMvc.perform(
                post("/api/operator/bookings/" + id + "/activate")
                        .with(httpBasic("operator1", "operator123"))
        ).andExpect(status().isOk());
    }

    private void rejectBooking(Long id) throws Exception {
        mockMvc.perform(
                post("/api/owner/bookings/" + id + "/reject")
                        .with(httpBasic("owner1", "owner123"))
        ).andExpect(status().isOk());
    }
}