package de.ait.homerent.booking.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.ait.homerent.utils.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request object for creating a new booking")
public class BookingCreateRequest {

    @NotNull
    @Schema(description = "ID of the property to book", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long propertyId;

    @NotNull
    @FutureOrPresent(message = "Start date must be today or in the future")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "Start date of rental (format: yyyy-MM-dd, will be set to start of day)", example = "2026-03-01", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startDate;

    @NotNull
    @FutureOrPresent(message = "End date must be today or in the future")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "End date of rental (format: yyyy-MM-dd, will be set to start of day)", example = "2026-03-10", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endDate;
}
