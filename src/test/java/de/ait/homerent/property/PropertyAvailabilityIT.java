package de.ait.homerent.property;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@SpringBootTest
@AutoConfigureMockMvc
class PropertyAvailabilityIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    User owner;
    User tenant;

    Property property1;
    Property property2;
    Property property3;

    @BeforeEach
    void setup() {

        bookingRepository.deleteAll();
        propertyRepository.deleteAll();

        owner = userRepository.findByUsername("owner1").orElseThrow();
        tenant = userRepository.findByUsername("tenant1").orElseThrow();

        property1 = createProperty("Property 1");
        property2 = createProperty("Property 2");
        property3 = createProperty("Property 3");

        propertyRepository.saveAll(List.of(property1, property2, property3));
    }

    // =====================================================
    // NO BOOKINGS → all properties available
    // =====================================================

    @Test
    @DisplayName("Should return all properties when no bookings exist")
    void shouldReturnAllPropertiesWhenNoBookings() throws Exception {

        mockMvc.perform(
                        get("/api/tenant/properties/available")
                                .with(httpBasic("tenant1", "tenant123"))
                                .param("startDate", "2026-05-01")
                                .param("endDate", "2026-05-10")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // =====================================================
    // REQUESTED does NOT block
    // =====================================================

    @Test
    @DisplayName("REQUESTED booking should NOT block property")
    void requestedShouldNotBlock() throws Exception {

        createBooking(property1, BookingStatus.REQUESTED,
                "2026-05-01", "2026-05-10");

        mockMvc.perform(
                        get("/api/tenant/properties/available")
                                .with(httpBasic("tenant1", "tenant123"))
                                .param("startDate", "2026-05-02")
                                .param("endDate", "2026-05-05")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // =====================================================
    // APPROVED blocks
    // =====================================================

    @Test
    @DisplayName("APPROVED booking should block property")
    void approvedShouldBlock() throws Exception {

        createBooking(property1, BookingStatus.APPROVED,
                "2026-05-01", "2026-05-10");

        mockMvc.perform(
                        get("/api/tenant/properties/available")
                                .with(httpBasic("tenant1", "tenant123"))
                                .param("startDate", "2026-05-02")
                                .param("endDate", "2026-05-05")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // =====================================================
    // ACTIVE blocks
    // =====================================================

    @Test
    @DisplayName("ACTIVE booking should block property")
    void activeShouldBlock() throws Exception {

        createBooking(property1, BookingStatus.ACTIVE,
                "2026-05-01", "2026-05-10");

        mockMvc.perform(
                        get("/api/tenant/properties/available")
                                .with(httpBasic("tenant1", "tenant123"))
                                .param("startDate", "2026-05-02")
                                .param("endDate", "2026-05-05")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // =====================================================
    // FINISHED does NOT block
    // =====================================================

    @Test
    @DisplayName("FINISHED booking should NOT block property")
    void finishedShouldNotBlock() throws Exception {

        createBooking(property1, BookingStatus.FINISHED,
                "2026-05-01", "2026-05-10");

        mockMvc.perform(
                        get("/api/tenant/properties/available")
                                .with(httpBasic("tenant1", "tenant123"))
                                .param("startDate", "2026-05-02")
                                .param("endDate", "2026-05-05")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // =====================================================
    // OUTSIDE DATE RANGE → should be available
    // =====================================================

    @Test
    @DisplayName("Property should be available if booking outside requested range")
    void outsideRangeShouldBeAvailable() throws Exception {

        createBooking(property1, BookingStatus.APPROVED,
                "2026-06-01", "2026-06-10");

        mockMvc.perform(
                        get("/api/tenant/properties/available")
                                .with(httpBasic("tenant1", "tenant123"))
                                .param("startDate", "2026-05-01")
                                .param("endDate", "2026-05-10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // =====================================================
    // Helpers
    // =====================================================

    private Property createProperty(String title) {

        return Property.builder()
                .title(title)
                .address(title + " Address")
                .description(title + " Nice place")
                .pricePerDay(100)
                .status(PropertyStatus.AVAILABLE)
                .owner(owner)
                .availableFrom(LocalDateTime.now().plusDays(1))
                .availableTo(LocalDateTime.now().plusDays(30))
                .build();
    }

    private void createBooking(Property property,
                               BookingStatus status,
                               String start,
                               String end) {

        Booking booking = Booking.builder()
                .property(property)
                .tenant(tenant)
                .status(status)
                .startDate(LocalDateTime.parse(start + "T00:00:00"))
                .endDate(LocalDateTime.parse(end + "T23:59:59"))
                .totalPrice(1000)
                .build();

        bookingRepository.save(booking);
    }
}
