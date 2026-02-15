package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import de.ait.homerent.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OperatorBookingController.class)
@Import({SecurityConfig.class, OperatorBookingControllerTest.TestUsers.class})
@EnableMethodSecurity
class OperatorBookingControllerTest {

    private static final Logger log = LoggerFactory.getLogger(OperatorBookingControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingService bookingService;

    @TestConfiguration
    static class TestUsers {

        @Bean
        public BookingService bookingService() {
            return Mockito.mock(BookingService.class);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("operator").password("pass").roles("OPERATOR").build(),
                    User.withUsername("tenant").password("pass").roles("TENANT").build()
            );
        }
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void getActiveBookings_success() throws Exception {
        log.info("Running test: getActiveBookings_success");

        BookingResponse response = new BookingResponse();
        response.setId(1L);
        response.setPropertyTitle("Test House");
        response.setTenantName("John");
        response.setStartDate(LocalDateTime.now());
        response.setEndDate(LocalDateTime.now().plusDays(2));
        response.setTotalPrice(200);

        when(bookingService.getActiveBookings()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/operator/bookings/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].propertyTitle").value("Test House"))
                .andExpect(jsonPath("$[0].tenantName").value("John"));

        log.info("Test completed: getActiveBookings_success");
    }

    @Test
    @WithMockUser(username = "tenant", roles = "TENANT")
    void getActiveBookings_forbiddenForWrongRole() throws Exception {
        log.info("Running test: getActiveBookings_forbiddenForWrongRole");

        mockMvc.perform(get("/api/operator/bookings/active"))
                .andExpect(status().isForbidden());

        log.info("Test completed: getActiveBookings_forbiddenForWrongRole");
    }

    @Test
    void getActiveBookings_unauthorized() throws Exception {
        log.info("Running test: getActiveBookings_unauthorized");

        mockMvc.perform(get("/api/operator/bookings/active"))
                .andExpect(status().isUnauthorized());

        log.info("Test completed: getActiveBookings_unauthorized");
    }
}







