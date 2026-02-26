package de.ait.homerent.issue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.it.AbstractIT;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class IssueFlowIT extends AbstractIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired PropertyRepository propertyRepository;
    @Autowired BookingRepository bookingRepository;

    @Test
    @DisplayName("Issue flow: tenant creates issue -> operator sees it -> operator changes status to DONE")
    void issueFlow() throws Exception {
        // 0) prepare booking for tenant1 (create + approve)
        Long propertyId = propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AVAILABLE properties in test data"))
                .getId();

        String createBookingJson = """
                {
                  "propertyId": %d,
                  "startDate": "2026-03-10",
                  "endDate": "2026-03-12"
                }
                """.formatted(propertyId);

        String bookingResponse = mockMvc.perform(post("/api/tenant/bookings")
                        .with(httpBasic("tenant1", "tenant123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBookingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long bookingId = objectMapper.readTree(bookingResponse).get("id").asLong();

        mockMvc.perform(post("/api/owner/bookings/{id}/approve", bookingId)
                        .with(httpBasic("owner1", "owner123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);

        // 1) tenant creates issue (NO photo -> no FileStorageService usage)
        // TenantIssueController uses @ModelAttribute and MULTIPART, so we send multipart form fields
        String description = "Water tap is leaking";

        String issueCreateResult = mockMvc.perform(multipart("/api/tenant/issues")
                        .with(httpBasic("tenant1", "tenant123"))
                        .param("bookingId", String.valueOf(bookingId))
                        .param("description", description))
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.photoPath").value("no-photo"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long issueId = objectMapper.readTree(issueCreateResult).get("id").asLong();

        // 2) operator sees issue in list
        mockMvc.perform(get("/api/operator/issues")
                        .with(httpBasic("operator1", "operator123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.id==" + issueId + ")]").exists());

        // 3) operator updates status -> DONE
        mockMvc.perform(post("/api/operator/issues/{id}/status", issueId)
                        .with(httpBasic("operator1", "operator123"))
                        .param("status", IssueStatus.DONE.name()))
                .andExpect(status().isOk());

        // 4) operator sees updated status in list
        mockMvc.perform(get("/api/operator/issues")
                        .with(httpBasic("operator1", "operator123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.id==" + issueId + ")].status").value("DONE"));
    }
}
