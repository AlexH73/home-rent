package de.ait.homerent.utils;

import de.ait.homerent.user.model.RoleName;
import de.ait.homerent.user.model.User;
import de.ait.homerent.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 22.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserHelper {

    private final UserRepository userRepository;

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User getCurrentOwner(String username) {
        User user = getCurrentUser(username);
        boolean isOwner = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_OWNER);
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an OWNER");
        }
        return user;
    }
}
