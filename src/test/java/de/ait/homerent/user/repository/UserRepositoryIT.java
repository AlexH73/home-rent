package de.ait.homerent.user.repository;

import de.ait.homerent.user.model.Role;
import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 15.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(BCryptPasswordEncoder.class)
@DisplayName("Integration tests for UserRepository")
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save user with existing roles")
    void saveUserWithRoles() {
        // given
        Role tenantRole = roleRepository.findByName(RoleName.ROLE_TENANT).orElseThrow();
        Role ownerRole = roleRepository.findByName(RoleName.ROLE_OWNER).orElseThrow();

        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("rawPassword")
                .enabled(true)
                .roles(Set.of(tenantRole, ownerRole))
                .build();

        // when
        User saved = userRepository.save(user);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("john_doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getRoles()).hasSize(2);
        assertThat(saved.getRoles()).extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.ROLE_TENANT, RoleName.ROLE_OWNER);
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername() {
        // given
        User user = User.builder()
                .username("unique_user")
                .email("unique@example.com")
                .password("pass")
                .enabled(true)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        var found = userRepository.findByUsername("unique_user");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("unique_user");
        assertThat(found.get().getEmail()).isEqualTo("unique@example.com");
    }

    @Test
    @DisplayName("Should return true when username exists")
    void existsByUsername() {
        // given
        User user = User.builder()
                .username("existing")
                .email("exist@example.com")
                .password("pass")
                .enabled(true)
                .build();
        entityManager.persist(user);

        // when
        boolean exists = userRepository.existsByUsername("existing");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void notExistsByUsername() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail() {
        // given
        User user = User.builder()
                .username("test")
                .email("test@example.com")
                .password("pass")
                .enabled(true)
                .build();
        entityManager.persist(user);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void notExistsByEmail() {
        boolean exists = userRepository.existsByEmail("missing@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should enforce unique username constraint")
    void uniqueUsername() {
        // given
        User user1 = User.builder()
                .username("same")
                .email("user1@example.com")
                .password("pass")
                .enabled(true)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .username("same")
                .email("user2@example.com")
                .password("pass")
                .enabled(true)
                .build();

        // when / then
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(user2);
            userRepository.flush();
        });
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void uniqueEmail() {
        // given
        User user1 = User.builder()
                .username("user1")
                .email("same@example.com")
                .password("pass")
                .enabled(true)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .username("user2")
                .email("same@example.com")
                .password("pass")
                .enabled(true)
                .build();

        // when / then
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(user2);
            userRepository.flush();
        });
    }
}
