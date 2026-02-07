package de.ait.homerent.issue.service;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.repository.IssueReportRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueService {

    private final IssueReportRepository issueReportRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public IssueReportResponse createIssue(IssueCreateRequest request, MultipartFile photo, String username) {
        User tenant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getTenant().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        String photoPath = "issues/" + System.currentTimeMillis() + "_" + photo.getOriginalFilename();

        IssueReport issue = new IssueReport();
        issue.setBooking(booking);
        issue.setReportedBy(tenant);
        issue.setDescription(request.getDescription());
        issue.setPhotoPath(photoPath);
        issue.setStatus(IssueStatus.OPEN);

        return mapToResponse(issueReportRepository.save(issue));
    }

    private IssueReportResponse mapToResponse(IssueReport issue) {
        return IssueReportResponse.builder()
                .id(issue.getId())
                .bookingId(issue.getBooking().getId())
                .reportedById(issue.getReportedBy().getId())
                .description(issue.getDescription())
                .photoPath(issue.getPhotoPath())
                .createdAt(issue.getCreatedAt())
                .status(issue.getStatus())
                .build();
    }
}
