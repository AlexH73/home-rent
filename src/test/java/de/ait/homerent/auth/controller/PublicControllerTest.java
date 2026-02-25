package de.ait.homerent.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.security.TestSecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 24.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@WebMvcTest(PublicController.class)
@Import(TestSecurityConfig.class)
@DisplayName("PublicController Unit Tests")
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/public/info - should return service information with status 200")
    void getServiceInfo_ShouldReturnServiceInfo() throws Exception {
        mockMvc.perform(get("/api/public/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
