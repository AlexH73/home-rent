package de.ait.homerent.security;

import de.ait.homerent.property.model.Property;
import de.ait.homerent.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 14.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Component("propertySecurity")
@RequiredArgsConstructor
public class PropertySecurity {

    private final PropertyRepository propertyRepository;

    /**
     * Verifies that the current user is the owner of the property.
     */
    public boolean isOwner(Long propertyId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String currentUsername = authentication.getName();
        Property property = propertyRepository.findByIdWithOwner(propertyId).orElse(null);
        if (property == null) {
            return false;
        }
        return property.getOwner().getUsername().equals(currentUsername);
    }
}
