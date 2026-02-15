package de.ait.homerent.property.controller;

import de.ait.homerent.property.dto.PropertyDto;
import de.ait.homerent.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "Tenant Property Management", description = "Operations related to properties for tenants")
public class TenantPropertyController {

    private final PropertyService propertyService;

    @GetMapping("/available")
    @PreAuthorize("hasRole('TENANT')")
    @Operation(summary = "Get available properties", description = "Returns a list of all properties currently available for rent")
    public List<PropertyDto> getAvailableProperties() {
        return propertyService.findAvailable();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT')")
    @Operation(summary = "Get property details", description = "Returns details of a specific property by its ID")
    public PropertyDto getPropertyById(@PathVariable Long id) {
        return propertyService.findById(id);
    }
}
