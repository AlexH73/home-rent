package de.ait.homerent.admin.service;

import de.ait.homerent.admin.dto.CreateUserRequest;
import de.ait.homerent.admin.dto.UpdateUserRoleRequest;
import de.ait.homerent.admin.dto.UserAdminResponse;

import java.util.List;

public interface AdminUserService {

    List<UserAdminResponse> getAllUsers();

    UserAdminResponse getUserById(Long id);

    UserAdminResponse createUser(CreateUserRequest request);

    void deleteUser(Long id);

    UserAdminResponse updateUserStatus(Long id, boolean enabled);

    UserAdminResponse updateUserRole(Long id, String role);
}

