package de.ait.homerent.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 26.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Configuration
public class DevStartupLogger {

    @Bean
    @Profile("dev")
    public CommandLineRunner logDevInfo() {
        return args -> {

            System.out.println("###########################################");
            System.out.println("HomeRent Application started successfully!");
            System.out.println("###########################################");
            System.out.println("Swagger UI available at: http://localhost:8080/swagger-ui.html");
            System.out.println("H2 Console available at: http://localhost:8080/h2-console");
            System.out.println("JDBC URL: jdbc:h2:mem:homerent_dev");
            System.out.println("Username: sa");
            System.out.println("Password: (empty)");
        };
    }
}
