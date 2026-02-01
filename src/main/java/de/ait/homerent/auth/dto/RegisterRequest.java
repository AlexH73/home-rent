package de.ait.homerent.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(description = "User registration data")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(
            description = "Unique username for the new account",
            example = "john_doe",
            minLength = 3,
            maxLength = 50
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(
            description = "Valid email address for the new account",
            example = "john@example.com",
            pattern = "^[A-Za-z0-9+_.-]+@(.+)$"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(
            description = "Password for the new account (will be encrypted)",
            example = "password123",
            minLength = 6,
            maxLength = 100
    )
    private String password;
}
