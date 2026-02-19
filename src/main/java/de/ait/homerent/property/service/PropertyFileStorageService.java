package de.ait.homerent.property.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 17.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

@Service
@Slf4j
public class PropertyFileStorageService {

    @Value("${app.upload.properties-dir}")
    private String propertiesDir;

    @Value("${app.upload.property-max-size}")
    private Long maxFileSize;

    public String storeFile(Long propertyId, MultipartFile file) {
        validateFile(file);

        try {
            Path propertyDir = Paths.get(propertiesDir, propertyId.toString());
            Files.createDirectories(propertyDir);

            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String storedFilename = UUID.randomUUID() + "_" + sanitizedFilename;

            Path targetPath = propertyDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved to {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Error storing file for property {}", propertyId, e);
            throw new RuntimeException("Could not store file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File too large: " + file.getOriginalFilename());
        }
    }
}
