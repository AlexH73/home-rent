package de.ait.homerent.property.dto;

import de.ait.homerent.property.model.PropertyStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 09.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
public class PropertyDto {

    private Long id;
    private Long ownerId;
    private String title;
    private String address;
    private String description;
    private Integer pricePerDay;
    private PropertyStatus status;
    private LocalDateTime availableFrom;
    private LocalDateTime availableTo;
    private LocalDateTime createdAt;
}
