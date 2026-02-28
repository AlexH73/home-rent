package de.ait.homerent.booking.controller;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.service.BookingService;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Tenant Booking Management", description = "Operations for tenants to manage bookings")
public class TenantBookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a booking", description = "Creates a new booking request for a property. Tenant is automatically set from authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"propertyTitle\":\"Cozy Apartment\",\"tenantName\":\"john_doe\",\"startDate\":\"2026-03-01T00:00:00\",\"endDate\":\"2026-03-10T00:00:00\",\"status\":\"REQUESTED\",\"totalPrice\":1500}"))),
            @ApiResponse(responseCode = "400", description = "Invalid dates or property not available",
                    content = @Content(examples = @ExampleObject(value = "{\"timestamp\":\"2026-02-21T12:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Start date must be before end date\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires TENANT role"),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public BookingResponse createBooking(
            @Valid @RequestBody
            @Parameter(description = "Booking details (propertyId, startDate, endDate)", required = true)
            BookingCreateRequest request,
            Authentication authentication) {
        User tenant = getUser(authentication);
        return bookingService.createBooking(request, tenant);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TENANT')")
    @Operation(summary = "Get my bookings", description = "Returns a list of bookings made by the current tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of bookings",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires TENANT role")
    })
    public List<BookingResponse> getMyBookings(Authentication authentication) {
        User user = getUser(authentication);
        return bookingService.findMyBookings(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT') and @bookingSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Get booking details", description = "Returns details of a specific booking owned by the current tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking details",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – not the owner"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public BookingResponse getBookingById(
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        User user = getUser(authentication);
        return bookingService.getBookingById(id, user);
    }

    @PostMapping(
            value = "/{id}/upload-contract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('TENANT') and @bookingSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Upload contract", description = "Allows the tenant to upload a signed rental contract (PDF) for a specific booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contract uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file (size, type) or booking not in correct state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – not the owner"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<String> uploadContract(
            @Parameter(description = "Booking ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "PDF file of the signed contract", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        User user = getUser(authentication);
        bookingService.uploadContract(id, file, user);
        return ResponseEntity.ok("Contract uploaded successfully");
    }

    private User getUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
