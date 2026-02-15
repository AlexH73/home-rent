package de.ait.homerent.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 09.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
public class UpdateRolesRequest {

    @NotEmpty(message = "Role list must not be empty")
    private List<String> roles;
}
