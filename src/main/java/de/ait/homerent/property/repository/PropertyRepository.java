package de.ait.homerent.property.repository;

import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

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
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
