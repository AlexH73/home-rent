package de.ait.homerent;

import de.ait.homerent.user.repository.RoleRepository;
import de.ait.homerent.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 26.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 * Smoke test: verifies that Liquibase runs successfully against a real
 * PostgreSQL instance and that seed data (roles, users) is present.
 */
@Testcontainers
@SpringBootTest(properties = {
        "spring.profiles.active=prod",
        "spring.liquibase.contexts=dev,test",
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "spring.mail.username=test",
        "spring.mail.password=test",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "app.mail.from=no-reply@test.local",
        "app.upload.rental-contracts-dir=./uploads-test/rental-contracts",
        "app.upload.rental-contracts-max-size=15728640",
        "app.upload.properties-dir=./uploads-test/properties",
        "app.upload.property-max-size=5242880",
        "app.upload.issues-dir=./uploads-test/issues",
        "app.upload.issue-photo-max-size=5242880",
        "app.upload.issue-photo-allowed-types=image/jpeg,image/png,image/jpg"
})
class PostgresContainerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("homerent_smoke")
            .withUsername("homerent")
            .withPassword("homerent");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Liquibase applies schema and inserts seed roles against PostgreSQL")
    void liquibaseAppliedRoles() {
        assertThat(roleRepository.count()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Liquibase inserts seed users (admin, owner1, tenant1, operator1) against PostgreSQL")
    void liquibaseAppliedSeedUsers() {
        assertThat(userRepository.findByUsername("admin")).isPresent();
        assertThat(userRepository.findByUsername("owner1")).isPresent();
        assertThat(userRepository.findByUsername("tenant1")).isPresent();
        assertThat(userRepository.findByUsername("operator1")).isPresent();
    }
}
