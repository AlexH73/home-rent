package de.ait.homerent.booking.service;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.service.RentalContractService;
import de.ait.homerent.mail.EmailService;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.Optional;

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
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock UserRepository userRepository;
    @Mock RentalContractService rentalContractService;
    @Mock EmailService emailService;

    @InjectMocks BookingService bookingService;

    @Captor ArgumentCaptor<Booking> bookingCaptor;

    private User tenant;
    private Property property;

    @BeforeEach
    void setUp() {
        tenant = new User();
        tenant.setId(10L);
        tenant.setUsername("tenant1");

        property = new Property();
        property.setId(100L);
        property.setTitle("Test Property");
        property.setAddress("Test address");
        property.setPricePerDay(100);
        property.setStatus(PropertyStatus.AVAILABLE);
        property.setAvailableFrom(LocalDateTime.of(2026, 1, 1, 0, 0));
        property.setAvailableTo(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
    }

    @Test
    @DisplayName("createBooking: normalizes dates to day bounds and sets REQUESTED")
    void createBooking_normalizesDates() {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setPropertyId(property.getId());
        req.setStartDate(LocalDateTime.of(2026, 3, 1, 15, 30));
        req.setEndDate(LocalDateTime.of(2026, 3, 3, 9, 0));

        when(propertyRepository.findById(property.getId())).thenReturn(Optional.of(property));
        when(bookingRepository.existsOverlappingBooking(eq(property.getId()), any(), any(), any()))
                .thenReturn(false);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingResponse resp = bookingService.createBooking(req, tenant);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getStatus()).isEqualTo(BookingStatus.REQUESTED);

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking saved = bookingCaptor.getValue();

        assertThat(saved.getStartDate()).isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
        // end should be at LocalTime.MAX (nanoseconds may be present)
        assertThat(saved.getEndDate().toLocalDate()).isEqualTo(LocalDateTime.of(2026, 3, 3, 0, 0).toLocalDate());
        assertThat(saved.getEndDate().toLocalTime()).isEqualTo(java.time.LocalTime.MAX);
    }

    @Test
    @DisplayName("createBooking: rejects if property not AVAILABLE")
    void createBooking_rejectsWhenPropertyNotAvailable() {
        property.setStatus(PropertyStatus.UNAVAILABLE);

        BookingCreateRequest req = new BookingCreateRequest();
        req.setPropertyId(property.getId());
        req.setStartDate(LocalDateTime.of(2026, 3, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 3, 3, 0, 0));

        when(propertyRepository.findById(property.getId())).thenReturn(Optional.of(property));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.createBooking(req, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Property is not available");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("createBooking: rejects if overlaps with REQUESTED/APPROVED/ACTIVE")
    void createBooking_rejectsWhenOverlaps() {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setPropertyId(property.getId());
        req.setStartDate(LocalDateTime.of(2026, 3, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 3, 3, 0, 0));

        when(propertyRepository.findById(property.getId())).thenReturn(Optional.of(property));
        when(bookingRepository.existsOverlappingBooking(eq(property.getId()), any(), any(), any()))
                .thenReturn(true);

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.createBooking(req, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("already booked");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("uploadContract: rejects when booking is REQUESTED")
    void uploadContract_rejectsWhenRequested() {
        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.REQUESTED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "x".getBytes()
        );

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.uploadContract(1L, pdf, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(rentalContractService);
    }

    @Test
    @DisplayName("activateBooking: rejects when contract is missing")
    void activateBooking_rejectsWhenContractMissing() {
        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.APPROVED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(rentalContractService.hasContract(1L)).thenReturn(false);

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.activateBooking(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("without uploaded rental contract");
    }

    @Test
    @DisplayName("activateBooking: sets booking ACTIVE and property RENTED")
    void activateBooking_setsActiveAndRentsProperty() {
        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.APPROVED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(rentalContractService.hasContract(1L)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse resp = bookingService.activateBooking(1L);

        assertThat(resp.getStatus()).isEqualTo(BookingStatus.ACTIVE);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACTIVE);

        verify(propertyRepository).save(argThat(p -> p.getStatus() == PropertyStatus.RENTED));
    }

    @Test
    @DisplayName("createBooking: rejects when startDate is after endDate")
    void createBooking_rejectsWhenStartAfterEnd() {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setPropertyId(property.getId());
        req.setStartDate(LocalDateTime.of(2026, 3, 5, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 3, 3, 0, 0));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.createBooking(req, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("Start date must be before or equal to end date");
        verifyNoInteractions(propertyRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("createBooking: rejects when property not found")
    void createBooking_rejectsWhenPropertyNotFound() {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setPropertyId(999L);
        req.setStartDate(LocalDateTime.of(2026, 3, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 3, 3, 0, 0));

        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.createBooking(req, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Property not found");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("uploadContract: happy path delegates to RentalContractService for APPROVED booking")
    void uploadContract_happyPath_delegates() {
        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.APPROVED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "x".getBytes()
        );

        bookingService.uploadContract(1L, pdf, tenant);

        verify(rentalContractService).uploadContract(1L, pdf);
    }

    @Test
    @DisplayName("uploadContract: rejects when file name is blank")
    void uploadContract_rejectsWhenFileNameBlank() {
        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.APPROVED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "   ", "application/pdf", "x".getBytes()
        );

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.uploadContract(1L, pdf, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("File name is required");

        verifyNoInteractions(rentalContractService);
    }

    @Test
    @DisplayName("approveBooking: happy path sets booking APPROVED and sends email")
    void approveBooking_happyPath_setsApproved_sendsEmail() {
        User owner = new User();
        owner.setId(200L);
        owner.setUsername("owner1");
        owner.setRoles(Set.of(role(RoleName.ROLE_OWNER)));

        tenant.setEmail("tenant1@test.com");

        property.setOwner(owner);
        property.setStatus(PropertyStatus.AVAILABLE); // начальный статус

        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.REQUESTED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse resp = bookingService.approveBooking("owner1", 1L);

        assertThat(resp.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(property.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);

        verify(emailService).sendBookingApproved(any());
        verify(bookingRepository).save(booking);
        verify(propertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveBooking: rejects when booking belongs to another owner (403)")
    void approveBooking_rejectsWhenNotPropertyOwner() {
        User owner = new User();
        owner.setId(200L);
        owner.setUsername("owner1");
        owner.setRoles(Set.of(role(RoleName.ROLE_OWNER)));

        User anotherOwner = new User();
        anotherOwner.setId(201L);

        property.setOwner(anotherOwner);

        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.REQUESTED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.approveBooking("owner1", 1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("approve only your bookings");

        verify(emailService, never()).sendBookingApproved(any());
    }

    @Test
    @DisplayName("rejectBooking: happy path sets booking REJECTED and property AVAILABLE")
    void rejectBooking_happyPath_setsRejected_makesPropertyAvailable() {
        User owner = new User();
        owner.setId(200L);
        owner.setUsername("owner1");
        owner.setRoles(Set.of(role(RoleName.ROLE_OWNER)));

        property.setOwner(owner);
        property.setStatus(PropertyStatus.BOOKED);

        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.REQUESTED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse resp = bookingService.rejectBooking("owner1", 1L);

        assertThat(resp.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(property.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);

        verify(propertyRepository).save(property);
    }

    @Test
    @DisplayName("finishBooking: sets booking FINISHED and property AVAILABLE; email failure is swallowed")
    void finishBooking_setsFinished_andSwallowsEmailFailure() {
        property.setStatus(PropertyStatus.RENTED);
        tenant.setEmail("tenant1@test.com");

        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.ACTIVE)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        doThrow(new RuntimeException("mail down")).when(emailService).sendRentalFinished(any());

        assertThatCode(() -> bookingService.finishBooking(booking)).doesNotThrowAnyException();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.FINISHED);
        assertThat(property.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);

        verify(bookingRepository).save(booking);
        verify(propertyRepository).save(property);
    }

    private static Role role(RoleName name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }

    @Test
    @DisplayName("getPendingBookings: returns mapped REQUESTED bookings for owner")
    void getPendingBookings_happyPath_returnsList() {
        User owner = new User();
        owner.setId(200L);
        owner.setUsername("owner1");
        owner.setRoles(Set.of(role(RoleName.ROLE_OWNER)));

        // booking must have property with this owner
        property.setOwner(owner);

        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.REQUESTED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));
        when(bookingRepository.findByPropertyOwnerIdAndStatus(200L, BookingStatus.REQUESTED))
                .thenReturn(List.of(booking));

        var res = bookingService.getPendingBookings("owner1");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(1L);
        assertThat(res.get(0).getStatus()).isEqualTo(BookingStatus.REQUESTED);
    }

    @Test
    @DisplayName("getBookingById: rejects when booking belongs to another tenant (403)")
    void getBookingById_whenNotOwnerTenant_throws403() {
        User otherTenant = new User();
        otherTenant.setId(11L);
        otherTenant.setUsername("other");

        Booking booking = Booking.builder()
                .id(1L)
                .tenant(otherTenant)
                .property(property)
                .status(BookingStatus.REQUESTED)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.getBookingById(1L, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("activateBooking: rejects when booking not found (404)")
    void activateBooking_whenNotFound_throws404() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.activateBooking(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("rejectBooking: rejects when booking not found (404)")
    void rejectBooking_whenBookingNotFound_throws404() {
        User owner = new User();
        owner.setId(200L);
        owner.setUsername("owner1");
        owner.setRoles(Set.of(role(RoleName.ROLE_OWNER)));

        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.rejectBooking("owner1", 1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("approveBooking: rejects when booking not found (404)")
    void approveBooking_whenBookingNotFound_throws404() {
        User owner = new User();
        owner.setId(200L);
        owner.setUsername("owner1");
        owner.setRoles(Set.of(role(RoleName.ROLE_OWNER)));

        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.approveBooking("owner1", 1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("getActiveBookings: returns empty list when no ACTIVE bookings")
    void getActiveBookings_whenEmpty_returnsEmptyList() {
        when(bookingRepository.findByStatus(BookingStatus.ACTIVE)).thenReturn(List.of());
        assertThat(bookingService.getActiveBookings()).isEmpty();
    }

    @Test
    @DisplayName("findMyBookings: returns empty list when tenant has no bookings")
    void findMyBookings_whenEmpty_returnsEmptyList() {
        when(bookingRepository.findByTenantId(tenant.getId())).thenReturn(List.of());
        assertThat(bookingService.findMyBookings(tenant)).isEmpty();
    }

    @Test
    @DisplayName("finishBooking: marks booking FINISHED and sends email on success")
    void finishBooking_happyPath_sendsEmail() {
        property.setStatus(PropertyStatus.RENTED);
        tenant.setEmail("tenant1@test.com");

        Booking booking = Booking.builder()
                .id(1L)
                .tenant(tenant)
                .property(property)
                .status(BookingStatus.ACTIVE)
                .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 3, 3, 0, 0))
                .totalPrice(300)
                .build();

        bookingService.finishBooking(booking);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.FINISHED);
        assertThat(property.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);

        verify(emailService).sendRentalFinished(any());
        verify(bookingRepository).save(booking);
        verify(propertyRepository).save(property);
    }

    @Test
    @DisplayName("getBookingById: when booking not found, throws 404 NOT_FOUND")
    void getBookingById_whenBookingNotFound_throws404() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.getBookingById(999L, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Booking not found");
    }

    @Test
    @DisplayName("uploadContract: when booking not found, throws 404 NOT_FOUND")
    void uploadContract_whenBookingNotFound_throws404() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        MockMultipartFile pdf = new MockMultipartFile("file", "contract.pdf", "application/pdf", "x".getBytes());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.uploadContract(999L, pdf, tenant),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Booking not found");
    }

    @Test
    @DisplayName("approveBooking: when owner user not found, throws 404 NOT_FOUND")
    void approveBooking_whenOwnerNotFound_throws404() {
        when(userRepository.findByUsername("missing-owner")).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> bookingService.approveBooking("missing-owner", 1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("User not found");
    }
}
