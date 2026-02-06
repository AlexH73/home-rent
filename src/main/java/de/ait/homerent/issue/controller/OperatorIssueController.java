package de.ait.homerent.issue.controller;

import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Operator Issue Management", description = "Operations for managing property maintenance issues (Operator access only)")
public class OperatorIssueController {

    private final IssueService issueService;

    @Operation(
            summary = "Get all issue reports",
            description = "Retrieves a list of all reported property issues and maintenance requests"
    )
    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<List<IssueReportResponse>> getAllIssues() {
        log.info("Operator fetching all issue reports");
        return ResponseEntity.ok(issueService.findAll());
    }

    @Operation(
            summary = "Update issue status",
            description = "Changes the current status of a specific maintenance issue (e.g., to IN_PROGRESS or CLOSED)"
    )
    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> updateIssueStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("Operator updating status for issue {} to {}", id, status);
        issueService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}