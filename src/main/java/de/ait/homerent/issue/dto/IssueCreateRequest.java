package de.ait.homerent.issue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Data
public class IssueCreateRequest {
    @NotNull(message = "Booking ID must not be null")
    private Long bookingId;
    
    @NotBlank(message = "Description must not be empty")
    private String description;
}