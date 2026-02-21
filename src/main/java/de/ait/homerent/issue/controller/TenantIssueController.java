package de.ait.homerent.issue.controller;

import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.service.IssueService;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "Tenant Issue Management", description = "Operations for tenants to manage bookings and issues")
public class TenantIssueController {

    private final IssueService issueService;
    private final UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an issue report", description = "Allows the tenant to report an issue (breakage) related to their booking")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Issue created successfully",
                    content = @Content(schema = @Schema(implementation = IssueReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – not your booking or not TENANT"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public IssueReportResponse createIssue(
            @Parameter(description = "Issue details (bookingId, description, optional photo)", required = true)
            @Valid @ModelAttribute IssueCreateRequest request,
            Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return issueService.createIssue(request, user);
    }
}
