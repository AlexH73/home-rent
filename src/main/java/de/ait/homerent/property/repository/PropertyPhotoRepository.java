package de.ait.homerent.property.repository;

import de.ait.homerent.property.model.PropertyPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 02.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, Long> {

    // Get all photos for a specific property
    List<PropertyPhoto> findByPropertyId(Long propertyId);

    // Delete all photos when the property is deleted
    void deleteByPropertyId(Long propertyId);

}
