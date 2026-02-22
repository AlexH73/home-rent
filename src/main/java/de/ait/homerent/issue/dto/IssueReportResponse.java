package de.ait.homerent.issue.dto;

import de.ait.homerent.issue.model.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing issue report details")
public class IssueReportResponse {

    @Schema(description = "Issue ID", example = "1")
    private Long id;

    @Schema(description = "Booking ID", example = "10")
    private Long bookingId;

    @Schema(description = "Issue description", example = "Leaky faucet in kitchen")
    private String description;

    @Schema(description = "Current status", example = "OPEN", allowableValues = {"OPEN", "IN_PROGRESS", "DONE"})
    private IssueStatus status;

    @Schema(description = "Path to uploaded photo (or 'no-photo')", example = "/uploads/issue/10/2026-02-21/photo.jpg")
    private String photoPath;

    @Schema(description = "Creation timestamp", example = "2026-02-21T10:15:30")
    private LocalDateTime createdAt;
}