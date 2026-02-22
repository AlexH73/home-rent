package de.ait.homerent.property.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.ait.homerent.property.model.PropertyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "List of photo URLs (optional field)", required = false)
    private List<String> photoUrls;
}
