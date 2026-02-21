package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.security.TestSecurityConfig;
import java.time.LocalDateTime;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OperatorBookingController.class)
@Import(TestSecurityConfig.class)
class OperatorBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingService bookingService;

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void getActiveBookings_success() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setId(1L);
        response.setPropertyTitle("Test House");
        response.setTenantName("John");
        response.setStartDate(LocalDateTime.now());
        response.setEndDate(LocalDateTime.now().plusDays(2));
        response.setTotalPrice(200);

        Mockito.when(bookingService.getActiveBookings())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/operator/bookings/active"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "tenant", roles = "TENANT")
    void getActiveBookings_forbiddenForWrongRole() throws Exception {
        mockMvc.perform(get("/api/operator/bookings/active"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getActiveBookings_unauthorized() throws Exception {
        mockMvc.perform(get("/api/operator/bookings/active"))
                .andExpect(status().isUnauthorized());
    }
}

