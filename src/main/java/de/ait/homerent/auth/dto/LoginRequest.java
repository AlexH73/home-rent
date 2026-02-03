package de.ait.homerent.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Schema(description = "User login credentials")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(
            description = "User's username",
            example = "john_doe",
            minLength = 3,
            maxLength = 50
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "User's password",
            example = "password123",
            minLength = 6,
            maxLength = 100
    )
    private String password;
}
