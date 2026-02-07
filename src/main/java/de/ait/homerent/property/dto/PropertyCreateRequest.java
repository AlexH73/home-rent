package de.ait.homerent.property.dto;

import de.ait.homerent.property.model.PropertyStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Data
public class PropertyCreateRequest {

    @NotNull(message = "Owner ID must not be null")
    private Long ownerId;

    @NotBlank(message = "Title must not be empty")
    private String title;

    @NotBlank(message = "Address must not be empty")
    private String address;

    @NotBlank(message = "Description must not be empty")
    private String description;

    @NotNull(message = "Price per day must not be null")
    @Min(value = 0, message = "Price per day must be at least 0")
    private Integer pricePerDay;

    @NotNull(message = "Status must not be null")
    private PropertyStatus status;

    @NotNull(message = "Available from date must not be null")
    private LocalDateTime availableFrom;

    @NotNull(message = "Available to date must not be null")
    private LocalDateTime availableTo;
}
