package de.ait.homerent.booking.scheduler;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import de.ait.homerent.booking.repository.BookingRepository;
import de.ait.homerent.booking.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
//@RequiredArgsConstructor
public class BookingFinishScheduler {
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public BookingFinishScheduler(BookingRepository bookingRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    @Scheduled(cron = "0 0 0,12 * * *") // Every 12 hours at midnight and noon

    @Transactional
    public void finishExpiredBookings() {
        log.info("Starting finishExpiredBookings task");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> activeBookings = bookingRepository.findByStatus(BookingStatus.ACTIVE);
        log.info("Found {} active bookings", activeBookings.size());

        int finishedCount = 0;

        for (Booking booking : activeBookings) {
            if (!booking.getEndDate().isAfter(now)) {
                log.info("Finishing booking with ID {}", booking.getId());
                bookingService.finishBooking(booking);
                finishedCount++;
            }
        }

        log.info("Finished {} bookings", finishedCount);
        log.info("finishExpiredBookings task completed");
    }
}