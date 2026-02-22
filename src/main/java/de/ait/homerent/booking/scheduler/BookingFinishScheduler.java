package de.ait.homerent.booking.scheduler;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 20.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingFinishScheduler {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Scheduled(fixedRate = 300000) // every 5 minutes
    @Transactional
    public void finishExpiredBookings() {
        log.info("Starting the task of completing expired bookings");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> activeBookings = bookingRepository.findByStatus(BookingStatus.ACTIVE);
        log.info ("Found {} active bookings", activeBookings.size());

        int finishedCount = 0;
        for (Booking booking : activeBookings) {
            // Completing bookings that have an end date <= now
            if (booking.getEndDate().isBefore(now) || booking.getEndDate().isEqual(now)) {
                log.info ("Completing the booking ID: {}", booking.getId());
                bookingService.finishBooking(booking);
                finishedCount++;
            }
        }

        log.info ("Bookings completed {}", finishedCount);
        log.info ("The task of completing expired bookings is completed");
    }
}