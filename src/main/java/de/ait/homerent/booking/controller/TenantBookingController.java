package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/tenant/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT')")
@Tag(name = "Tenant Booking Controller", description = "Operations related to bookings for tenants")
public class TenantBookingController {
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create booking", description = "Allows a tenant to create a new booking request for a property")
    public BookingResponse createBooking(@RequestBody BookingCreateRequest request, Principal principal) {
        return bookingService.createBooking(request, principal.getName());
    }

    @GetMapping("/my")
    @Operation(summary = "Get my bookings", description = "Returns a list of bookings for the currently authenticated tenant")
    public List<BookingResponse> getMyBookings(Principal principal) {
        return bookingService.getMyBookings(principal.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Returns details of a specific booking by its ID")
    public BookingResponse getBooking(@PathVariable Long id, Principal principal) {
        return bookingService.getBookingForTenant(id, principal.getName());
    }

    @PostMapping("/{id}/upload-contract")
    @Operation(summary = "Upload contract", description = "Allows a tenant to upload a signed rental contract for a specific booking")
    public void uploadContract(@PathVariable Long id, @RequestParam("file") MultipartFile file, Principal principal) {
        bookingService.uploadContract(id, file, principal.getName());
    }
}
