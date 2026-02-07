package de.ait.homerent.booking.dto;

import de.ait.homerent.booking.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 07.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private Long propertyId;
    private Long tenantId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BookingStatus status;
    private Integer totalPrice;
}
