package de.ait.homerent.issue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 13.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new issue report")
public class IssueCreateRequest {

    @NotNull
    @Schema(description = "ID of the booking related to the issue", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookingId;

    @NotBlank
    @Schema(description = "Description of the issue", example = "Leaky faucet in kitchen", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "Optional photo of the issue (multipart file)", type = "string", format = "binary")
    private MultipartFile photo;
}
