package de.ait.homerent.issue.service;

import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.repository.IssueReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueReportRepository issueReportRepository;

    public List<IssueReportResponse> findAll() {
        List<IssueReport> issues = issueReportRepository.findAll();

        return issues.stream().map(issue -> {
            IssueReportResponse response = new IssueReportResponse();
            response.setId(issue.getId());
            response.setBookingId(issue.getBookingId().getId());
            response.setDescription(issue.getDescription());
            response.setStatus(issue.getStatus().name());
            response.setPhotoPath(issue.getPhotoPath());
            response.setCreatedAt(issue.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        IssueReport issue = issueReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found with id: " + id));

        issue.setStatus(IssueStatus.valueOf(status.toUpperCase()));
        issueReportRepository.save(issue);
    }
}
