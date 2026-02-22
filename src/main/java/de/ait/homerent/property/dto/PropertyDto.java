package de.ait.homerent.property.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.ait.homerent.property.model.PropertyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "ID of the property owner", example = "5")
    private Long ownerId;

    @Schema(description = "Property title", example = "Cozy Apartment in City Center")
    private String title;

    @Schema(description = "Full address", example = "123 Main St, New York, NY 10001")
    private String address;

    @Schema(description = "Detailed description", example = "Spacious 2-bedroom apartment with modern amenities...")
    private String description;

    @Schema(description = "Price per day in USD", example = "150")
    private Integer pricePerDay;

    @Schema(description = "Current status of the property", example = "AVAILABLE", allowableValues = {"AVAILABLE", "BOOKED", "RENTED", "UNAVAILABLE"})
    private PropertyStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Start of availability period", example = "2026-03-01 00:00:00")
    private LocalDateTime availableFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "End of availability period", example = "2026-12-31 23:59:59")
    private LocalDateTime availableTo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation timestamp", example = "2026-02-21 10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "List of photo URLs (read-only, populated in responses)", required = false)
    private List<String> photoUrls;
}
