package de.ait.homerent.booking.service;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<BookingResponse> getActiveBookings() {
        log.info("Fetching all active bookings from database");

        List<Booking> activeBookings = bookingRepository.findByStatus(BookingStatus.ACTIVE);

        return activeBookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
