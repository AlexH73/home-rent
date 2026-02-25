package de.ait.homerent.booking;

import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.it.AbstractIT;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingOverlapIT extends AbstractIT {

    @Autowired MockMvc mockMvc;
    @Autowired PropertyRepository propertyRepository;

    @Test
    @DisplayName("Tenant cannot create overlapping booking for the same property (REQUESTED blocks)")
    void overlappingBookingRejected() throws Exception {
        Long propertyId = propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AVAILABLE properties in test data"))
                .getId();

        // First booking: 2026-04-01 .. 2026-04-03
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

        // Overlapping booking: 2026-04-02 .. 2026-04-05 (overlaps)
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
                .andExpect(status().isBadRequest());
    }
}
