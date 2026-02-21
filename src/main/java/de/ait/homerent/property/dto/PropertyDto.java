package de.ait.homerent.property.dto;

import de.ait.homerent.property.model.PropertyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 09.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
@Schema(description = "Property data transfer object")
public class PropertyDto {

    @Schema(description = "Property ID", example = "1")
    private Long id;

    @Schema(description = "Owner's user ID", example = "5")
    private Long ownerId;

    @Schema(description = "Property title", example = "Cozy Apartment")
    private String title;

    @Schema(description = "Address", example = "123 Main St, New York")
    private String address;

    @Schema(description = "Description", example = "Spacious 2-bedroom apartment")
    private String description;

    @Schema(description = "Price per day", example = "150")
    private Integer pricePerDay;

    @Schema(description = "Current status", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BOOKED", "RENTED", "UNAVAILABLE"})
    private PropertyStatus status;

    @Schema(description = "Availability start date", example = "2026-03-01T00:00:00")
    private LocalDateTime availableFrom;

    @Schema(description = "Availability end date", example = "2026-12-31T00:00:00")
    private LocalDateTime availableTo;

    @Schema(description = "Creation timestamp", example = "2026-02-21T10:15:30")
    private LocalDateTime createdAt;
}
