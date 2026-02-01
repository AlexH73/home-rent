package de.ait.homerent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 31.01.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF only for H2 Console
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )

                // Allow H2 Console without authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Allow frames for H2 Console while keeping clickjacking protection
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // Enable default form login
                .formLogin(Customizer.withDefaults());

        return http.build();
    }
}

