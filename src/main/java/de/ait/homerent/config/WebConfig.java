package de.ait.homerent.config;

import de.ait.homerent.utils.LocalDateTimeFormatter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LocalDateTimeFormatter localDateTimeFormatter;

    public WebConfig(LocalDateTimeFormatter localDateTimeFormatter) {
        this.localDateTimeFormatter = localDateTimeFormatter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(localDateTimeFormatter);
    }
}
