package de.ait.homerent.contract.dto;

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
public class ContractUploadedEmailRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String username;

    @NotBlank
    private String propertyAddress;

    @NotBlank
    private String contractFileName; // имя загруженного PDF или filePath
}
