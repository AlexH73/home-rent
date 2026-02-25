package de.ait.homerent.property.service;

import de.ait.homerent.property.dto.PropertyCreateRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyService unit tests")
class PropertyServiceTest {

    @Mock PropertyRepository propertyRepository;
    @Mock UserRepository userRepository;
    @Mock PropertyPhotoRepository propertyPhotoRepository;
    @Mock PropertyFileStorageService propertyFileStorageService;
    @Mock CurrentUserHelper currentUserHelper;

    @InjectMocks PropertyService propertyService;

    @Test
    @DisplayName("findById(): when property not found, throws 404 NOT_FOUND")
    void findById_whenNotFound_throws404() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.findById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("save(): when owner not found, throws 404 NOT_FOUND")
    void save_whenOwnerNotFound_throws404() {
        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setOwnerId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.save(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("save(): when user is not OWNER, throws 403 FORBIDDEN")
    void save_whenUserIsNotOwnerRole_throws403() {
        User notOwner = new User();
        notOwner.setId(10L);
        notOwner.setRoles(Set.of(role(RoleName.ROLE_TENANT)));

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setOwnerId(10L);
        req.setTitle("t");
        req.setAddress("a");
        req.setDescription("d");
        req.setPricePerDay(100);
        req.setAvailableFrom(LocalDateTime.now().plusDays(1));
        req.setAvailableTo(LocalDateTime.now().plusDays(2));

        when(userRepository.findById(10L)).thenReturn(Optional.of(notOwner));

        assertThatThrownBy(() -> propertyService.save(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("save(): when availableFrom is after availableTo, throws 400 BAD_REQUEST")
    void save_whenDatesInvalid_throws400() {
        User owner = new User();
        owner.setId(10L);
        owner.setRoles(Set.of(role(RoleName.ROLE_OWNER)));

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setOwnerId(10L);
        req.setTitle("t");
        req.setAddress("a");
        req.setDescription("d");
        req.setPricePerDay(100);
        req.setAvailableFrom(LocalDateTime.now().plusDays(3));
        req.setAvailableTo(LocalDateTime.now().plusDays(2));

        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> propertyService.save(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("createProperty(): when files list is null, does not store photos and returns DTO")
    void createProperty_whenFilesNull_doesNotStorePhotos() {
        User owner = ownerWithId(10L);

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setTitle("t");
        req.setAddress("a");
        req.setDescription("d");
        req.setPricePerDay(100);
        req.setAvailableFrom(LocalDateTime.now().plusDays(1));
        req.setAvailableTo(LocalDateTime.now().plusDays(2));

        when(currentUserHelper.getCurrentOwner("owner1")).thenReturn(owner);
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> {
            Property p = inv.getArgument(0);
            p.setId(77L);
            return p;
        });

        PropertyDto dto = propertyService.createProperty("owner1", req, null);

        assertThat(dto.getId()).isEqualTo(77L);
        verify(propertyPhotoRepository, never()).saveAll(any());
        verify(propertyFileStorageService, never()).storeFile(anyLong(), any());
    }

    @Test
    @DisplayName("createProperty(): when files provided, stores files and persists photos")
    void createProperty_whenFilesProvided_storesFilesAndSavesPhotos() {
        User owner = ownerWithId(10L);

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setTitle("t");
        req.setAddress("a");
        req.setDescription("d");
        req.setPricePerDay(100);
        req.setAvailableFrom(LocalDateTime.now().plusDays(1));
        req.setAvailableTo(LocalDateTime.now().plusDays(2));

        MockMultipartFile f1 = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("file", "b.png", "image/png", "y".getBytes());

        when(currentUserHelper.getCurrentOwner("owner1")).thenReturn(owner);
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> {
            Property p = inv.getArgument(0);
            p.setId(77L);
            return p;
        });

        when(propertyFileStorageService.storeFile(eq(77L), any())).thenReturn("path-1", "path-2");

        PropertyDto dto = propertyService.createProperty("owner1", req, List.of(f1, f2));

        assertThat(dto.getId()).isEqualTo(77L);

        verify(propertyFileStorageService, times(2)).storeFile(eq(77L), any());

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Iterable<PropertyPhoto>> captor = (ArgumentCaptor) ArgumentCaptor.forClass(Iterable.class);

        verify(propertyPhotoRepository).saveAll(captor.capture());

        int count = 0;
        for (PropertyPhoto ignored : captor.getValue()) {
            count++;
        }
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteProperty(): when property not found, throws 404 NOT_FOUND")
    void deleteProperty_whenPropertyNotFound_throws404() {
        User owner = ownerWithId(10L);

        when(currentUserHelper.getCurrentOwner("owner1")).thenReturn(owner);
        when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.deleteProperty("owner1", 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("deleteProperty(): when property belongs to another owner, throws 403 FORBIDDEN")
    void deleteProperty_whenNotOwner_throws403() {
        User owner = ownerWithId(10L);

        User anotherOwner = new User();
        anotherOwner.setId(11L);

        Property property = new Property();
        property.setId(99L);
        property.setOwner(anotherOwner);

        when(currentUserHelper.getCurrentOwner("owner1")).thenReturn(owner);
        when(propertyRepository.findById(99L)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> propertyService.deleteProperty("owner1", 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("deleteById(): deletes associated files and then deletes property")
    void deleteById_deletesFilesAndEntity() {
        PropertyPhoto photo1 = new PropertyPhoto();
        photo1.setFilePath("p1");

        PropertyPhoto photo2 = new PropertyPhoto();
        photo2.setFilePath("p2");

        User owner = ownerWithId(10L);

        Property property = new Property();
        property.setId(1L);
        property.setOwner(owner);
        property.setStatus(PropertyStatus.AVAILABLE);
        property.setPhotos(List.of(photo1, photo2));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        propertyService.deleteById(1L);

        verify(propertyFileStorageService).deleteFile("p1");
        verify(propertyFileStorageService).deleteFile("p2");
        verify(propertyRepository).delete(property);
    }

    private static Role role(RoleName name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }

    private static User ownerWithId(long id) {
        User u = new User();
        u.setId(id);
        u.setRoles(Set.of(role(RoleName.ROLE_OWNER)));
        return u;
    }
}
