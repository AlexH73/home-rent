package de.ait.homerent.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 13.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
@Schema(description = "Request payload for sending contract uploaded email notification")
public class ContractUploadedEmailRequest {

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

    @NotBlank
    @Schema(description = "Name of the uploaded contract file", example = "rental_contract.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contractFileName;
}
