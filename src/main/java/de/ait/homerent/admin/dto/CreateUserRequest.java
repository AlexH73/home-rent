package de.ait.homerent.admin.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String role; // ROLE_TENANT, ROLE_OWNER, ROLE_ADMIN, ROLE_OPERATOR
}

