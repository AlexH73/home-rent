package de.ait.homerent.booking.service;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.service.RentalContractService;
import de.ait.homerent.mail.EmailService;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService additional unit tests")
class BookingServiceAdditionalTest {

    @Mock BookingRepository bookingRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock UserRepository userRepository;
    @Mock RentalContractService rentalContractService;
    @Mock EmailService emailService;

    @InjectMocks BookingService bookingService;

    @Test
    @DisplayName("getActiveBookings(): returns mapped list")
    void getActiveBookings_returnsMappedList() {
        Booking b = booking(1L, owner(10L), tenant(20L), BookingStatus.ACTIVE);
        when(bookingRepository.findByStatus(BookingStatus.ACTIVE)).thenReturn(List.of(b));

        List<BookingResponse> res = bookingService.getActiveBookings();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(1L);
        assertThat(res.get(0).getStatus()).isEqualTo(BookingStatus.ACTIVE);
    }

    @Test
    @DisplayName("findMyBookings(): returns tenant bookings mapped")
    void findMyBookings_returnsMappedList() {
        User tenant = tenant(20L);
        Booking b = booking(1L, owner(10L), tenant, BookingStatus.REQUESTED);

        when(bookingRepository.findByTenantId(20L)).thenReturn(List.of(b));

        List<BookingResponse> res = bookingService.findMyBookings(tenant);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getTenantName()).isEqualTo("tenant");
    }

    @Test
    @DisplayName("getBookingById(): when booking belongs to another tenant, throws 403 FORBIDDEN")
    void getBookingById_whenNotOwnerTenant_throws403() {
        User t1 = tenant(1L);
        User t2 = tenant(2L);

        Booking b = booking(1L, owner(10L), t2, BookingStatus.REQUESTED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.getBookingById(1L, t1),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("view your own bookings");
    }

    @Test
    @DisplayName("getPendingBookings(): returns REQUESTED bookings for owner's properties")
    void getPendingBookings_returnsRequestedForOwner() {
        User owner = owner(10L);
        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));

        Booking b = booking(1L, owner, tenant(20L), BookingStatus.REQUESTED);
        when(bookingRepository.findByPropertyOwnerIdAndStatus(10L, BookingStatus.REQUESTED))
                .thenReturn(List.of(b));

        List<BookingResponse> res = bookingService.getPendingBookings("owner1");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getStatus()).isEqualTo(BookingStatus.REQUESTED);
    }

    @Test
    @DisplayName("approveBooking(): when booking is not REQUESTED, throws 400 BAD_REQUEST")
    void approveBooking_whenWrongStatus_throws400() {
        User owner = owner(10L);
        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));

        Booking b = booking(1L, owner, tenant(20L), BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.approveBooking("owner1", 1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Booking cannot be approved");
    }

    @Test
    @DisplayName("rejectBooking(): when booking is not REQUESTED, throws 400 BAD_REQUEST")
    void rejectBooking_whenWrongStatus_throws400() {
        User owner = owner(10L);
        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));

        Booking b = booking(1L, owner, tenant(20L), BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.rejectBooking("owner1", 1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Booking cannot be rejected");
    }

    @Test
    @DisplayName("uploadContract(): rejects when booking belongs to another tenant (403)")
    void uploadContract_whenNotTenant_throws403() {
        User tenant1 = tenant(1L);
        User tenant2 = tenant(2L);

        Booking b = booking(1L, owner(10L), tenant2, BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        MockMultipartFile pdf = new MockMultipartFile("file", "c.pdf", "application/pdf", "x".getBytes());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.uploadContract(1L, pdf, tenant1),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("upload contracts for your own bookings");
        verifyNoInteractions(rentalContractService);
    }

    @Test
    @DisplayName("uploadContract(): rejects when booking status is REJECTED (400)")
    void uploadContract_whenWrongStatus_throws400() {
        User tenant = tenant(1L);

        Booking b = booking(1L, owner(10L), tenant, BookingStatus.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        MockMultipartFile pdf = new MockMultipartFile("file", "c.pdf", "application/pdf", "x".getBytes());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.uploadContract(1L, pdf, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("APPROVED or ACTIVE");
    }

    @Test
    @DisplayName("activateBooking(): when booking status is not APPROVED, throws 400 BAD_REQUEST")
    void activateBooking_whenNotApproved_throws400() {
        Booking b = booking(1L, owner(10L), tenant(20L), BookingStatus.ACTIVE);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.activateBooking(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Only APPROVED bookings can be activated");
    }

    @Test
    @DisplayName("finishBooking(): swallows exceptions from EmailService")
    void finishBooking_swallowsEmailExceptions() {
        Booking b = booking(1L, owner(10L), tenant(20L), BookingStatus.ACTIVE);

        doThrow(new RuntimeException("SMTP down")).when(emailService).sendRentalFinished(any());

        assertThatCode(() -> bookingService.finishBooking(b)).doesNotThrowAnyException();
        assertThat(b.getStatus()).isEqualTo(BookingStatus.FINISHED);

        verify(bookingRepository).save(b);
        verify(propertyRepository).save(any(Property.class));
    }

    private static Booking booking(Long id, User owner, User tenant, BookingStatus status) {
        Property p = new Property();
        p.setId(100L);
        p.setTitle("t");
        p.setAddress("a");
        p.setStatus(PropertyStatus.AVAILABLE);
        p.setOwner(owner);
        p.setAvailableFrom(LocalDateTime.of(2026, 1, 1, 0, 0));
        p.setAvailableTo(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        p.setPricePerDay(100);

        Booking b = new Booking();
        b.setId(id);
        b.setProperty(p);
        b.setTenant(tenant);
        b.setStatus(status);
        b.setStartDate(LocalDateTime.of(2026, 3, 1, 0, 0));
        b.setEndDate(LocalDateTime.of(2026, 3, 3, 23, 59, 59));
        b.setTotalPrice(300);
        return b;
    }

    private static User owner(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("owner");
        u.setRoles(Set.of(role(RoleName.ROLE_OWNER)));
        return u;
    }

    private static User tenant(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("tenant");
        u.setEmail("tenant@test.com");
        u.setRoles(Set.of(role(RoleName.ROLE_TENANT)));
        return u;
    }

    private static Role role(RoleName name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }
}
