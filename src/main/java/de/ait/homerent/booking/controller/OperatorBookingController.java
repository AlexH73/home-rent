package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@Tag(name = "Operator Booking Management", description = "Endpoints for operators to manage bookings and issues")
@RestController
@RequestMapping("/api/operator/bookings")
@RequiredArgsConstructor
@Slf4j
public class OperatorBookingController {

    private final BookingService bookingService;

    @GetMapping("/active")
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Get all active bookings", description = "Returns a list of all bookings with status ACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of active bookings",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires OPERATOR role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<BookingResponse>> getActiveBookings() {
        log.info("Operator requested active bookings list");
        return ResponseEntity.ok(bookingService.getActiveBookings());
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(
            summary = "Activate booking",
            description = "Marks an APPROVED booking as ACTIVE after operator verification (e.g. contract uploaded)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking activated successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Booking not in APPROVED state or contract missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires OPERATOR role"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingResponse> activateBooking(
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        log.info("Operator activating booking id={}", id);
        return ResponseEntity.ok(bookingService.activateBooking(id));
    }
}
