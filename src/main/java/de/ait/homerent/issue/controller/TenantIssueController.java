package de.ait.homerent.issue.controller;

import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.service.IssueService;
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
public class TenantIssueController {
    private final IssueService issueService;

    @PostMapping
    public IssueReportResponse createIssue(
            @RequestPart("data") IssueCreateRequest request,
            @RequestPart("photo") MultipartFile photo,
            Principal principal) {
        return issueService.createIssue(request, photo, principal.getName());
    }
}
