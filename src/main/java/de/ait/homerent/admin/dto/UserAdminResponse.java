package de.ait.homerent.admin.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserAdminResponse {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
}

