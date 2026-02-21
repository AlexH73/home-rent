package de.ait.homerent.security;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 14.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Component("bookingSecurity")
@RequiredArgsConstructor
public class BookingSecurity {

    private final BookingRepository bookingRepository;

    /**
     * Verifies that the current user is the owner (tenant) of the reservation.
     */
    public boolean isOwner(Long bookingId, Authentication authentication) {
        log.debug("isOwner called with bookingId: {}, user: {}", bookingId, authentication != null ? authentication.getName() : null);

        if (bookingId == null) {
            log.warn("Booking id is null – cannot check ownership, returning false");
            return false;
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            log.warn("Booking not found with id: {}", bookingId);
            return false;
        }

        boolean isOwner = booking.getTenant().getUsername().equals(currentUsername);
        log.debug("Is owner: {} for bookingId: {}", isOwner, bookingId);
        return isOwner;
    }
}
