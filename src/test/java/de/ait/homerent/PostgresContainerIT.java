package de.ait.homerent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Testcontainers
@SpringBootTest(classes = HomeRentApplication.class)
@ActiveProfiles("tc")
@DisplayName("PostgreSQL Testcontainers smoke test (Liquibase + seeded test data)")
class PostgresContainerIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("homerent")
            .withUsername("homerent")
            .withPassword("homerent");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Liquibase applied schema and inserted reference + test data")
    void liquibaseApplied_andSeededDataExists() {
        // Show what actually exists (helps debug Liquibase contexts / migrations)
        var roleNames = jdbcTemplate.queryForList("select name from roles order by name", String.class);
        System.out.println("roles in DB = " + roleNames);

        assertThat(roleNames).contains("ROLE_TENANT", "ROLE_OWNER", "ROLE_OPERATOR", "ROLE_ADMIN");

        Integer users = jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
        Integer properties = jdbcTemplate.queryForObject("select count(*) from properties", Integer.class);
        Integer bookings = jdbcTemplate.queryForObject("select count(*) from bookings", Integer.class);

        assertThat(users).isNotNull().isGreaterThanOrEqualTo(4);
        assertThat(properties).isNotNull().isGreaterThanOrEqualTo(2);
        assertThat(bookings).isNotNull().isGreaterThanOrEqualTo(5);
    }
}
