package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.service.BookingService;
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
public class OwnerBookingController {
    private final BookingService bookingService;

    @GetMapping("/bookings/pending")
    public List<Booking> getPendingBookings(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching pending bookings for owner: {}", username);
        return bookingService.getPendingBookings(username);
    }

    @PostMapping("/bookings/{id}/approve")
    public Booking approveBooking(Authentication authentication,
                                  @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Approving booking {} for owner: {}", id, username);
        return bookingService.approveBooking(username, id);
    }

    @PostMapping("/bookings/{id}/reject")
    public Booking rejectBooking(Authentication authentication,
                                 @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Rejecting booking {} for owner: {}", id, username);
        return bookingService.rejectBooking(username, id);
    }
}