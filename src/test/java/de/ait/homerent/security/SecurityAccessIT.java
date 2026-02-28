package de.ait.homerent.security;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
class SecurityAccessIT extends AbstractIT {

    @Autowired MockMvc mockMvc;
    @Autowired PropertyRepository propertyRepository;

    @Test
    @DisplayName("Tenant bookings create: 401 when not authenticated")
    void tenantCreateBooking_requiresAuth() throws Exception {
        Long propertyId = propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream().findFirst().orElseThrow().getId();

        String json = """
                {
                  "propertyId": %d,
                  "startDate": "2026-05-01",
                  "endDate": "2026-05-02"
                }
                """.formatted(propertyId);

        mockMvc.perform(post("/api/tenant/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Owner approve booking: forbidden for TENANT")
    void ownerApprove_forbiddenForTenant() throws Exception {
        mockMvc.perform(post("/api/owner/bookings/{id}/approve", 9999L)
                        .with(httpBasic("tenant1", "tenant123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operator activate booking: forbidden for OWNER")
    void operatorActivate_forbiddenForOwner() throws Exception {
        mockMvc.perform(post("/api/operator/bookings/{id}/activate", 9999L)
                        .with(httpBasic("owner1", "owner123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operator issues list: forbidden for TENANT")
    void operatorIssues_forbiddenForTenant() throws Exception {
        mockMvc.perform(get("/api/operator/issues")
                        .with(httpBasic("tenant1", "tenant123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tenant issues create: forbidden for OPERATOR")
    void tenantIssuesCreate_forbiddenForOperator() throws Exception {
        mockMvc.perform(multipart("/api/tenant/issues")
                        .with(httpBasic("operator1", "operator123"))
                        .param("bookingId", "1")
                        .param("description", "test"))
                .andExpect(status().isForbidden());
    }
}
