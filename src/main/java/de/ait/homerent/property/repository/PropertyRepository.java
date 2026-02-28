package de.ait.homerent.property.repository;

import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 02.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // All objects of a specific owner
    List<Property> findByOwnerId(Long ownerId);

    // All properties with the specified status
    List<Property> findByStatus(PropertyStatus status);

    // Available during the period
    @Query("""
                SELECT p FROM Property p
                WHERE p.status = :status
                  AND p.availableFrom <= :start
                  AND p.availableTo >= :end
            """)
    List<Property> findAvailableInPeriod(
            @Param("status") PropertyStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT p FROM Property p
            WHERE p.status = 'AVAILABLE'
            AND NOT EXISTS (
                SELECT b FROM Booking b
                WHERE b.property = p
                AND b.status IN ('APPROVED', 'ACTIVE')
                AND (
                    (:startDate IS NULL OR b.endDate > :startDate)
                    AND
                    (:endDate IS NULL OR b.startDate < :endDate)
                )
            )
            """)
    List<Property> findAvailable(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM Property p JOIN FETCH p.owner WHERE p.id = :id")
    Optional<Property> findByIdWithOwner(@Param("id") Long id);
}
