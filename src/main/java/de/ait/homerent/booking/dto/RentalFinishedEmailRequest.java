package de.ait.homerent.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Request payload for sending rental finished notification email")
public class RentalFinishedEmailRequest {

    @NotBlank
    @Email
    @Schema(description = "Recipient email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "Username of the tenant", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank
    @Schema(description = "Address of the booked property", example = "123 Main St, New York", requiredMode = Schema.RequiredMode.REQUIRED)
    private String propertyAddress;

    @NotNull
    @Schema(description = "Start date of rental (format: yyyy-MM-dd)", example = "2026-03-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull
    @Schema(description = "End date of rental (format: yyyy-MM-dd)", example = "2026-03-10", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Positive
    @NotNull
    @Schema(description = "Total price for the rental period", example = "1500", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalPrice;
}