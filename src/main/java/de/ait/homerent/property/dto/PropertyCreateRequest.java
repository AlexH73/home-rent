package de.ait.homerent.property.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Data
@Schema(description = "Request object for creating a new property")
public class PropertyCreateRequest {

    @NotNull(message = "Owner ID must not be null")
    @Schema(description = "ID of the property owner (must have ROLE_OWNER)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ownerId;

    @NotBlank(message = "Title must not be empty")
    @Schema(description = "Property title", example = "Cozy Apartment in City Center", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Address must not be empty")
    @Schema(description = "Full address of the property", example = "123 Main St, New York, NY 10001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotBlank(message = "Description must not be empty")
    @Schema(description = "Detailed description of the property", example = "Spacious 2-bedroom apartment with modern amenities...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "Price per day must not be null")
    @Min(value = 0, message = "Price per day must be at least 0")
    @Schema(description = "Price per day in USD", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pricePerDay;

    @NotNull(message = "Available from date must not be null")
    @Schema(description = "Start of availability period (format: yyyy-MM-ddTHH:mm:ss)", example = "2026-03-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date-time")
    private LocalDateTime availableFrom;

    @NotNull(message = "Available to date must not be null")
    @Schema(description = "End of availability period (format: yyyy-MM-ddTHH:mm:ss)", example = "2026-12-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date-time")
    private LocalDateTime availableTo;
}
