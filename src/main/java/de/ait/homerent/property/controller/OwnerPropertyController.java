package de.ait.homerent.property.controller;

import de.ait.homerent.property.dto.PropertyCreateRequest;
import de.ait.homerent.property.dto.PropertyDto;
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
public class OwnerPropertyController {

    private final PropertyService propertyService;

    @GetMapping("/properties")
    public List<PropertyDto> getMyProperties(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching properties for owner: {}", username);
        return propertyService.getMyProperties(username);
    }

    @PostMapping(value = "/properties", consumes = {"multipart/form-data"})
    public PropertyDto createProperty(
            Authentication authentication,
            @Valid @RequestPart("property") PropertyCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        String username = authentication.getName();
        int filesCount = (files != null) ? files.size() : 0;
        log.info("Creating property for owner: {} with {} files", username, filesCount);

        return propertyService.createProperty(username, request, files);
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