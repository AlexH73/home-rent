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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 14.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/owner/properties")
@RequiredArgsConstructor
@Tag(name = "Owner Property Management", description = "Endpoints for property owners to manage their listings")
public class OwnerPropertyController {

    private final PropertyService propertyService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') and @propertySecurity.isOwner(#id, authentication)")
    @Operation(summary = "Update property", description = "Updates property details if the authenticated user is the owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property updated successfully",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – not the owner or missing OWNER role"),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public PropertyDto updateProperty(
            @Parameter(description = "Property ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody
            @Parameter(description = "Updated property details", required = true)
            PropertyDto propertyDto,
            Authentication authentication) {
        return propertyService.updateProperty(id, propertyDto);
    }
}