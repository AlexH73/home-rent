package de.ait.homerent.property.repository;

import de.ait.homerent.property.enums.PropertyStatus;
import de.ait.homerent.property.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

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

    // Все объекты конкретного владельца
    List<Property> findByOwnerId(Long ownerId);

    // Только доступные
    List<Property> findByStatus(PropertyStatus status);

    // Доступные в период
    List<Property> findByStatusAndAvailableBetween(
            PropertyStatus status,
            LocalDate availableFrom,
            LocalDate availableTo
    );
}
