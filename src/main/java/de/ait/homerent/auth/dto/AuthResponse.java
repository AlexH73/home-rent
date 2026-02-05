package de.ait.homerent.auth.dto;

import de.ait.homerent.user.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing user information")
public class AuthResponse {

    @Schema(
            description = "Response message",
            example = "Registration successful"
    )
    private String message;

    @Schema(
            description = "Authenticated user's username",
            example = "john_doe"
    )
    private String username;

    @Schema(
            description = "Authenticated user's email address",
            example = "john@example.com"
    )
    private String email;

    @Schema(
            description = "Roles assigned to the user",
            example = """
                    [
                      {
                        "id": 1,
                        "name": "ROLE_TENANT"
                      }
                    ]
                    """
    )
    private Set<Role> roles;
}
