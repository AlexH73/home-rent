package de.ait.homerent.property.controller;

import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.service.PropertyService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class OwnerPropertyController {

    private final PropertyService propertyService;

    @GetMapping("/properties")
    public List<Property> getMyProperties(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching properties for owner: {}", username);
        return propertyService.getMyProperties(username);
    }

    @PostMapping("/properties")
    public Property createProperty(Authentication authentication,
                                   @Valid @RequestBody Property property) {
        String username = authentication.getName();
        log.info("Creating property for owner: {}", username);
        return propertyService.createProperty(username, property);
    }

    @DeleteMapping("/properties/{id}")
    public ResponseEntity<Void> deleteProperty(Authentication authentication,
                                               @PathVariable Long id) {
        String username = authentication.getName();
        log.info("Deleting property {} for owner: {}", id, username);
        propertyService.deleteProperty(username, id);
        return ResponseEntity.noContent().build();
    }
}