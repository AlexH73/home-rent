package de.ait.homerent.property.controller;

import de.ait.homerent.property.dto.PropertyDto;
import de.ait.homerent.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Owner Property Management",
        description = """
        Endpoints for property management by owners.
        """)
public class OwnerPropertyController {

    private final PropertyService propertyService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') and @propertySecurity.isOwner(#id, authentication)")
    @Operation(summary = "Update property", description = "Updates property if the authenticated user is the owner")
    public PropertyDto updateProperty(@PathVariable Long id, @RequestBody PropertyDto propertyDto,
                                      Authentication authentication) {
        return propertyService.updateProperty(id, propertyDto);
    }
}
