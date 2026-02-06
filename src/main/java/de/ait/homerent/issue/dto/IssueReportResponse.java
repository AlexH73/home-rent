package de.ait.homerent.issue.dto;

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
    private String status;
    private String photoPath;
    private LocalDateTime createdAt;
}
