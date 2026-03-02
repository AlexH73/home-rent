package de.ait.homerent.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.repository.RentalContractRepository;
import de.ait.homerent.mail.EmailService;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.it.AbstractIT;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
class BookingFlowIT extends AbstractIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    RentalContractRepository rentalContractRepository;
//
//    @BeforeEach
//    void setUp() {
//        // Clean up bookings and contracts before each test to ensure a clean slate
//        bookingRepository.deleteAll();
//        rentalContractRepository.deleteAll();
//    }

    @Test
    @DisplayName("Full booking lifecycle: tenant REQUESTED -> owner APPROVED -> tenant uploads contract -> operator activates -> operator sees ACTIVE")
    void fullLifecycle() throws Exception {
        // --- arrange: pick any AVAILABLE property from liquibase test data ---
        Long propertyId = propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AVAILABLE properties in test data"))
                .getId();

        // book a future period (keep in 2026 as your test data availability is 2026)
        BookingCreateRequest req = new BookingCreateRequest();
        req.setPropertyId(propertyId);
        req.setStartDate(LocalDateTime.of(2026, 3, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 3, 3, 0, 0));

        // --- 1) tenant creates booking (REQUESTED) ---
        String createJson = """
                {
                  "propertyId": %d,
                  "startDate": "2026-03-10",
                  "endDate": "2026-03-13"
                }
                """.formatted(propertyId);

        String createResponse = mockMvc.perform(post("/api/tenant/bookings")
                        .with(httpBasic("tenant1", "tenant123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Long bookingId = objectMapper.readTree(createResponse).get("id").asLong();

        // sanity: booking is in DB
        Booking bookingAfterCreate = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(bookingAfterCreate.getStatus()).isEqualTo(BookingStatus.REQUESTED);

        // --- 2) owner approves booking (APPROVED) ---
        mockMvc.perform(post("/api/owner/bookings/{id}/approve", bookingId)
                        .with(httpBasic("owner1", "owner123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Booking bookingAfterApprove = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(bookingAfterApprove.getStatus()).isEqualTo(BookingStatus.APPROVED);

        // --- 3) tenant uploads contract (PDF) ---
        MockMultipartFile pdf = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "dummy pdf bytes".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/tenant/bookings/{id}/upload-contract", bookingId)
                        .file(pdf)
                        .with(httpBasic("tenant1", "tenant123")))
                .andExpect(status().isOk());

        assertThat(rentalContractRepository.findByBookingId(bookingId)).isPresent();

        // --- 4) operator activates booking (ACTIVE) ---
        mockMvc.perform(post("/api/operator/bookings/{id}/activate", bookingId)
                        .with(httpBasic("operator1", "operator123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Booking bookingAfterActivate = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(bookingAfterActivate.getStatus()).isEqualTo(BookingStatus.ACTIVE);

        // --- 5) operator sees booking in active list ---
        mockMvc.perform(get("/api/operator/bookings/active")
                        .with(httpBasic("operator1", "operator123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].id").isArray())
                .andExpect(jsonPath("$.[?(@.id==" + bookingId + ")]").exists());
    }
}
