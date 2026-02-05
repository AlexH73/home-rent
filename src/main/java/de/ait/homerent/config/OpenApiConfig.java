package de.ait.homerent.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HomeRent API",
                version = "1.0.0",
                description = """
            ## HomeRent - Home Rental Platform
            
            ### Overview
            Backend service for a home rental platform. The system allows:
            - User registration and account management
            - Property listing for rent
            - Booking accommodations for selected dates
            - Rental contract management
            - Maintenance request tracking and management
            
            ### User Roles:
            - **ROLE_TENANT** - Tenant/Renter
            - **ROLE_OWNER** - Property Owner
            - **ROLE_OPERATOR** - Platform Operator
            - **ROLE_ADMIN** - System Administrator
            
            ### Technology Stack:
            - Java 17, Spring Boot 3.1.5
            - Spring Security, Spring Data JPA
            - H2 Database (development), PostgreSQL (production)
            - Liquibase for database migrations
            - OpenAPI 3.0 (Swagger) for documentation
            """,
                contact = @Contact(
                        name = "HomeRent Support",
                        email = "support@homerent.com",
                        url = "https://homerent.example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local development server"
                ),
                @Server(
                        url = "https://api.homerent.example.com",
                        description = "Production server"
                )
        }
)
public class OpenApiConfig {
}
