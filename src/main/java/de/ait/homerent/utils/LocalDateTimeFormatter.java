package de.ait.homerent.utils;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Component
public class LocalDateTimeFormatter implements Formatter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDateTime parse(String text, Locale locale) {
        if (text == null || text.isBlank()) {
            return null;
        }
        LocalDate date = LocalDate.parse(text, FORMATTER);
        return date.atStartOfDay(); // начало дня
    }

    @Override
    public String print(LocalDateTime object, Locale locale) {
        return object.toLocalDate().format(FORMATTER);
    }
}
