package de.ait.homerent.issue.controller;

import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RestController
@RequestMapping("/api/operator/issues")
@RequiredArgsConstructor
public class OperatorIssueController {

    private static final Logger log = LoggerFactory.getLogger(OperatorIssueController.class);
    private final IssueService issueService;

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<List<IssueReportResponse>> getAllIssues() {
        log.info("Operator fetching all issue reports");
        return ResponseEntity.ok(issueService.findAll());
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> updateIssueStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("Operator updating status for issue {} to {}", id, status);
        issueService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}