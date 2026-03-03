package de.ait.homerent.issue.service;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.utils.FileStorageUtilService;
import de.ait.homerent.issue.dto.IssueCreateRequest;
import de.ait.homerent.issue.dto.IssueReportResponse;
import de.ait.homerent.issue.model.IssueReport;
import de.ait.homerent.issue.model.IssueStatus;
import de.ait.homerent.issue.repository.IssueReportRepository;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IssueService unit tests")
class IssueServiceTest {

    @Mock
    IssueReportRepository issueReportRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    FileStorageUtilService fileStorageService;

    @InjectMocks
    IssueService issueService;

    @Test
    @DisplayName("createIssue(): when booking is not found, throws 404 NOT_FOUND")
    void createIssue_whenBookingNotFound_throws404() {
        User tenant = new User();
        tenant.setId(10L);
        tenant.setUsername("t1");

        IssueCreateRequest req = new IssueCreateRequest();
        req.setBookingId(99L);
        req.setDescription("broken");

        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.createIssue(req, tenant))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404")
                .hasMessageContaining("Booking not found");
    }

    @Test
    @DisplayName("createIssue(): when booking belongs to another tenant, throws 403 FORBIDDEN")
    void createIssue_whenBookingBelongsToAnotherTenant_throws403() {
        User tenant = new User();
        tenant.setId(10L);
        tenant.setUsername("t1");

        User otherTenant = new User();
        otherTenant.setId(11L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setTenant(otherTenant);

        IssueCreateRequest req = new IssueCreateRequest();
        req.setBookingId(1L);
        req.setDescription("broken");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> issueService.createIssue(req, tenant))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403")
                .hasMessageContaining("only report issues for your own bookings");
    }

    @Test
    @DisplayName("createIssue(): without photo, sets photoPath to 'no-photo' and saves report")
    void createIssue_withoutPhoto_setsNoPhotoAndSaves() {
        User tenant = new User();
        tenant.setId(10L);
        tenant.setUsername("t1");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setTenant(tenant);
        booking.setStatus(BookingStatus.APPROVED);

        // Add a property with the AVAILABLE status
        var property = new de.ait.homerent.property.model.Property();
        property.setId(100L);
        property.setStatus(PropertyStatus.AVAILABLE);
        booking.setProperty(property);

        IssueCreateRequest req = new IssueCreateRequest();
        req.setBookingId(1L);
        req.setDescription("broken");
        req.setPhoto(null);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(issueReportRepository.save(any(IssueReport.class))).thenAnswer(inv -> inv.getArgument(0));

        IssueReportResponse response = issueService.createIssue(req, tenant);

        assertThat(response.getPhotoPath()).isEqualTo("no-photo");
        assertThat(response.getDescription()).isEqualTo("broken");
        assertThat(response.getStatus()).isEqualTo(IssueStatus.OPEN);

        verify(fileStorageService, never()).storeIssueFile(anyLong(), any());
        verify(issueReportRepository).save(any(IssueReport.class));
    }

    @Test
    @DisplayName("createIssue(): with photo, stores photo and saves report")
    void createIssue_withPhoto_storesPhoto() {
        User tenant = new User();
        tenant.setId(10L);
        tenant.setUsername("t1");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setTenant(tenant);
        booking.setStatus(BookingStatus.APPROVED);

        // Add a property with the AVAILABLE status
        var property = new de.ait.homerent.property.model.Property();
        property.setId(100L);
        property.setStatus(PropertyStatus.AVAILABLE);
        booking.setProperty(property);

        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);

        IssueCreateRequest req = new IssueCreateRequest();
        req.setBookingId(1L);
        req.setDescription("broken");
        req.setPhoto(photo);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(fileStorageService.storeIssueFile(1L, photo)).thenReturn("path/to/photo.jpg");
        when(issueReportRepository.save(any(IssueReport.class))).thenAnswer(inv -> inv.getArgument(0));

        IssueReportResponse response = issueService.createIssue(req, tenant);

        assertThat(response.getPhotoPath()).isEqualTo("path/to/photo.jpg");
        verify(fileStorageService).storeIssueFile(1L, photo);
        verify(issueReportRepository).save(any(IssueReport.class));
    }

    @Test
    @DisplayName("findAll(): returns mapped list of issue reports")
    void findAll_mapsToResponse() {
        IssueReport issue = new IssueReport();
        issue.setId(1L);
        issue.setDescription("d1");
        issue.setStatus(IssueStatus.OPEN);
        issue.setPhotoPath("p1");

        when(issueReportRepository.findAll()).thenReturn(List.of(issue));

        List<IssueReportResponse> res = issueService.findAll();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(1L);
        assertThat(res.get(0).getDescription()).isEqualTo("d1");
        assertThat(res.get(0).getStatus()).isEqualTo(IssueStatus.OPEN);
        assertThat(res.get(0).getPhotoPath()).isEqualTo("p1");
    }

    @Test
    @DisplayName("updateStatus(): when issue not found, throws 404 NOT_FOUND")
    void updateStatus_whenNotFound_throws404() {
        when(issueReportRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.updateStatus(5L, IssueStatus.DONE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("updateStatus(): updates status and persists entity")
    void updateStatus_updatesAndSaves() {
        IssueReport issue = new IssueReport();
        issue.setId(5L);
        issue.setStatus(IssueStatus.OPEN);

        when(issueReportRepository.findById(5L)).thenReturn(Optional.of(issue));

        issueService.updateStatus(5L, IssueStatus.DONE);

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.DONE);
        verify(issueReportRepository).save(issue);
    }

    @Test
    @DisplayName("createIssue(): when booking status is not APPROVED or ACTIVE, throws 400 BAD_REQUEST")
    void createIssue_whenBookingStatusInvalid_throws400() {
        User tenant = new User();
        tenant.setId(10L);
        tenant.setUsername("t1");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setTenant(tenant);
        booking.setStatus(BookingStatus.REQUESTED); // Not APPROVED/ACTIVE

        IssueCreateRequest req = new IssueCreateRequest();
        req.setBookingId(1L);
        req.setDescription("broken");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> issueService.createIssue(req, tenant))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400")
                .hasMessageContaining("booking status must be APPROVED or ACTIVE");
    }

    @Test
    @DisplayName("createIssue(): when property status is not AVAILABLE, throws 400 BAD_REQUEST")
    void createIssue_whenPropertyNotAvailable_throws400() {
        User tenant = new User();
        tenant.setId(10L);
        tenant.setUsername("t1");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setTenant(tenant);

        // property with non-AVAILABLE status
        booking.setProperty(new de.ait.homerent.property.model.Property());
        booking.getProperty().setStatus(PropertyStatus.BOOKED);

        booking.setStatus(BookingStatus.APPROVED);

        IssueCreateRequest req = new IssueCreateRequest();
        req.setBookingId(1L);
        req.setDescription("broken");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> issueService.createIssue(req, tenant))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400")
                .hasMessageContaining("property must be AVAILABLE");
    }
}
