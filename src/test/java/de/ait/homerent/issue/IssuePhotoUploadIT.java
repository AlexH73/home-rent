package de.ait.homerent.issue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    @DisplayName("Tenant can create issue with PNG photo; photoPath is stored (not 'no-photo')")
    void createIssue_withPhoto() throws Exception {
        Long propertyId = propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream().findFirst().orElseThrow().getId();

        String createBookingJson = """
                {
                  "propertyId": %d,
                  "startDate": "2026-06-01",
                  "endDate": "2026-06-02"
                }
                """.formatted(propertyId);

        String bookingResponse = mockMvc.perform(post("/api/tenant/bookings")
                        .with(httpBasic("tenant1", "tenant123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBookingJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long bookingId = objectMapper.readTree(bookingResponse).get("id").asLong();

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "issue.png",
                "image/png",
                "fake png bytes".getBytes()
        );

        String issueResponse = mockMvc.perform(multipart("/api/tenant/issues")
                        .file(photo)
                        .param("bookingId", String.valueOf(bookingId))
                        .with(httpBasic("tenant1", "tenant123"))
                        .param("description", "Broken chair"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photoPath").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String photoPath = objectMapper.readTree(issueResponse).get("photoPath").asText();
        assertThat(photoPath).isNotBlank();
        assertThat(photoPath).isNotEqualTo("no-photo");
    }
}
