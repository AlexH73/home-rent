package de.ait.homerent.property.service;

import de.ait.homerent.property.dto.PropertyCreateRequest;
import de.ait.homerent.property.dto.PropertyDto;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyPhoto;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final PropertyFileStorageService propertyFileStorageService;

    @Transactional(readOnly = true)
    public List<PropertyDto> findAll() {
        log.info("Admin requested all properties");

        List<Property> properties = propertyRepository.findAll();
        if (properties.isEmpty()) {
            log.info("No properties found in the database");
        }

        return properties.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public PropertyDto save(PropertyCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + request.getOwnerId()));

        boolean isOwner = owner.getRoles().stream()
                .map(role -> role.getName())
                .anyMatch(RoleName.ROLE_OWNER::equals);

        if (!isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not an OWNER"
            );
        }

        if (request.getAvailableFrom().isAfter(request.getAvailableTo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "availableFrom must be before availableTo"
            );
        }

        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(request.getTitle());
        property.setAddress(request.getAddress());
        property.setDescription(request.getDescription());
        property.setPricePerDay(request.getPricePerDay());
        property.setStatus(PropertyStatus.AVAILABLE);
        property.setAvailableFrom(request.getAvailableFrom());
        property.setAvailableTo(request.getAvailableTo());

        Property savedProperty = propertyRepository.save(property);
        log.info("Admin created new property with id: {}", savedProperty.getId());
        return mapToDto(savedProperty);
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

    @Transactional(readOnly = true)
    public List<PropertyDto> findAvailable() {
        log.info("Tenant requested available properties");
        return propertyRepository.findByStatus(PropertyStatus.AVAILABLE).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyDto findById(Long id) {
        log.info("Requesting property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        return mapToDto(property);
    }

    private PropertyDto mapToDto(Property property) {
        PropertyDto dto = new PropertyDto();
        dto.setId(property.getId());
       // dto.setOwnerId(property.getOwner().getId());
        dto.setTitle(property.getTitle());
        dto.setAddress(property.getAddress());
        dto.setDescription(property.getDescription());
        dto.setPricePerDay(property.getPricePerDay());
        dto.setStatus(property.getStatus());
        dto.setAvailableFrom(property.getAvailableFrom());
        dto.setAvailableTo(property.getAvailableTo());
        dto.setCreatedAt(property.getCreatedAt());
        // map photos to URLs (optional)
        if (property.getPhotos() != null && !property.getPhotos().isEmpty()) {
            List<String> photoUrls = property.getPhotos().stream()
                    .map(PropertyPhoto::getUrl)
                    .filter(url -> url != null)
                    .toList();
            dto.setPhotoUrls(photoUrls);
        }

        return dto;
    }

    // Get all properties of the current owner
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('OWNER')")
    public List<PropertyDto> getMyProperties(String username) {
        User owner = getCurrentOwner(username);
        return propertyRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // Create a new property
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public PropertyDto createProperty(
            String username,
            PropertyCreateRequest request,
            List<MultipartFile> files // новые загружаемые файлы
    ) {
        User owner = getCurrentOwner(username);

        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(request.getTitle());
        property.setAddress(request.getAddress());
        property.setDescription(request.getDescription());
        property.setPricePerDay(request.getPricePerDay());
        property.setStatus(PropertyStatus.AVAILABLE);
        property.setAvailableFrom(request.getAvailableFrom());
        property.setAvailableTo(request.getAvailableTo());

        Property savedProperty = propertyRepository.save(property);

        // ------------------ сохраняем файлы ------------------
        if (files != null && !files.isEmpty()) {
            List<PropertyPhoto> photos = new ArrayList<>();
            for (MultipartFile file : files) {
                String path = propertyFileStorageService.storeFile(savedProperty.getId(), file);

                PropertyPhoto photo = new PropertyPhoto();
                photo.setFilePath(path);
                photo.setFileName(file.getOriginalFilename());
                photo.setProperty(savedProperty);
                photo.setUploadedAt(LocalDateTime.now());

                photos.add(photo);
            }
            savedProperty.setPhotos(photos);
            propertyRepository.save(savedProperty); // обновляем с файлами
        }

        return mapToDto(savedProperty);
    }

    // Delete a property
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public boolean deleteProperty(String username, Long propertyId) {
        User owner = getCurrentOwner(username);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can delete only your properties");
        }

        propertyRepository.delete(property);
        return true;
    }

    // Helper method to get the current owner
    private User getCurrentOwner(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isOwner = owner.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_OWNER);

        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an OWNER");
        }

        return owner;
    }
}