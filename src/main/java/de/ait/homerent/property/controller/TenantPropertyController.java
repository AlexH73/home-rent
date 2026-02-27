package de.ait.homerent.property.controller;

import de.ait.homerent.property.dto.PropertyDto;
import de.ait.homerent.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 13.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/tenant/properties")
@RequiredArgsConstructor
@Tag(name = "Tenant Property Management", description = "Operations for tenants to view available properties")
public class TenantPropertyController {

    private final PropertyService propertyService;

    @GetMapping("/available")
    @PreAuthorize("hasRole('TENANT')")
    @Operation(
            summary = "Get available properties",
            description = """
                    Returns properties available for booking.
                    
                    Optional query parameters allow filtering by date range.
                    
                    Rules:
                    - If no parameters provided → returns all available properties
                    - If startDate provided → returns properties available from that date
                    - If endDate provided → returns properties available until that date
                    - If both provided → returns properties available within the date range
                    
                    Date format: yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss
                    Example: 2026-05-01
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "List of available properties",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid date format"),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden – requires TENANT role")
    })
    public List<PropertyDto> getAvailableProperties(

            @Parameter(
                    description = "Start date filter (inclusive). Format: yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss",
                    example = "2026-05-01"
            )
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDateTime startDate,

            @Parameter(
                    description = "End date filter (inclusive). Format: yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss",
                    example = "2026-05-31"
            )
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDateTime endDate
    ) {

        return propertyService.findAvailable(startDate, endDate);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT')")
    @Operation(summary = "Get property details", description = "Returns details of a specific property by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property details",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires TENANT role"),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public PropertyDto getPropertyById(
            @Parameter(description = "Property ID", example = "1", required = true)
            @PathVariable Long id) {
        return propertyService.findById(id);
    }
}