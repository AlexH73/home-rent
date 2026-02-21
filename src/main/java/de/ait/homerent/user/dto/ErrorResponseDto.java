package de.ait.homerent.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 21.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Data
@Builder
@Schema(description = "Standard error response body")
public class ErrorResponseDto {

    @Schema(description = "Timestamp of the error", example = "2026-02-21T12:00:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP status phrase", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "User not found with id: 1")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/admin/users/1/status")
    private String path;
}
