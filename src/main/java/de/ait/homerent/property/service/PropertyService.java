package de.ait.homerent.property.service;

import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
public class PropertyService {

    private final PropertyRepository propertyRepository;

    // Get all properties for Admin
    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    // Delete property by ID
    @Transactional
    public void deleteById(Long id) {
        if (!propertyRepository.existsById(id)) {
            log.warn("Attempt to delete non-existing property with id: {}", id);
            throw new IllegalArgumentException("Property not found");
        }
        propertyRepository.deleteById(id);
        log.info("Property with id {} was deleted by admin", id);
    }
}
