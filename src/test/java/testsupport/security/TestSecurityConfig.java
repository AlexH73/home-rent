package testsupport.security;

import de.ait.homerent.booking.service.BookingService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Auth‑Endpoints IMMER erlauben
                        .requestMatchers("/api/auth/**").permitAll()

                        // Operator‑Endpoints schützen
                        .requestMatchers("/api/operator/**").hasRole("OPERATOR")

                        // Tenant‑Endpoints schützen
                        .requestMatchers("/api/tenant/**").hasRole("TENANT")

                        // Owner‑Endpoints schützen
                        .requestMatchers("/api/owner/**").hasRole("OWNER")

                        // Admin‑Endpoints schützen
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // alles andere muss authentifiziert sein
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {});

        return http.build();
    }

    /**
     * Nur für Tests, die BookingService brauchen.
     * Andere Tests ignorieren diese Bean einfach.
     */
    @Bean
    public BookingService bookingService() {
        return Mockito.mock(BookingService.class);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("operator").password("{noop}pass").roles("OPERATOR").build(),
                User.withUsername("tenant").password("{noop}pass").roles("TENANT").build(),
                User.withUsername("owner").password("{noop}pass").roles("OWNER").build(),
                User.withUsername("admin").password("{noop}pass").roles("ADMIN").build()
        );
    }
}












