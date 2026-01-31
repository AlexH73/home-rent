package de.ait.homerent.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TenantBookingController.class)
@Import(SecurityConfig.class)
class TenantBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicInfoShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/public/info"))
                .andExpect(status().isOk());
    }

    @Test
    void tenantEndpointWithoutAuthShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tenant/bookings/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "tenant1", roles = {"TENANT"})
    void tenantCanAccessOwnBookings() throws Exception {
        mockMvc.perform(get("/api/tenant/bookings/my"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "owner1", roles = {"OWNER"})
    void ownerCannotAccessTenantEndpoint() throws Exception {
        mockMvc.perform(get("/api/tenant/bookings/my"))
                .andExpect(status().isForbidden());
    }
}

