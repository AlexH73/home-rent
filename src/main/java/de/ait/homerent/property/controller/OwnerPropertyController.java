package de.ait.homerent.property.controller;

import de.ait.homerent.property.dto.PropertyCreateRequest;
import de.ait.homerent.property.dto.PropertyDto;
import de.ait.homerent.property.dto.ErrorResponseDto;
import de.ait.homerent.property.service.PropertyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
@Tag(name = "Owner Properties", description = "Endpoints for property owners to manage their listings")
public class OwnerPropertyController {

    private final PropertyService propertyService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') and @propertySecurity.isOwner(#id, authentication)")
    @Operation(summary = "Update property", description = "Updates property details if the authenticated user is the owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property updated successfully",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – not the owner or missing OWNER role",
                    content = @Content()),
            @ApiResponse(responseCode = "404", description = "Property not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public PropertyDto updateProperty(
            @Parameter(description = "Property ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody
            @Parameter(description = "Updated property details", required = true) PropertyDto propertyDto,
            Authentication authentication) {
        return propertyService.updateProperty(id, propertyDto);
    }

    @GetMapping("/properties")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get owner's properties", description = "Fetch all properties that belong to the currently authenticated owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of properties",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires OWNER role",
                    content = @Content())
    })
    public List<PropertyDto> getMyProperties(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching properties for owner: {}", username);
        return propertyService.getMyProperties(username);
    }

    @PostMapping(value = "/properties", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Create a new property", description = "Create a new property for the currently authenticated owner with optional photos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property created successfully",
                    content = @Content(schema = @Schema(implementation = PropertyDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires OWNER role",
                    content = @Content())
    })
    public PropertyDto createProperty(
            Authentication authentication,
            @Parameter(description = "Property details (JSON)", required = true,
                    content = @Content(mediaType = "application/json"))
            @Valid @RequestPart("property") PropertyCreateRequest request,
            @Parameter(description = "Optional photo files (images)")
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String username = authentication.getName();
        int filesCount = (files != null) ? files.size() : 0;
        log.info("Creating property for owner: {} with {} files", username, filesCount);
        return propertyService.createProperty(username, request, files);
    }

    @DeleteMapping("/properties/{id}")
    @PreAuthorize("hasRole('OWNER') and @propertySecurity.isOwner(#id, authentication)")
    @Operation(summary = "Delete a property", description = "Delete a property by ID for the currently authenticated owner")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Property deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden – not the owner or missing OWNER role",
                    content = @Content()),
            @ApiResponse(responseCode = "404", description = "Property not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Void> deleteProperty(Authentication authentication,
                                               @Parameter(description = "Property ID", example = "1", required = true)
                                               @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Deleting property {} for owner: {}", id, username);
        propertyService.deleteProperty(username, id);
        return ResponseEntity.noContent().build();
    }
}
