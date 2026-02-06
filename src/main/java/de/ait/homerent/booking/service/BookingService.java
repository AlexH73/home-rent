package de.ait.homerent.booking.service;

import de.ait.homerent.booking.dto.BookingResponse;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    public List<BookingResponse> getActiveBookings() {
        List<Booking> activeBookings = bookingRepository.findByStatus(BookingStatus.ACTIVE);

        return activeBookings.stream()
                .map(booking -> {
                    BookingResponse response = new BookingResponse();
                    response.setId(booking.getId());
                    response.setPropertyTitle(booking.getProperty().getTitle());
                    response.setTenantName(booking.getTenant().getUsername());
                    response.setStartDate(booking.getStartDate());
                    response.setEndDate(booking.getEndDate());
                    response.setTotalPrice(booking.getTotalPrice());
                    response.setStatus(booking.getStatus().name());
                    return response;
                })
                .collect(Collectors.toList());
    }
}
