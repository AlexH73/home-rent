package de.ait.homerent.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 15.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@SpringBootTest(properties = {
        "app.mail.from=test@example.com",
        "EMAIL_FROM_USERNAME=test@example.com"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration tests for PublicController")
class PublicControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/public/info returns 200 and service info")
    void getServiceInfo_ShouldReturnServiceInfo() throws Exception {
        mockMvc.perform(get("/api/public/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("HomeRent"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.description").value("Home rental service system"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
