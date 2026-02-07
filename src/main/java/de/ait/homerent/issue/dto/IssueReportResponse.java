package de.ait.homerent.issue.dto;

import de.ait.homerent.issue.model.IssueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IssueReportResponse {
    private Long id;
    private Long bookingId;
    private Long reportedById;
    private String description;
    private String photoPath;
    private LocalDateTime createdAt;
    private IssueStatus status;
}
