package de.ait.homerent.issue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 13.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueCreateRequest {

    @NotNull(message = "Booking ID must not be null")
    private Long bookingId;

    @NotBlank(message = "Description must not be blank")
    private String description;

    private MultipartFile photo;
}
