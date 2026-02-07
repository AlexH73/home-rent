package de.ait.homerent.property.controller;

import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/tenant/properties")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT')")
@Tag(name = "Tenant Property Controller", description = "Operations related to properties for tenants")
public class TenantPropertyController {
    private final PropertyService propertyService;

    @GetMapping("/available")
    @Operation(summary = "Get available properties", description = "Returns a list of properties with AVAILABLE status")
    public List<Property> getAvailableProperties() {
        return propertyService.findAvailable();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property by ID", description = "Returns details of a specific property by its ID")
    public Property getProperty(@PathVariable Long id) {
        return propertyService.findById(id);
    }
}
