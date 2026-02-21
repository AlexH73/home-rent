package de.ait.homerent.issue.controller;

import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("/api/operator/issues")
@RequiredArgsConstructor
@Tag(name = "Operator Issue Management", description = "Endpoints for operators to manage bookings and issues")
public class OperatorIssueController {

    private final IssueService issueService;

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Get all issue reports", description = "Retrieves a list of all reported property issues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of issues",
                    content = @Content(schema = @Schema(implementation = IssueReportResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires OPERATOR role")
    })
    public ResponseEntity<List<IssueReportResponse>> getAllIssues() {
        log.info("Operator fetching all issue reports");
        return ResponseEntity.ok(issueService.findAll());
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Update issue status", description = "Changes the current status of a specific maintenance issue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or issue not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires OPERATOR role"),
            @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<Void> updateIssueStatus(
            @Parameter(description = "ID of the issue", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "New status (OPEN, IN_PROGRESS, DONE)", required = true,
                    schema = @Schema(implementation = IssueStatus.class))
            @RequestParam IssueStatus status) {
        log.info("Operator updating status for issue {} to {}", id, status);
        issueService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}