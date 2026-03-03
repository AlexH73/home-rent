package de.ait.homerent.issue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.it.AbstractIT;


import java.time.LocalDate;

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

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PropertyRepository propertyRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Issue flow: tenant creates issue -> operator sees it -> operator changes status to DONE")
    void issueFlow() throws Exception {
        // 0) prepare booking for tenant1
        Long propertyId = propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AVAILABLE properties in test data"))
                .getId();

        // get user tenant1
        var tenant = userRepository.findByUsername("tenant1")
                .orElseThrow(() -> new IllegalStateException("tenant1 not found"));

        Booking booking = new Booking();
        booking.setProperty(propertyRepository.findById(propertyId).orElseThrow());
        booking.setTenant(tenant);
        booking.setStatus(BookingStatus.APPROVED); // сразу одобряем для теста
        booking.setStartDate(LocalDate.of(2026, 3, 10).atTime(14, 0));
        booking.setEndDate(LocalDate.of(2026, 3, 12).atTime(11, 0));
        booking.setTotalPrice(15000000);
        booking = bookingRepository.save(booking);


        Long bookingId = booking.getId();

        // 1) tenant creates issue (NO photo)
        String description = "Water tap is leaking";

        IssueCreateRequest request = new IssueCreateRequest();
        request.setBookingId(bookingId);
        request.setDescription(description);

        String issueJson = objectMapper.writeValueAsString(request);

        MockMultipartFile issuePart = new MockMultipartFile(
                "issue",
                "issue.json",
                MediaType.APPLICATION_JSON_VALUE,
                issueJson.getBytes()
        );

        String issueCreateResult = mockMvc.perform(
                        multipart("/api/tenant/issues")
                                .file(issuePart)
                                .with(httpBasic("tenant1", "tenant123"))
                )
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
                .andExpect(jsonPath("$[?(@.id==" + issueId + ")]").isNotEmpty());

        // 3) operator updates status -> DONE
        mockMvc.perform(post("/api/operator/issues/{id}/status", issueId)
                        .with(httpBasic("operator1", "operator123"))
                        .param("status", IssueStatus.DONE.name()))
                .andExpect(status().isOk());

        // 4) operator sees updated status in list
        mockMvc.perform(get("/api/operator/issues")
                        .with(httpBasic("operator1", "operator123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + issueId + ")].status").value(org.hamcrest.Matchers.hasItem("DONE")));
    }

    @Test
    @DisplayName("Tenant cannot create issue if property is not AVAILABLE")
    void cannotCreateIssue_whenPropertyNotAvailable() throws Exception {
        // property preparation
        Property property = propertyRepository.findAll().stream().findFirst().orElseThrow();
        property.setStatus(PropertyStatus.BOOKED); // not AVAILABLE
        propertyRepository.save(property);

        var tenant = userRepository.findByUsername("tenant1").orElseThrow();

        // creating a booking
        Booking booking = new Booking();
        booking.setProperty(property);
        booking.setTenant(tenant);
        booking.setStatus(BookingStatus.APPROVED); // статус APPROVED
        booking.setStartDate(LocalDate.of(2026, 3, 10).atTime(14, 0));
        booking.setEndDate(LocalDate.of(2026, 3, 12).atTime(11, 0));
        booking.setTotalPrice(15000000);
        booking = bookingRepository.save(booking);

        // Create JSON for the issue.
        IssueCreateRequest request = new IssueCreateRequest();
        request.setBookingId(booking.getId());
        request.setDescription("Broken chair");
        String issueJson = objectMapper.writeValueAsString(request);

        MockMultipartFile issuePart = new MockMultipartFile(
                "issue",
                "issue.json",
                MediaType.APPLICATION_JSON_VALUE,
                issueJson.getBytes()
        );

        // sending a request and checking for BAD_REQUEST
        mockMvc.perform(
                        multipart("/api/tenant/issues")
                                .file(issuePart)
                                .with(httpBasic("tenant1", "tenant123"))
                )
                .andExpect(status().isBadRequest());
    }
}

