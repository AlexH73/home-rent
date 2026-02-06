package de.ait.homerent.issue.dto;

import de.ait.homerent.issue.model.IssueStatus;
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
public class IssueReportResponse {
    private Long id;
    private Long bookingId;
    private String description;
    private IssueStatus status;
    private String photoPath;
    private LocalDateTime createdAt;
}