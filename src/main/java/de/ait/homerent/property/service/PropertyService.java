package de.ait.homerent.property.service;

import de.ait.homerent.property.dto.PropertyCreateRequest;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Property> findAll() {
        log.info("Admin requested all properties");
        return propertyRepository.findAll();
    }

    @Transactional
    public Property save(PropertyCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + request.getOwnerId()));

        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(request.getTitle());
        property.setAddress(request.getAddress());
        property.setDescription(request.getDescription());
        property.setPricePerDay(request.getPricePerDay());
        property.setStatus(request.getStatus());
        property.setAvailableFrom(request.getAvailableFrom());
        property.setAvailableTo(request.getAvailableTo());

        Property savedProperty = propertyRepository.save(property);
        log.info("Admin created new property with id: {}", savedProperty.getId());
        return savedProperty;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!propertyRepository.existsById(id)) {
            log.warn("Attempt to delete non-existing property with id: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with id: " + id);
        }

        propertyRepository.deleteById(id);
        log.info("Property with id {} was successfully deleted by admin", id);
    }
}