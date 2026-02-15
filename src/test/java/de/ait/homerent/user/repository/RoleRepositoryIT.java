package de.ait.homerent.user.repository;

import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 15.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Integration tests for RoleRepository")
class RoleRepositoryIT {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should find role by name")
    void findByName() {
        Optional<Role> found = roleRepository.findByName(RoleName.ROLE_ADMIN);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(RoleName.ROLE_ADMIN);
    }
}
