package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 16.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/owner")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Owner Bookings", description = "Endpoints for managing bookings of properties owned by the current owner")
public class OwnerBookingController {
    private final BookingService bookingService;

    @GetMapping("/bookings/pending")
    @Operation(
            summary = "Get bookings in REQUESTED status",
            description = "Fetch all bookings for properties owned by the current owner that are pending confirmation"
    )
    public List<BookingResponse> getPendingBookings(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching pending bookings for owner: {}", username);
        return bookingService.getPendingBookings(username);
    }

    @PostMapping("/bookings/{id}/approve")
    @Operation(
            summary = "Approve booking",
            description = "Approve a booking request by booking ID for the current owner's property"
    )
    public BookingResponse approveBooking(Authentication authentication,
                                          @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Approving booking {} for owner: {}", id, username);
        return bookingService.approveBooking(username, id);
    }

    @PostMapping("/bookings/{id}/reject")
    @Operation(
            summary = "Reject booking",
            description = "Reject a booking request by booking ID for the current owner's property"
    )
    public BookingResponse rejectBooking(Authentication authentication,
                                         @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Rejecting booking {} for owner: {}", id, username);
        return bookingService.rejectBooking(username, id);
    }
}