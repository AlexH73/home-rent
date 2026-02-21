package de.ait.homerent.property.controller;

import de.ait.homerent.property.dto.PropertyCreateRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/properties")
@RequiredArgsConstructor
@Tag(name = "Admin Property Management", description = "Administrative endpoints for managing properties and users")
public class AdminPropertyController {

    private final PropertyService propertyService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all properties", description = "Retrieves a full list of all properties in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of properties",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role")
    })
    public ResponseEntity<List<PropertyDto>> getAllProperties() {
        log.info("Admin requested all properties list");
        return ResponseEntity.ok(propertyService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create property listing", description = "Creates a new property listing and assigns it to a specific owner.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Property created successfully",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data (dates, owner not found, etc.)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Owner not found")
    })
    public ResponseEntity<PropertyDto> createProperty(
            @Valid @RequestBody
            @Parameter(description = "Property details", required = true)
            PropertyCreateRequest request) {
        log.info("Admin creating new property: {}", request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.save(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete property", description = "Permanently removes a property listing from the system by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Property deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<Void> deleteProperty(
            @Parameter(description = "Property ID", example = "1", required = true)
            @PathVariable Long id) {
        log.info("Admin deleting property with id: {}", id);
        propertyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
