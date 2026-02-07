package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
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
public class TenantBookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponse createBooking(@RequestBody BookingCreateRequest request, Principal principal) {
        return bookingService.createBooking(request, principal.getName());
    }

    @GetMapping("/my")
    public List<BookingResponse> getMyBookings(Principal principal) {
        return bookingService.getMyBookings(principal.getName());
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(@PathVariable Long id, Principal principal) {
        return bookingService.getBookingForTenant(id, principal.getName());
    }

    @PostMapping("/{id}/upload-contract")
    public void uploadContract(@PathVariable Long id, @RequestParam("file") MultipartFile file, Principal principal) {
        bookingService.uploadContract(id, file, principal.getName());
    }
}
