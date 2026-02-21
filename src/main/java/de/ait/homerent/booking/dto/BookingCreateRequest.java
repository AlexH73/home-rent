package de.ait.homerent.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 10.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequest {
    @NotNull
    private Long propertyId;
    @NotNull
    private Long tenantId;
    @NotNull
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;
    @NotNull
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;
}
