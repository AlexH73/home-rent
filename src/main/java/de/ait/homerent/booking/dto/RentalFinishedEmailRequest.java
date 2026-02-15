package de.ait.homerent.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 13.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */


@Getter
@Setter
public class RentalFinishedEmailRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;

    @NotBlank
    private String propertyAddress;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Positive
    @NotNull
    private Integer totalPrice;
}

