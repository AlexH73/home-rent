package de.ait.homerent.issue.service;

import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.repository.IssueReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueReportRepository issueReportRepository;

    @Transactional(readOnly = true)
    public List<IssueReportResponse> findAll() {
        log.info("Fetching all issue reports");
        return issueReportRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        log.info("Updating status for issue ID: {} to {}", id, status);

        IssueReport issue = issueReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Issue not found with id: " + id));

        try {
            issue.setStatus(IssueStatus.valueOf(status.toUpperCase()));
            issueReportRepository.save(issue);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status provided: {}", status);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    private IssueReportResponse mapToResponse(IssueReport issue) {
        IssueReportResponse response = new IssueReportResponse();
        response.setId(issue.getId());
        response.setBookingId(issue.getBookingId() != null ? issue.getBookingId().getId() : null);
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPhotoPath(issue.getPhotoPath());
        response.setCreatedAt(issue.getCreatedAt());
        return response;
    }
}