package de.ait.homerent.booking.repository;

import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 02.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTenantId(Long tenantId);

    List<Booking> findByPropertyId(Long propertyId);

    List<Booking> findByStatus(BookingStatus status);

    //For searching by owner and status
    List<Booking> findByPropertyOwnerIdAndStatus(Long ownerId, BookingStatus status);

    /**
     * Checks whether there is at least one booking for the given property
     * whose date interval overlaps with [start, end] and whose status is in the given list.
     * <p>
     * Overlap rule (inclusive):
     * newStart <= existingEnd AND newEnd >= existingStart
     */
    @Query("""
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.property.id = :propertyId
            AND b.status IN :statuses
            AND :start < b.endDate
            AND :end > b.startDate
            """)
    boolean existsOverlappingBooking(
            @Param("propertyId") Long propertyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") Collection<BookingStatus> statuses
    );
}
