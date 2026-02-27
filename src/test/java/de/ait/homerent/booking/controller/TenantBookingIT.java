package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingResponse;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ----------------------------------------------------------------------------
 * Author : Dmitri Nedioglo
 * Created : 27.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TenantBookingIT {

        @Autowired
        private TestRestTemplate restTemplate;

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
        void createBooking_ShouldReturnCreated_WhenRequestIsValid() {
                Map<String, Object> request = new HashMap<>();
                request.put("propertyId", propertyId);
                request.put("startDate", "2026-03-01");
                request.put("endDate", "2026-03-05");

                ResponseEntity<BookingResponse> response = restTemplate
                                .withBasicAuth(TENANT_USERNAME, TENANT_PASSWORD)
                                .postForEntity("/api/tenant/bookings", request, BookingResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getStatus()).isEqualTo(BookingStatus.REQUESTED);
                assertThat(response.getBody().getTotalPrice()).isEqualTo(500); // 5 days inclusive
                assertThat(response.getBody().getPropertyTitle()).isEqualTo("Integration Test Property");
        }

        @Test
        @DisplayName("Create Booking: Forbidden for User without ROLE_TENANT")
        void createBooking_ShouldReturnForbidden_WhenUserIsOwner() {
                Map<String, Object> request = new HashMap<>();
                request.put("propertyId", propertyId);
                request.put("startDate", "2026-03-01");
                request.put("endDate", "2026-03-05");

                ResponseEntity<String> response = restTemplate
                                .withBasicAuth("owner_test", "password")
                                .postForEntity("/api/tenant/bookings", request, String.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Create Booking: Conflict when overlapping")
        void createBooking_ShouldReturnBadRequest_WhenOverlappingExists() {
                // First booking
                Map<String, Object> request1 = new HashMap<>();
                request1.put("propertyId", propertyId);
                request1.put("startDate", "2026-03-10");
                request1.put("endDate", "2026-03-15");

                ResponseEntity<BookingResponse> response1 = restTemplate.withBasicAuth(TENANT_USERNAME, TENANT_PASSWORD)
                                .postForEntity("/api/tenant/bookings", request1, BookingResponse.class);

                assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                // Second overlapping booking
                Map<String, Object> request2 = new HashMap<>();
                request2.put("propertyId", propertyId);
                request2.put("startDate", "2026-03-12");
                request2.put("endDate", "2026-03-17");

                ResponseEntity<String> response2 = restTemplate
                                .withBasicAuth(TENANT_USERNAME, TENANT_PASSWORD)
                                .postForEntity("/api/tenant/bookings", request2, String.class);

                assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response2.getBody()).contains("already booked");
        }
}
