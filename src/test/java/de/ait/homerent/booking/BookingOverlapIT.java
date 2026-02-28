package de.ait.homerent.booking;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import testsupport.it.AbstractIT;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingOverlapIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long propertyId;

    @BeforeEach
    void setUp() {
        Role ownerRole = roleRepository.findByName(RoleName.ROLE_OWNER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_OWNER).build()));
        Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_TENANT).build()));

        User owner = userRepository.findByUsername("owner1").orElseGet(() -> {
            User newOwner = User.builder()
                    .username("owner1")
                    .email("owner1@test.com")
                    .password(passwordEncoder.encode("pass"))
                    .enabled(true)
                    .roles(Set.of(ownerRole))
                    .build();
            return userRepository.save(newOwner);
        });

        userRepository.findByUsername("tenant1").orElseGet(() -> {
            User newTenant = User.builder()
                    .username("tenant1")
                    .email("tenant1@test.com")
                    .password(passwordEncoder.encode("tenant123"))
                    .roles(Set.of(tenantRole))
                    .enabled(true)
                    .build();
            return userRepository.save(newTenant);
        });

        Property property = Property.builder()
                .owner(owner)
                .title("Test House")
                .address("Test Address")
                .description("Test Description")
                .pricePerDay(100)
                .status(PropertyStatus.AVAILABLE)
                .availableFrom(LocalDateTime.now().minusDays(1))
                .availableTo(LocalDateTime.now().plusMonths(6))
                .build();
        property = propertyRepository.save(property);
        propertyId = property.getId();
    }

    @Test
    @DisplayName("Tenant CAN create overlapping REQUESTED bookings")
    void overlappingRequestedBookingAllowed() throws Exception {
        String firstJson = """
                {
                  "propertyId": %d,
                  "startDate": "2026-04-01",
                  "endDate": "2026-04-03"
                }
                """.formatted(propertyId);

        mockMvc.perform(post("/api/tenant/bookings")
                        .with(httpBasic("tenant1", "tenant123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstJson))
                .andExpect(status().isCreated());

        String secondJson = """
                {
                  "propertyId": %d,
                  "startDate": "2026-04-02",
                  "endDate": "2026-04-05"
                }
                """.formatted(propertyId);

        mockMvc.perform(post("/api/tenant/bookings")
                        .with(httpBasic("tenant1", "tenant123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondJson))
                .andExpect(status().isCreated());
    }
}