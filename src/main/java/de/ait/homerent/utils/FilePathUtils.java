package de.ait.homerent.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 02.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */
public class FilePathUtils {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generates the file path for any entity with a photo.
     * The path format is:
     * /uploads/{entityId}-{slug}/{yyyy-MM-dd}/{uuid-fileName}
     * Example:
     * /uploads/1-kvartira-v-centre/2026-02-03/550e8400-e29b-41d4-a716-446655440000-room1.jpg
     *
     * @param entityId   id of the entity
     * @param entityName name of the entity (used to generate slug)
     * @param date       upload date
     * @param fileName   original file name
     * @return generated file path with a unique UUID prefix to avoid name collisions
     */
    public static String generateFilePath(Long entityId, String entityName, LocalDate date, String fileName) {
        String slug = entityName
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        String uniqueFileName = java.util.UUID.randomUUID() + "-" + fileName;
        return "/uploads/" + entityId + "-" + slug + "/" + date.format(DATE_FORMAT) + "/" + uniqueFileName;
    }
}
