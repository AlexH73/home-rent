package de.ait.homerent.booking.service;

import de.ait.homerent.booking.dto.BookingCreateRequest;
import de.ait.homerent.booking.dto.BookingEmailRequest;
import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.dto.RentalFinishedEmailRequest;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.contract.service.RentalContractService;
import de.ait.homerent.mail.EmailService;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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

    private static final Set<BookingStatus> BLOCKING_STATUSES =
            EnumSet.of(BookingStatus.REQUESTED, BookingStatus.APPROVED, BookingStatus.ACTIVE);

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RentalContractService rentalContractService;
    private final EmailService emailService;

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

        if (booking.getStatus() != BookingStatus.APPROVED && booking.getStatus() != BookingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Contract can be uploaded only for APPROVED or ACTIVE bookings");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required");
        }

        // File type/size is validated inside FileStorageService
        rentalContractService.uploadContract(bookingId, file);
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, User tenant) {

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before or equal to end date");
        }

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property is not available for booking");
        }

        LocalDateTime normalizedStart = normalizeStart(request.getStartDate());
        LocalDateTime normalizedEnd = normalizeEnd(request.getEndDate());

        // Make sure booking period fits into property availability window
        if (normalizedStart.isBefore(property.getAvailableFrom()) || normalizedEnd.isAfter(property.getAvailableTo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested dates are outside property availability period");
        }

        // Prevent double booking (REQUESTED/APPROVED/ACTIVE block)
        boolean overlaps = bookingRepository.existsOverlappingBooking(
                property.getId(),
                normalizedStart,
                normalizedEnd,
                BLOCKING_STATUSES
        );
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property is already booked for the selected dates");
        }

        int totalPrice = calculateTotalPrice(normalizedStart, normalizedEnd, property.getPricePerDay());

        Booking booking = Booking.builder()
                .property(property)
                .tenant(tenant)
                .startDate(normalizedStart)
                .endDate(normalizedEnd)
                .status(BookingStatus.REQUESTED)
                .totalPrice(totalPrice)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("New booking created with ID: {}", savedBooking.getId());

        return mapToResponse(savedBooking);
    }

    private LocalDateTime normalizeStart(LocalDateTime start) {
        LocalDate d = start.toLocalDate();
        return d.atStartOfDay();
    }

    private LocalDateTime normalizeEnd(LocalDateTime end) {
        LocalDate d = end.toLocalDate();
        return d.atTime(LocalTime.MAX);
    }

    private int calculateTotalPrice(LocalDateTime start, LocalDateTime end, int pricePerDay) {
        long daysInclusive = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        return Math.toIntExact(daysInclusive * (long) pricePerDay);
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

    // Get bookings pending confirmation
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public List<BookingResponse> getPendingBookings(String username) {
        User owner = getCurrentOwner(username);

        return bookingRepository
                .findByPropertyOwnerIdAndStatus(owner.getId(), BookingStatus.REQUESTED)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Confirm the booking
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public BookingResponse approveBooking(String username, Long bookingId) {
        User owner = getCurrentOwner(username);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getProperty().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can approve only your bookings");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            log.warn("Attempt to approve booking with id {} failed. Current status: {}", bookingId, booking.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Booking cannot be approved because its status is " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.APPROVED);

        Property property = booking.getProperty();
        property.setStatus(PropertyStatus.BOOKED);
        propertyRepository.save(property);

        bookingRepository.save(booking);

        // Email sending
        BookingEmailRequest emailRequest = new BookingEmailRequest();
        emailRequest.setEmail(booking.getTenant().getEmail());
        emailRequest.setUsername(booking.getTenant().getUsername());
        emailRequest.setPropertyAddress(booking.getProperty().getAddress());
        emailRequest.setStartDate(booking.getStartDate().toLocalDate());
        emailRequest.setEndDate(booking.getEndDate().toLocalDate());
        emailRequest.setTotalPrice(booking.getTotalPrice());
        emailRequest.setConfirmUrl("https://your-app.com/bookings/" + booking.getId() + "/confirm");

        emailService.sendBookingApproved(emailRequest);

        return mapToResponse(booking);
    }

    // Reject the booking
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public BookingResponse rejectBooking(String username, Long bookingId) {
        User owner = getCurrentOwner(username);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getProperty().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can reject only your bookings");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            log.warn("Attempt to reject booking with id {} failed. Current status: {}", bookingId, booking.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Booking cannot be rejected because its status is " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.REJECTED);

        Property property = booking.getProperty();
        property.setStatus(PropertyStatus.AVAILABLE);
        propertyRepository.save(property);

        return mapToResponse(bookingRepository.save(booking));
    }

    // Helper method to retrieve the current owner
    private User getCurrentOwner(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isOwner = owner.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_OWNER);

        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an OWNER");
        }

        return owner;
    }

    @Transactional
    public void finishBooking(Booking booking) {
        booking.setStatus(BookingStatus.FINISHED);
        bookingRepository.save(booking);

        Property property = booking.getProperty();
        property.setStatus(PropertyStatus.AVAILABLE);
        propertyRepository.save(property);

        RentalFinishedEmailRequest emailRequest = new RentalFinishedEmailRequest();
        emailRequest.setEmail(booking.getTenant().getEmail());
        emailRequest.setUsername(booking.getTenant().getUsername());
        emailRequest.setPropertyAddress(booking.getProperty().getAddress());
        emailRequest.setStartDate(booking.getStartDate().toLocalDate());
        emailRequest.setEndDate(booking.getEndDate().toLocalDate());
        emailRequest.setTotalPrice(booking.getTotalPrice());

        try {
            emailService.sendRentalFinished(emailRequest);
            log.info("Booking ID {} marked as FINISHED and email sent to {}", booking.getId(), booking.getTenant().getEmail());
        } catch (Exception e) {
            log.error("Failed to send FINISHED email for booking ID {}", booking.getId(), e);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('OPERATOR')")
    public BookingResponse activateBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only APPROVED bookings can be activated. Current status: " + booking.getStatus());
        }

        if (!rentalContractService.hasContract(bookingId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot activate booking without uploaded rental contract");
        }

        booking.setStatus(BookingStatus.ACTIVE);
        Booking saved = bookingRepository.save(booking);

        Property property = saved.getProperty();
        // BOOKED -> RENTED when operator activates
        property.setStatus(PropertyStatus.RENTED);
        propertyRepository.save(property);

        return mapToResponse(saved);
    }
}
