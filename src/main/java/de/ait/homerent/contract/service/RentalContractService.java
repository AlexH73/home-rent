package de.ait.homerent.contract.service;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.dto.ContractUploadedEmailRequest;
import de.ait.homerent.contract.model.RentalContract;
import de.ait.homerent.contract.repository.RentalContractRepository;
import de.ait.homerent.mail.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 13.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RentalContractService {

    private final RentalContractRepository rentalContractRepository;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public boolean hasContract(Long bookingId) {
        return rentalContractRepository.findByBookingId(bookingId).isPresent();
    }

    @Transactional
    public RentalContract uploadContract(Long bookingId, MultipartFile file) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        // saving the file to disk via FileStorageService
        String filePath = fileStorageService.storeRentalContract(bookingId, file);

        // saving the record in the database
        RentalContract contract = rentalContractRepository.findByBookingId(bookingId)
                .orElse(new RentalContract());

        contract.setBooking(booking);
        contract.setFilePath(filePath);
        contract.setUploadedAt(LocalDateTime.now());

        RentalContract saved = rentalContractRepository.save(contract);
        log.info("Rental contract saved with id {}", saved.getId());

        // ------------------- sending an email -------------------
        sendContractUploadedEmail(booking, file.getOriginalFilename());

        return saved;
    }

    private void sendContractUploadedEmail(Booking booking, String originalFileName) {
        try {
            ContractUploadedEmailRequest emailRequest = new ContractUploadedEmailRequest();
            if (booking.getTenant() != null) {
                emailRequest.setEmail(booking.getTenant().getEmail());
                emailRequest.setUsername(booking.getTenant().getUsername());
            }
            if (booking.getProperty() != null) {
                emailRequest.setPropertyAddress(booking.getProperty().getAddress());
            }
            emailRequest.setContractFileName(originalFileName);

            emailService.sendContractUploaded(emailRequest);

        } catch (Exception e) {
            log.error("Failed to send contract uploaded email for booking {}", booking.getId(), e);

        }
    }
}