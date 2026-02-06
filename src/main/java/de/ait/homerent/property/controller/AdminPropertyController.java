package de.ait.homerent.property.controller;

import de.ait.homerent.property.dto.PropertyCreateRequest;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Admin Property Management", description = "Operations for managing property listings (Admin access only)")
public class AdminPropertyController {

    private final PropertyService propertyService;

    @Operation(summary = "Get all properties", description = "Retrieves a full list of all properties in the system")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Property>> getAllProperties() {
        log.info("Admin requested all properties list");
        return ResponseEntity.ok(propertyService.findAll());
    }

    @Operation(summary = "Create property", description = "Allows an administrator to manually create a new property listing")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Property> createProperty(@Valid @RequestBody PropertyCreateRequest request) {
        log.info("Admin creating new property: {}", request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.save(request));
    }

    @Operation(summary = "Delete property", description = "Permanently removes a property listing from the system by its ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        log.info("Admin deleting property id: {}", id);
        propertyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}