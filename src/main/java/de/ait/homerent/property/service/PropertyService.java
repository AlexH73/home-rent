package de.ait.homerent.property.service;

import de.ait.homerent.property.dto.PropertyCreateRequest;
import de.ait.homerent.property.dto.PropertyDto;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyPhoto;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyPhotoRepository;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import de.ait.homerent.utils.CurrentUserHelper;
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
    private final PropertyPhotoRepository propertyPhotoRepository;
    private final PropertyFileStorageService propertyFileStorageService;
    private final CurrentUserHelper currentUserHelper;

    @Transactional(readOnly = true)
    public List<PropertyDto> findAll() {
        log.info("Fetching all properties");
        return propertyRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PropertyDto> findAvailable() {
        log.info("Fetching available properties");
        return propertyRepository.findByStatus(PropertyStatus.AVAILABLE).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyDto findById(Long id) {
        log.info("Fetching property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        return mapToDto(property);
    }

    @Transactional
    public PropertyDto save(PropertyCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Owner not found with id: " + request.getOwnerId()));

        checkOwnerRole(owner);
        validateDates(request.getAvailableFrom(), request.getAvailableTo());

        Property property = buildProperty(owner, request);
        Property savedProperty = propertyRepository.save(property);
        log.info("Admin created new property with id: {}", savedProperty.getId());
        return mapToDto(savedProperty);
    }

    @Transactional
    public void deleteById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Property not found with id: " + id));
        deletePropertyFiles(property);

        propertyRepository.delete(property);
        log.info("Property with id {} was successfully deleted by admin", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('OWNER')")
    public List<PropertyDto> getMyProperties(String username) {
        User owner = currentUserHelper.getCurrentOwner(username);
        return propertyRepository.findByOwnerId(owner.getId()).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public PropertyDto createProperty(String username,
                                      PropertyCreateRequest request,
                                      List<MultipartFile> files) {
        User owner = currentUserHelper.getCurrentOwner(username);
        validateDates(request.getAvailableFrom(), request.getAvailableTo());

        Property property = buildProperty(owner, request);
        Property savedProperty = propertyRepository.save(property);

        if (files != null && !files.isEmpty()) {
            savePropertyPhotos(savedProperty, files);
        }

        return mapToDto(savedProperty);
    }

    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public boolean deleteProperty(String username, Long propertyId) {
        User owner = currentUserHelper.getCurrentOwner(username);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can delete only your own properties");
        }

        deletePropertyFiles(property);
        propertyRepository.delete(property);
        return true;
    }

    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public PropertyDto updateProperty(Long id, PropertyDto dto) {
        log.info("Updating property with id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (dto.getAvailableFrom() != null && dto.getAvailableTo() != null) {
            validateDates(dto.getAvailableFrom(), dto.getAvailableTo());
        }

        property.setTitle(dto.getTitle());
        property.setAddress(dto.getAddress());
        property.setDescription(dto.getDescription());
        property.setPricePerDay(dto.getPricePerDay());
        property.setStatus(dto.getStatus());
        property.setAvailableFrom(dto.getAvailableFrom());
        property.setAvailableTo(dto.getAvailableTo());

        Property updated = propertyRepository.save(property);
        log.info("Property updated successfully with id: {}", updated.getId());
        return mapToDto(updated);
    }

    private Property buildProperty(User owner, PropertyCreateRequest request) {
        return Property.builder()
                .owner(owner)
                .title(request.getTitle())
                .address(request.getAddress())
                .description(request.getDescription())
                .pricePerDay(request.getPricePerDay())
                .status(PropertyStatus.AVAILABLE)
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .build();
    }

    private void validateDates(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "availableFrom must be before or equal to availableTo");
        }
    }

    private void checkOwnerRole(User user) {
        boolean isOwner = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_OWNER);
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an OWNER");
        }
    }

    private void savePropertyPhotos(Property property, List<MultipartFile> files) {
        List<PropertyPhoto> photos = new ArrayList<>();
        for (MultipartFile file : files) {
            String path = propertyFileStorageService.storeFile(property.getId(), file);
            PropertyPhoto photo = PropertyPhoto.builder()
                    .filePath(path)
                    .fileName(file.getOriginalFilename())
                    .property(property)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            photos.add(photo);
        }
        propertyPhotoRepository.saveAll(photos);
    }

    private void deletePropertyFiles(Property property) {
        if (property.getPhotos() != null && !property.getPhotos().isEmpty()) {
            for (PropertyPhoto photo : property.getPhotos()) {
                propertyFileStorageService.deleteFile(photo.getFilePath());
            }
        }
    }

    private PropertyDto mapToDto(Property property) {
        PropertyDto dto = new PropertyDto();
        dto.setId(property.getId());
        dto.setOwnerId(property.getOwner().getId());
        dto.setTitle(property.getTitle());
        dto.setAddress(property.getAddress());
        dto.setDescription(property.getDescription());
        dto.setPricePerDay(property.getPricePerDay());
        dto.setStatus(property.getStatus());
        dto.setAvailableFrom(property.getAvailableFrom());
        dto.setAvailableTo(property.getAvailableTo());
        dto.setCreatedAt(property.getCreatedAt());

        if (property.getPhotos() != null && !property.getPhotos().isEmpty()) {
            List<String> photoUrls = property.getPhotos().stream()
                    .map(PropertyPhoto::getUrl)   // предполагается, что в PropertyPhoto есть метод getUrl()
                    .filter(url -> url != null)
                    .toList();
            dto.setPhotoUrls(photoUrls);
        }

        return dto;
    }
}