package de.ait.homerent.property.dto;

import de.ait.homerent.property.model.PropertyStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.ait.homerent.utils.LocalDateTimeDeserializer;
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

    @NotNull
    @Schema(description = "ID of the property owner (must have ROLE_OWNER)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ownerId;

    @NotBlank
    @Schema(description = "Property title", example = "Cozy Apartment in City Center", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank
    @Schema(description = "Full address of the property", example = "123 Main St, New York, NY 10001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotBlank
    @Schema(description = "Detailed description of the property", example = "Spacious 2-bedroom apartment with modern amenities...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull
    @Min(0)
    @Schema(description = "Price per day in USD", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pricePerDay;

    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "Start of availability period (format: yyyy-MM-dd)", example = "2026-03-01", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDateTime availableFrom;

    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "End of availability period (format: yyyy-MM-dd)", example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
    private LocalDateTime availableTo;

    @Schema(description = "List of photo URLs (optional field)", required = false)
    private List<String> photoUrls;
}
