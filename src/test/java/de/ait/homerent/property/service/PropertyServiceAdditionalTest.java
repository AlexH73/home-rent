package de.ait.homerent.property.service;

import de.ait.homerent.property.dto.PropertyDto;
import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.model.PropertyPhoto;
import de.ait.homerent.property.model.PropertyStatus;
import de.ait.homerent.property.repository.PropertyPhotoRepository;
import de.ait.homerent.property.repository.PropertyRepository;
import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import de.ait.homerent.utils.CurrentUserHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyService additional unit tests")
class PropertyServiceAdditionalTest {

    @Mock PropertyRepository propertyRepository;
    @Mock UserRepository userRepository;
    @Mock PropertyPhotoRepository propertyPhotoRepository;
    @Mock PropertyFileStorageService propertyFileStorageService;
    @Mock CurrentUserHelper currentUserHelper;

    @InjectMocks PropertyService propertyService;

    @Test
    @DisplayName("findAll(): maps entities to DTOs")
    void findAll_mapsToDtos() {
        Property p = property(1L, owner(10L));
        when(propertyRepository.findAll()).thenReturn(List.of(p));

        List<PropertyDto> res = propertyService.findAll();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(1L);
        assertThat(res.get(0).getOwnerId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("findAvailable(): returns only AVAILABLE properties")
    void findAvailable_mapsToDtos() {
        Property p = property(1L, owner(10L));
        p.setStatus(PropertyStatus.AVAILABLE);

        when(propertyRepository.findByStatus(PropertyStatus.AVAILABLE)).thenReturn(List.of(p));

        List<PropertyDto> res = propertyService.findAvailable(null, null);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
    }

    @Test
    @DisplayName("getMyProperties(): fetches owner via CurrentUserHelper and returns mapped list")
    void getMyProperties_returnsOwnerProperties() {
        User owner = owner(10L);

        Property p = property(1L, owner);

        when(currentUserHelper.getCurrentOwner("owner1")).thenReturn(owner);
        when(propertyRepository.findByOwnerId(10L)).thenReturn(List.of(p));

        List<PropertyDto> res = propertyService.getMyProperties("owner1");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getOwnerId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("updateProperty(): when dto has both dates and from>to, throws 400 BAD_REQUEST")
    void updateProperty_whenInvalidDates_throws400() {
        Property existing = property(1L, owner(10L));
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(existing));

        PropertyDto dto = new PropertyDto();
        dto.setAvailableFrom(LocalDateTime.of(2026, 3, 5, 0, 0));
        dto.setAvailableTo(LocalDateTime.of(2026, 3, 4, 0, 0));

        ResponseStatusException ex = catchThrowableOfType(
                () -> propertyService.updateProperty(1L, dto),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).contains("availableFrom must be before or equal to availableTo");
    }

    @Test
    @DisplayName("updateProperty(): when only one date is provided, skips date validation and updates fields")
    void updateProperty_whenOnlyOneDateProvided_updatesWithoutDateValidation() {
        Property existing = property(1L, owner(10L));
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        PropertyDto dto = new PropertyDto();
        dto.setTitle("New title");
        dto.setAddress("New addr");
        dto.setDescription("New desc");
        dto.setPricePerDay(200);
        dto.setStatus(PropertyStatus.BOOKED);
        dto.setAvailableFrom(LocalDateTime.of(2026, 3, 1, 0, 0));
        dto.setAvailableTo(null); // important branch

        PropertyDto res = propertyService.updateProperty(1L, dto);

        assertThat(res.getTitle()).isEqualTo("New title");
        assertThat(res.getStatus()).isEqualTo(PropertyStatus.BOOKED);
    }

    @Test
    @DisplayName("deleteById(): when property has photos, deletes photo files before deleting entity")
    void deleteById_deletesFilesWhenPhotosPresent() {
        PropertyPhoto ph1 = PropertyPhoto.builder().filePath("p1").build();
        PropertyPhoto ph2 = PropertyPhoto.builder().filePath("p2").build();

        Property p = property(1L, owner(10L));
        p.setPhotos(List.of(ph1, ph2));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(p));

        propertyService.deleteById(1L);

        verify(propertyFileStorageService).deleteFile("p1");
        verify(propertyFileStorageService).deleteFile("p2");
        verify(propertyRepository).delete(p);
    }

    @Test
    @DisplayName("findById(): maps photo URLs and filters null URLs")
    void findById_mapsPhotoUrls_filtersNulls() {
        PropertyPhoto withUrl = mock(PropertyPhoto.class);
        when(withUrl.getUrl()).thenReturn("http://x/1");

        PropertyPhoto nullUrl = mock(PropertyPhoto.class);
        when(nullUrl.getUrl()).thenReturn(null);

        Property p = property(1L, owner(10L));
        p.setPhotos(List.of(withUrl, nullUrl));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(p));

        PropertyDto dto = propertyService.findById(1L);

        assertThat(dto.getPhotoUrls()).containsExactly("http://x/1");
    }

    private static Property property(Long id, User owner) {
        Property p = new Property();
        p.setId(id);
        p.setOwner(owner);
        p.setTitle("t");
        p.setAddress("a");
        p.setDescription("d");
        p.setPricePerDay(100);
        p.setStatus(PropertyStatus.AVAILABLE);
        p.setAvailableFrom(LocalDateTime.of(2026, 1, 1, 0, 0));
        p.setAvailableTo(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        return p;
    }

    private static User owner(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("owner");
        u.setRoles(Set.of(role(RoleName.ROLE_OWNER)));
        return u;
    }

    private static Role role(RoleName name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }
}
