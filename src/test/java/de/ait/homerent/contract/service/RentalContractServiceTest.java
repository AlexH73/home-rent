package de.ait.homerent.contract.service;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.model.RentalContract;
import de.ait.homerent.contract.repository.RentalContractRepository;
import de.ait.homerent.mail.EmailService;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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
@DisplayName("RentalContractService unit tests")
class RentalContractServiceTest {

    @Mock RentalContractRepository rentalContractRepository;
    @Mock BookingRepository bookingRepository;
    @Mock FileStorageService fileStorageService;
    @Mock EmailService emailService;

    @InjectMocks RentalContractService rentalContractService;

    @Test
    @DisplayName("hasContract(): returns true when repository contains a contract for booking")
    void hasContract_returnsTrueWhenPresent() {
        when(rentalContractRepository.findByBookingId(1L)).thenReturn(Optional.of(new RentalContract()));
        assertThat(rentalContractService.hasContract(1L)).isTrue();
    }

    @Test
    @DisplayName("hasContract(): returns false when repository has no contract for booking")
    void hasContract_returnsFalseWhenMissing() {
        when(rentalContractRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        assertThat(rentalContractService.hasContract(1L)).isFalse();
    }

    @Test
    @DisplayName("uploadContract(): throws when booking not found")
    void uploadContract_whenBookingNotFound_throws() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "x".getBytes()
        );

        assertThatThrownBy(() -> rentalContractService.uploadContract(1L, pdf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    @DisplayName("uploadContract(): creates a new contract when none exists and sends email")
    void uploadContract_createsNewContract_andSendsEmail() {
        Booking booking = bookingWithTenantAndProperty();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(fileStorageService.storeRentalContract(eq(1L), any())).thenReturn("/tmp/contract.pdf");

        when(rentalContractRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(rentalContractRepository.save(any(RentalContract.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "x".getBytes()
        );

        RentalContract saved = rentalContractService.uploadContract(1L, pdf);

        assertThat(saved.getBooking()).isEqualTo(booking);
        assertThat(saved.getFilePath()).isEqualTo("/tmp/contract.pdf");
        assertThat(saved.getUploadedAt()).isNotNull();

        verify(emailService).sendContractUploaded(argThat(req ->
                "tenant@test.com".equals(req.getEmail())
                        && "tenant1".equals(req.getUsername())
                        && "addr".equals(req.getPropertyAddress())
                        && "contract.pdf".equals(req.getContractFileName())
        ));
    }

    @Test
    @DisplayName("uploadContract(): updates existing contract when it already exists")
    void uploadContract_updatesExistingContract() {
        Booking booking = bookingWithTenantAndProperty();

        RentalContract existing = new RentalContract();
        existing.setId(55L);
        existing.setFilePath("old");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(fileStorageService.storeRentalContract(eq(1L), any())).thenReturn("/tmp/new.pdf");

        when(rentalContractRepository.findByBookingId(1L)).thenReturn(Optional.of(existing));
        when(rentalContractRepository.save(any(RentalContract.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "new.pdf", "application/pdf", "x".getBytes()
        );

        RentalContract saved = rentalContractService.uploadContract(1L, pdf);

        assertThat(saved.getId()).isEqualTo(55L);
        assertThat(saved.getFilePath()).isEqualTo("/tmp/new.pdf");
        assertThat(saved.getBooking()).isEqualTo(booking);
        assertThat(saved.getUploadedAt()).isNotNull();
    }

    @Test
    @DisplayName("uploadContract(): swallows exceptions during email sending")
    void uploadContract_whenEmailFails_doesNotThrow() {
        Booking booking = bookingWithTenantAndProperty();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(fileStorageService.storeRentalContract(eq(1L), any())).thenReturn("/tmp/contract.pdf");

        when(rentalContractRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(rentalContractRepository.save(any(RentalContract.class))).thenAnswer(inv -> inv.getArgument(0));

        doThrow(new RuntimeException("SMTP down")).when(emailService).sendContractUploaded(any());

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "x".getBytes()
        );

        assertThatCode(() -> rentalContractService.uploadContract(1L, pdf))
                .doesNotThrowAnyException();
    }

    private static Booking bookingWithTenantAndProperty() {
        User tenant = new User();
        tenant.setUsername("tenant1");
        tenant.setEmail("tenant@test.com");

        Property property = new Property();
        property.setAddress("addr");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setTenant(tenant);
        booking.setProperty(property);
        return booking;
    }
}
