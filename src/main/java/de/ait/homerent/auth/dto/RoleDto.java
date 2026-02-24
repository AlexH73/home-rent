package de.ait.homerent.auth.dto;

import de.ait.homerent.user.model.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 24.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role information")
public class RoleDto {
    @Schema(description = "Role ID", example = "1")
    private Long id;

    @Schema(description = "Role name", example = "ROLE_TENANT")
    private RoleName name;
}
