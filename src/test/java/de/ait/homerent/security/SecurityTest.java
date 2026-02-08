package de.ait.homerent.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    private static final Logger log = LoggerFactory.getLogger(SecurityTest.class);

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    void testSecureEndpoint() throws Exception {
        log.info("Calling /api/secure/data with authenticated user");
        mockMvc.perform(get("/api/secure/data"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Protected endpoints require authentication")
    void protectedEndpoints() throws Exception {
        log.info("Calling /api/secure/data without authentication");
        mockMvc.perform(get("/api/secure/data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void protectedEndpointsWithAuth() throws Exception {
        log.info("Calling /api/secure/data with authentication");
        mockMvc.perform(get("/api/secure/data"))
                .andExpect(status().isNotFound());
    }
}





