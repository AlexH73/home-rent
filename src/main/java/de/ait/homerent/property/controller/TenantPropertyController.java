package de.ait.homerent.property.controller;

import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.service.PropertyService;
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
public class TenantPropertyController {
    private final PropertyService propertyService;

    @GetMapping("/available")
    public List<Property> getAvailableProperties() {
        return propertyService.findAvailable();
    }

    @GetMapping("/{id}")
    public Property getProperty(@PathVariable Long id) {
        return propertyService.findById(id);
    }
}
