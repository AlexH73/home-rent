package de.ait.homerent.booking.service;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.service.RentalContractService;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
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
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RentalContractService rentalContractService;

    @Transactional(readOnly = true)
    public List<BookingResponse> getActiveBookings() {
        log.info("Fetching all active bookings from database");
        return bookingRepository.findByStatus(BookingStatus.ACTIVE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findMyBookings(User tenant) {
        log.info("Fetching bookings for tenant: {}", tenant.getUsername());
        return bookingRepository.findByTenantId(tenant.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, User tenant) {
        log.info("Fetching booking with ID: {} for user: {}", id, tenant.getUsername());
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getTenant().getId().equals(tenant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own bookings");
        }

        return mapToResponse(booking);
    }

    @Transactional
    public void uploadContract(Long bookingId, MultipartFile file, User tenant) {
        log.info("Uploading contract for booking ID: {} by user: {}", bookingId, tenant.getUsername());
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getTenant().getId().equals(tenant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only upload contracts for your own bookings");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required");
        }

        rentalContractService.uploadContract(bookingId, file);
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }

        var property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        var tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));

        int totalPrice = calculateTotalPrice(request.getStartDate(), request.getEndDate(), property.getPricePerDay());

        Booking booking = Booking.builder()
                .property(property)
                .tenant(tenant)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(BookingStatus.REQUESTED)
                .totalPrice(totalPrice)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("New booking created with ID: {}", savedBooking.getId());

        return mapToResponse(savedBooking);
    }

    private int calculateTotalPrice(LocalDateTime start, LocalDateTime end, int pricePerDay) {
        long days = Duration.between(start.toLocalDate().atStartOfDay(), end.toLocalDate().atStartOfDay()).toDays() + 1;
        return (int) (days * pricePerDay);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setPropertyTitle(booking.getProperty().getTitle());
        response.setTenantName(booking.getTenant().getUsername());
        response.setStartDate(booking.getStartDate());
        response.setEndDate(booking.getEndDate());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        return response;
    }
}
