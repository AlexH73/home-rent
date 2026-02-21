package de.ait.homerent.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 21.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Data
@Schema(description = "Request to update user enabled status")
public class UpdateUserStatusRequest {

    @NotNull
    @Schema(description = "New enabled status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enabled;
}
