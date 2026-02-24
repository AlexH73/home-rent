package de.ait.homerent.auth.service;

import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.core.userdetails.User.builder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Custom UserDetailsService implementation for loading user details from database.
 * Required by Spring Security for authentication.
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        log.debug("Found user: {} with roles: {}", username, user.getRoles());


        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .toList();

        return builder()
                .username(user.getUsername())
                .password(user.getPassword())

                // Indicates whether the user account is enabled or disabled.
                // Disabled accounts cannot authenticate.
                .disabled(!user.isEnabled())

                // Indicates whether the user account has expired.
                // Expired accounts cannot authenticate.
                .accountExpired(false)

                // Indicates whether the user account is locked.
                // Locked accounts cannot authenticate (e.g., after multiple failed login attempts).
                .accountLocked(false)

                // Indicates whether the user credentials (password) have expired.
                // Users with expired credentials must reset their password before logging in.
                .credentialsExpired(false)

                .authorities(authorities)
                .build();
    }
}
