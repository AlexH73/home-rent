package de.ait.homerent.booking.dto;

import de.ait.homerent.booking.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Dmitri Nedioglo
 * Created : 06.02.26
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String propertyTitle;
    private String tenantName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalPrice;
    private BookingStatus status;
}