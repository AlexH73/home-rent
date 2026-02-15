package de.ait.homerent.issue.controller;

import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.service.IssueService;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 13.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/tenant/issues")
@RequiredArgsConstructor
@Tag(name = "Tenant Issue Management", description = "Operations related to reporting issues for tenants")
public class TenantIssueController {

    private final IssueService issueService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an issue report", description = "Allows the tenant to report an issue (breakage) related to their booking")
    public IssueReportResponse createIssue(@Valid @ModelAttribute IssueCreateRequest request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return issueService.createIssue(request, user);
    }
}
