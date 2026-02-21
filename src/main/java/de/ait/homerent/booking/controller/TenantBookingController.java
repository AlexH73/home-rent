package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 13.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/tenant/bookings")
@RequiredArgsConstructor
@Tag(name = "Tenant Booking Management", description = "Operations related to bookings for tenants")
public class TenantBookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a booking", description = "Creates a new booking request for a property")
    public BookingResponse createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TENANT')")
    @Operation(summary = "Get my bookings", description = "Returns a list of bookings made by the current tenant")
    public List<BookingResponse> getMyBookings(Authentication authentication) {
        User user = getUser(authentication);
        return bookingService.findMyBookings(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT') and @bookingSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Get booking details", description = "Returns details of a specific booking owned by the current tenant")
    public BookingResponse getBookingById(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        return bookingService.getBookingById(id, user);
    }

    @PostMapping("/{id}/upload-contract")
    @PreAuthorize("hasRole('TENANT') and @bookingSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Upload contract", description = "Allows the tenant to upload a signed rental contract for a specific booking")
    public void uploadContract(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                               Authentication authentication) {
        User user = getUser(authentication);
        bookingService.uploadContract(id, file, user);
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
