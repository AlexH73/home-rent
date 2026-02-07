package de.ait.homerent.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Data
public class BookingCreateRequest {
    @NotNull(message = "Property ID must not be null")
    private Long propertyId;
    
    @NotNull(message = "Start date must not be null")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date must not be null")
    private LocalDateTime endDate;
}