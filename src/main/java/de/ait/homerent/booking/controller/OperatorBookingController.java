package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Tag(name = "Operator Booking Management")
@RestController
@RequestMapping("/api/operator/bookings")
@RequiredArgsConstructor
public class OperatorBookingController {

    private static final Logger log = LoggerFactory.getLogger(OperatorBookingController.class);
    private final BookingService bookingService;

    @Operation(summary = "Get all active bookings")
    @GetMapping("/active")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<List<BookingResponse>> getActiveBookings() {
        log.info("Operator requested active bookings list");
        return ResponseEntity.ok(bookingService.getActiveBookings());
    }
}
