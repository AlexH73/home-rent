package de.ait.homerent.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Public API")
public class PublicController {

    @GetMapping("/info")
    @Operation(summary = "Get service information", description = "Returns basic information about HomeRent service")
    public Map<String, String> getServiceInfo() {
        return Map.of(
                "service", "HomeRent",
                "version", "1.0.0",
                "description", "Home rental service system"
        );
    }
}
