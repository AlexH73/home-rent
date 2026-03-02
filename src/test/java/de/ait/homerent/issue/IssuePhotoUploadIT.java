package de.ait.homerent.issue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.issue.dto.IssueCreateRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.it.AbstractIT;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
class IssuePhotoUploadIT extends AbstractIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PropertyRepository propertyRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("Tenant can create issue with PNG photo; photoPath is stored (not 'no-photo')")
    void createIssue_withPhoto() throws Exception {
        // create booking through the repository
        Long propertyId = propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream().findFirst().orElseThrow().getId();

        var tenant = userRepository.findByUsername("tenant1")
                .orElseThrow(() -> new IllegalStateException("tenant1 not found"));

        Booking booking = new Booking();
        booking.setProperty(propertyRepository.findById(propertyId).orElseThrow());
        booking.setTenant(tenant);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(LocalDate.of(2026, 3, 10).atTime(14, 0));
        booking.setEndDate(LocalDate.of(2026, 3, 12).atTime(11, 0));
        booking.setTotalPrice(15000000);
        booking = bookingRepository.save(booking);

        Long bookingId = booking.getId();

// 1) create JSON for issue
        IssueCreateRequest request = new IssueCreateRequest();
        request.setBookingId(bookingId);
        request.setDescription("Broken chair");
        String issueJson = objectMapper.writeValueAsString(request);

        // 2) multipart part for issue
        MockMultipartFile issuePart = new MockMultipartFile(
                "issue",
                "issue.json",
                MediaType.APPLICATION_JSON_VALUE,
                issueJson.getBytes()
        );

        // multipart photo section
        MockMultipartFile photoPart = new MockMultipartFile(
                "photo",
                "issue.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake png bytes".getBytes()
        );

        // 4) multipart photo section
        String issueResponse = mockMvc.perform(
                        multipart("/api/tenant/issues")
                                .file(issuePart)
                                .file(photoPart)
                                .with(httpBasic("tenant1", "tenant123"))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photoPath").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 5) checking the result
        String photoPath = objectMapper.readTree(issueResponse).get("photoPath").asText();
        assertThat(photoPath).isNotBlank();
        assertThat(photoPath).isNotEqualTo("no-photo");
    }
}
