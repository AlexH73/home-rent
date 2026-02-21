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
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Owner Bookings", description = "Endpoints for property owners to manage their listings and bookings")
public class OwnerBookingController {

    private final BookingService bookingService;

    @GetMapping("/bookings/pending")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get bookings in REQUESTED status",
            description = "Fetch all bookings for properties owned by the current owner that are pending confirmation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of pending bookings",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires OWNER role",
                    content = @Content())
    })
    public List<BookingResponse> getPendingBookings(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching pending bookings for owner: {}", username);
        return bookingService.getPendingBookings(username);
    }

    @PostMapping("/bookings/{id}/approve")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Approve booking",
            description = "Approve a booking request by booking ID for the current owner's property.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking approved successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Booking not in REQUESTED state or already processed",
                    content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – not the owner of the property or missing OWNER role",
                    content = @Content()),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public BookingResponse approveBooking(
            Authentication authentication,
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Approving booking {} for owner: {}", id, username);
        return bookingService.approveBooking(username, id);
    }

    @PostMapping("/bookings/{id}/reject")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Reject booking",
            description = "Reject a booking request by booking ID for the current owner's property.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking rejected successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Booking not in REQUESTED state or already processed",
                    content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – not the owner of the property or missing OWNER role",
                    content = @Content()),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public BookingResponse rejectBooking(
            Authentication authentication,
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Rejecting booking {} for owner: {}", id, username);
        return bookingService.rejectBooking(username, id);
    }
}