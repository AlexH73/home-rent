package de.ait.homerent.booking.repository;
import de.ait.homerent.booking.model.Booking;
import de.ait.homerent.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}
