package de.ait.homerent.booking.dto;

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
    private Long propertyId;
    private Long tenantId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
