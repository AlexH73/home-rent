package de.ait.homerent.issue.controller;

import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/tenant/issues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT')")
@Tag(name = "Tenant Issue Controller", description = "Operations related to issue reports for tenants")
public class TenantIssueController {
    private final IssueService issueService;

    @PostMapping
    @Operation(summary = "Create issue report", description = "Allows a tenant to report a problem/issue related to their booking")
    public IssueReportResponse createIssue(
            @RequestPart("data") IssueCreateRequest request,
            @RequestPart("photo") MultipartFile photo,
            Principal principal) {
        return issueService.createIssue(request, photo, principal.getName());
    }
}
