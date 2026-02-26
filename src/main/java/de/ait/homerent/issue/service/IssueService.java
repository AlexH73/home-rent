package de.ait.homerent.issue.service;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.service.FileStorageService;
import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.repository.IssueReportRepository;
import de.ait.homerent.user.model.User;
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
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public IssueReportResponse createIssue(IssueCreateRequest request, User tenant) {
        log.info("Creating issue report for booking ID: {} by user: {}", request.getBookingId(), tenant.getUsername());

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getTenant().getId().equals(tenant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only report issues for your own bookings");
        }

        if (!BookingStatus.ACTIVE.equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issues can only be reported for active bookings");
        }

        String photoPath = "no-photo";
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            photoPath = fileStorageService.storeIssuePhoto(booking.getId(), request.getPhoto());
        }

        IssueReport issue = new IssueReport();
        issue.setBooking(booking);
        issue.setReportedBy(tenant);
        issue.setDescription(request.getDescription());
        issue.setPhotoPath(photoPath);
        issue.setStatus(IssueStatus.OPEN);

        IssueReport savedIssue = issueReportRepository.save(issue);
        log.info("Issue report created with ID: {}", savedIssue.getId());

        return mapToResponse(savedIssue);
    }

    @Transactional(readOnly = true)
    public List<IssueReportResponse> findAll() {
        log.info("Fetching all issue reports");
        return issueReportRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(Long id, IssueStatus status) {
        log.info("Updating status for issue ID: {} to {}", id, status);

        IssueReport issue = issueReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Issue not found with id: " + id));

        try {
            issue.setStatus(status);
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
        response.setBookingId(issue.getBooking() != null ? issue.getBooking().getId() : null);
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPhotoPath(issue.getPhotoPath());
        response.setCreatedAt(issue.getCreatedAt());
        return response;
    }
}