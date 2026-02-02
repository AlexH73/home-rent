package de.ait.homerent.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
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
@Tag(
        name = "Public API Endpoints",
        description = """
        ### Endpoints available without authentication
        
        These API methods provide general service information and are accessible to all users.
        No authentication is required for these endpoints.
        """
)
public class PublicController {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.name:HomeRent}")
    private String appName;

    @Value("${app.description:Home rental service system}")
    private String appDescription;

    @GetMapping("/info")
    @Operation(
            summary = "Get service information",
            description = """
            ### Returns basic information about the HomeRent platform
            
            Use this endpoint to:
            - Check service availability
            - Get API version information
            - Verify service functionality before integration
            
            **Note:** This endpoint does not require authentication.
            """,
            tags = {"Public API Endpoints"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success response example",
                                    value = """
                        {
                          "service": "HomeRent",
                          "version": "1.0.0",
                          "description": "Home rental service system",
                          "status": "active"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error response example",
                                    value = """
                        {
                          "timestamp": "2024-01-15T10:30:00.000Z",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "An unexpected error occurred",
                          "path": "/api/public/info"
                        }
                        """
                            )
                    )
            )
    })
    public Map<String, String> getServiceInfo() {
        return Map.of(
                "service", appName,
                "version", appVersion,
                "description", appDescription,
                "status", "active",
                "timestamp", Instant.now().toString()
        );
    }
}
