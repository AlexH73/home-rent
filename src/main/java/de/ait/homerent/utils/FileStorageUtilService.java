package de.ait.homerent.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

//Universal service for file storage (Property & Issue)
@Service
@Slf4j
@Getter
public class FileStorageUtilService {

    @Value("${app.upload.properties-dir}")
    private String propertiesDir;

    @Value("${app.upload.property-max-size}")
    private Long propertyMaxSize;

    @Value("${app.upload.issues-dir}")
    private String issuesDir;


    @Value("${app.upload.issue-photo-max-size}")
    private Long issueMaxSize;

    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png"};

    // For Property
    public String storePropertyFile(Long propertyId, MultipartFile file) {
        validateFile(file, propertyMaxSize, ALLOWED_IMAGE_TYPES);
        return storeFile(file, propertiesDir, propertyId);
    }

    // For Issue
    public String storeIssueFile(Long issueId, MultipartFile file) {
        validateFile(file, issueMaxSize, ALLOWED_IMAGE_TYPES);
        return storeFile(file, issuesDir, issueId);
    }

    // General save method
    private String storeFile(MultipartFile file, String baseDir, Long entityId) {
        try {
            Path dir = Paths.get(baseDir, entityId.toString());
            Files.createDirectories(dir);

            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = originalFilename != null
                    ? originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_")
                    : "file";

            String storedFilename = UUID.randomUUID() + "_" + sanitizedFilename;
            Path targetPath = dir.resolve(storedFilename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to {}", targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            log.error("Error storing file for entity {}", entityId, e);
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

    // File check
    private void validateFile(MultipartFile file, Long maxSize, String[] allowedContentTypes) {
        if (file == null || file.isEmpty()) {
            log.warn("Rejected upload: file is empty");
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File too large: " + file.getOriginalFilename());
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            log.warn("Rejected upload: file={}, reason=content type is empty",
                    file.getOriginalFilename());
            throw new IllegalArgumentException("File content type is empty");
        }

        boolean allowed = contentType.equals("image/jpeg")|| contentType.equals("image/jpg")
                || contentType.equals("image/png");

        if (!allowed) {
            log.warn("Rejected property image upload: file={}, contentType={}, reason=not allowed",
                    file.getOriginalFilename(), contentType);
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
        }
    }
}