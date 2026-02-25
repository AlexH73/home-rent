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
        property.setStatus(PropertyStatus.BOOKED);

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
}
