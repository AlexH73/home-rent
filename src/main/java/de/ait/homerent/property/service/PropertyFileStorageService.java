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

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("Deleted file: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
            throw new RuntimeException("Could not delete file", e);
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            log.warn("Rejected property image upload: file={}, reason={}",
                    file != null ? file.getOriginalFilename() : "null",
                    "file is empty");
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            log.warn("Rejected property image upload: reason=filename is empty");
            throw new IllegalArgumentException("File name is empty");
        }

        if (file.getSize() > maxFileSize) {
            log.warn("Rejected property image upload: file={}, size={}, maxAllowed={}",
                    originalFilename, file.getSize(), maxFileSize);
            throw new IllegalArgumentException("File too large");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            log.warn("Rejected property image upload: file={}, reason=content type is empty",
                    originalFilename);
            throw new IllegalArgumentException("File content type is empty");
        }

        boolean allowed = contentType.equals("image/jpeg")
                || contentType.equals("image/png");

        if (!allowed) {
            log.warn("Rejected property image upload: file={}, contentType={}, reason=not allowed",
                    originalFilename, contentType);
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
        }
    }
}
