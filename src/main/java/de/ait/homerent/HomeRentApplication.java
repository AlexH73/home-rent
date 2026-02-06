package de.ait.homerent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomeRentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeRentApplication.class, args);

        System.out.println("###########################################");
        System.out.println("HomeRent Application started successfully!");
        System.out.println("###########################################");
        System.out.println("Swagger UI available at: http://localhost:8080/swagger-ui.html");
        System.out.println("H2 Console available at: http://localhost:8080/h2-console");
        System.out.println("JDBC URL: jdbc:h2:mem:homerent_dev");
        System.out.println("Username: sa");
        System.out.println("Password: (empty)");
    }

}
