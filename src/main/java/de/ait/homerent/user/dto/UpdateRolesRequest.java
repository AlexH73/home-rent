package de.ait.homerent.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 09.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
@Schema(description = "Request payload for updating user roles")
public class UpdateRolesRequest {

    @NotEmpty(message = "Role list must not be empty")
    @Schema(description = "List of role names (e.g., ROLE_TENANT, ROLE_OWNER, ROLE_OPERATOR, ROLE_ADMIN)",
            example = "[\"ROLE_TENANT\", \"ROLE_OWNER\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> roles;
}
