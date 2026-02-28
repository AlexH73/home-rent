package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.it.AbstractIT;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ----------------------------------------------------------------------------
 * Author : Dmitri Nedioglo
 * Created : 27.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantBookingIT extends AbstractIT {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private PropertyRepository propertyRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private BookingRepository bookingRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private Long propertyId;
        private final String TENANT_USERNAME = "tenant_test";
        private final String TENANT_PASSWORD = "password123";

        @BeforeEach
        void setUp() {
                bookingRepository.deleteAll();
                propertyRepository.deleteAll();
                userRepository.deleteAll();

                Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT)
                                .orElseGet(() -> roleRepository
                                                .save(Role.builder().name(RoleName.ROLE_TENANT).build()));
                Role ownerRole = roleRepository.findByName(RoleName.ROLE_OWNER)
                                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_OWNER).build()));

                User owner = new User();
                owner.setUsername("owner_test");
                owner.setEmail("owner@test.com");
                owner.setPassword(passwordEncoder.encode("password"));
                owner.setEnabled(true);
                owner.setRoles(Set.of(ownerRole));
                User savedOwner = userRepository.save(owner);

                User tenant = new User();
                tenant.setUsername(TENANT_USERNAME);
                tenant.setEmail("tenant@test.com");
                tenant.setPassword(passwordEncoder.encode(TENANT_PASSWORD));
                tenant.setEnabled(true);
                tenant.setRoles(Set.of(tenantRole));
                userRepository.save(tenant);

                Property property = new Property();
                property.setTitle("Integration Test Property");
                property.setAddress("Test Address 123");
                property.setDescription("Long enough description for validation purposes.");
                property.setPricePerDay(100);
                property.setStatus(PropertyStatus.AVAILABLE);
                property.setOwner(savedOwner);
                property.setAvailableFrom(LocalDateTime.now().minusDays(10));
                property.setAvailableTo(LocalDateTime.now().plusMonths(6));

                Property savedProperty = propertyRepository.save(property);
                this.propertyId = savedProperty.getId();
        }

        @Test
        @DisplayName("Create Booking: Success as Tenant")
        void createBooking_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
                String jsonRequest = """
                                {
                                  "propertyId": %d,
                                  "startDate": "2026-03-01",
                                  "endDate": "2026-03-05"
                                }
                                """.formatted(propertyId);

                mockMvc.perform(post("/api/tenant/bookings")
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("REQUESTED"))
                                .andExpect(jsonPath("$.totalPrice").value(500))
                                .andExpect(jsonPath("$.propertyTitle").value("Integration Test Property"));
        }

        @Test
        @DisplayName("Create Booking: Forbidden for User without ROLE_TENANT")
        void createBooking_ShouldReturnForbidden_WhenUserIsOwner() throws Exception {
                String jsonRequest = """
                                {
                                  "propertyId": %d,
                                  "startDate": "2026-03-01",
                                  "endDate": "2026-03-05"
                                }
                                """.formatted(propertyId);

                mockMvc.perform(post("/api/tenant/bookings")
                                .with(httpBasic("owner_test", "password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Create Booking: Conflict when overlapping")
        void createBooking_ShouldReturnBadRequest_WhenOverlappingExists() throws Exception {
                // First booking
                String json1 = """
                                {
                                  "propertyId": %d,
                                  "startDate": "2026-03-10",
                                  "endDate": "2026-03-15"
                                }
                                """.formatted(propertyId);

                mockMvc.perform(post("/api/tenant/bookings")
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json1))
                                .andExpect(status().isCreated());

                // IMPORTANT: In the new logic, REQUESTED bookings do not block each other.
                // We must APPROVE the first one to test blocking.
                // We can use a simple way to find it since we cleared DB
                Booking booking = bookingRepository.findAll().get(0);
                booking.setStatus(BookingStatus.APPROVED);
                bookingRepository.save(booking);

                // Second overlapping booking
                String json2 = """
                                {
                                  "propertyId": %d,
                                  "startDate": "2026-03-12",
                                  "endDate": "2026-03-17"
                                }
                                """.formatted(propertyId);

                mockMvc.perform(post("/api/tenant/bookings")
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json2))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Get My Bookings: Success")
        void getMyBookings_ShouldReturnList() throws Exception {
                createBooking_ShouldReturnCreated_WhenRequestIsValid();

                mockMvc.perform(get("/api/tenant/bookings/my")
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$[0].tenantName").value(TENANT_USERNAME));
        }

        @Test
        @DisplayName("Get Booking By ID: Success")
        void getBookingById_ShouldReturnBooking() throws Exception {
                // Create booking manually or via API
                String jsonRequest = """
                                {
                                  "propertyId": %d,
                                  "startDate": "2026-03-01",
                                  "endDate": "2026-03-05"
                                }
                                """.formatted(propertyId);

                mockMvc.perform(post("/api/tenant/bookings")
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                                .andExpect(status().isCreated());

                // Extract ID from response (simplified for example, or just use repository)
                Booking booking = bookingRepository.findAll().get(0);
                Long bookingId = booking.getId();

                mockMvc.perform(get("/api/tenant/bookings/" + bookingId)
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(bookingId));
        }

        @Test
        @DisplayName("Get Booking By ID: Forbidden for another tenant")
        void getBookingById_ShouldReturnForbidden_WhenNotOwner() throws Exception {
                // Create a booking for primary tenant
                createBooking_ShouldReturnCreated_WhenRequestIsValid();
                Booking booking = bookingRepository.findAll().get(0);
                Long bookingId = booking.getId();

                // Create another tenant
                Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT).orElseThrow();
                User otherTenant = User.builder()
                                .username("other_tenant")
                                .email("other@test.com")
                                .password(passwordEncoder.encode("password"))
                                .enabled(true)
                                .roles(Set.of(tenantRole))
                                .build();
                userRepository.save(otherTenant);

                // Try to access as other tenant
                mockMvc.perform(get("/api/tenant/bookings/" + bookingId)
                                .with(httpBasic("other_tenant", "password")))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Upload Contract: Success when approved")
        void uploadContract_ShouldReturnOk_WhenApproved() throws Exception {
                createBooking_ShouldReturnCreated_WhenRequestIsValid();
                Booking booking = bookingRepository.findAll().get(0);
                booking.setStatus(BookingStatus.APPROVED);
                bookingRepository.save(booking);

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "contract.pdf",
                                MediaType.APPLICATION_PDF_VALUE,
                                "test content".getBytes());

                mockMvc.perform(multipart("/api/tenant/bookings/" + booking.getId() + "/upload-contract")
                                .file(file)
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Upload Contract: Bad Request when only requested")
        void uploadContract_ShouldReturnBadRequest_WhenStatusIsRequested() throws Exception {
                createBooking_ShouldReturnCreated_WhenRequestIsValid();
                Booking booking = bookingRepository.findAll().get(0);

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "contract.pdf",
                                MediaType.APPLICATION_PDF_VALUE,
                                "test content".getBytes());

                mockMvc.perform(multipart("/api/tenant/bookings/" + booking.getId() + "/upload-contract")
                                .file(file)
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Create Booking: Bad Request when dates outside availability")
        void createBooking_ShouldReturnBadRequest_WhenOutsideAvailability() throws Exception {
                String jsonRequest = """
                                {
                                  "propertyId": %d,
                                  "startDate": "2027-01-01",
                                  "endDate": "2027-01-05"
                                }
                                """.formatted(propertyId);

                mockMvc.perform(post("/api/tenant/bookings")
                                .with(httpBasic(TENANT_USERNAME, TENANT_PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest))
                                .andExpect(status().isBadRequest());
        }
}
