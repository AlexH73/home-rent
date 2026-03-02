package de.ait.homerent.contract.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author  : Tetiana Anufriieva
 * Created : 13.02.2026
 * Project : home-rent
 * ----------------------------------------------------------------------------
 */

//Service for working with files on disk
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.rental-contracts-dir}")
    private String rentalContractsDir;

    @Value("${app.upload.rental-contracts-max-size}")
    private Long rentalContractMaxSize;

    @Value("${app.upload.issues-dir}")
    private String issuesDir;

    @Value("${app.upload.issue-photo-max-size}")
    private Long issuePhotoMaxSize;

    @Value("${app.upload.issue-photo-allowed-types}")
    private String issuePhotoAllowedTypes;

    public String storeRentalContract(Long bookingId, MultipartFile file) {
        validateFile(file, rentalContractMaxSize, "application/pdf");
        return storeFile(file, rentalContractsDir, bookingId.toString());
    }

    public String storeIssuePhoto(Long bookingId, MultipartFile file) {
        List<String> allowedTypes = Arrays.asList(issuePhotoAllowedTypes.split(","));
        validateFile(file, issuePhotoMaxSize, allowedTypes);
        return storeFile(file, issuesDir, bookingId.toString());
    }

    private String storeFile(MultipartFile file, String baseDir, String subDir) {
        try {
            Path targetDir = Paths.get(baseDir, subDir);
            Files.createDirectories(targetDir);

            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String storedFilename = UUID.randomUUID() + "_" + sanitizedFilename;
            Path targetPath = targetDir.resolve(storedFilename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to {}", targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            log.error("Error storing file", e);
            throw new RuntimeException("Could not store file", e);
        }
    }

    private void validateFile(MultipartFile file, long maxSize, String... allowedContentTypes) {
        validateFile(file, maxSize, Arrays.asList(allowedContentTypes));
    }

    private void validateFile(MultipartFile file, long maxSize, List<String> allowedContentTypes) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("File content type is empty");
        }

        boolean allowed = allowedContentTypes.stream()
                .anyMatch(allowedType -> allowedType.equalsIgnoreCase(contentType));
        if (!allowed) {
            throw new IllegalArgumentException("File content type not allowed: " + contentType);
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File too large (max " + maxSize + " bytes)");
        }
    }
}
