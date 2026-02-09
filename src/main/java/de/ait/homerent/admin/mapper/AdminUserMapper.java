package de.ait.homerent.admin.mapper;

import de.ait.homerent.admin.dto.UserAdminResponse;
import de.ait.homerent.user.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AdminUserMapper {

    public UserAdminResponse toResponse(User user) {
        UserAdminResponse dto = new UserAdminResponse();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );

        return dto;
    }
}

