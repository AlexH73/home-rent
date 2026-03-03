package de.ait.homerent.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Unified file storage service for property photos, issue photos and rental contracts.
 * Provides safe empty-directory cleanup that never removes directories above the
 * configured domain root dirs.
 */
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

    @Value("${app.upload.issue-photo-allowed-types}")
    private String issuePhotoAllowedTypes;

    @Value("${app.upload.rental-contracts-dir}")
    private String rentalContractsDir;

    @Value("${app.upload.rental-contracts-max-size}")
    private Long rentalContractMaxSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/jpg", "image/png");

    // For Property photos
    public String storePropertyFile(Long propertyId, MultipartFile file) {
        validateFile(file, propertyMaxSize, ALLOWED_IMAGE_TYPES);
        return storeFile(file, propertiesDir, propertyId.toString());
    }

    // For Issue photos
    public String storeIssueFile(Long issueId, MultipartFile file) {
        List<String> allowedTypes = Arrays.stream(issuePhotoAllowedTypes.split(","))
                .map(String::trim)
                .toList();
        validateFile(file, issueMaxSize, allowedTypes);
        return storeFile(file, issuesDir, issueId.toString());
    }

    // For Rental Contract files
    public String storeRentalContractFile(Long bookingId, MultipartFile file) {
        validateFile(file, rentalContractMaxSize, List.of("application/pdf"));
        return storeFile(file, rentalContractsDir, bookingId.toString());
    }

    // General save method
    private String storeFile(MultipartFile file, String baseDir, String subDir) {
        try {
            Path dir = Paths.get(baseDir, subDir);
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
            log.error("Error storing file in {}/{}", baseDir, subDir, e);
            throw new RuntimeException("Could not store file", e);
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("Deleted file: {}", filePath);
                deleteEmptyParentDir(path);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
        }
    }

    /**
     * Safely deletes the parent directory of {@code filePath} if it is empty,
     * but only when that parent is a direct child of one of the configured
     * upload root directories (propertiesDir, issuesDir, rentalContractsDir).
     * This prevents accidental deletion of the root dirs themselves or any
     * directories above them.
     */
    private void deleteEmptyParentDir(Path filePath) {
        Path parent = filePath.getParent();
        if (parent == null) {
            return;
        }
        Path grandParent = parent.getParent();
        if (grandParent == null) {
            return;
        }

        Set<Path> rootDirs = buildRootDirsSet();
        if (rootDirs.isEmpty() || !rootDirs.contains(grandParent)) {
            return;
        }

        try {
            if (Files.isDirectory(parent) && isDirectoryEmpty(parent)) {
                Files.delete(parent);
                log.info("Deleted empty directory: {}", parent);
            }
        } catch (IOException e) {
            log.warn("Could not delete empty directory {}: {}", parent, e.getMessage());
        }
    }

    private Set<Path> buildRootDirsSet() {
        Set<Path> dirs = new java.util.HashSet<>();
        if (propertiesDir != null) dirs.add(Paths.get(propertiesDir).toAbsolutePath().normalize());
        if (issuesDir != null) dirs.add(Paths.get(issuesDir).toAbsolutePath().normalize());
        if (rentalContractsDir != null) dirs.add(Paths.get(rentalContractsDir).toAbsolutePath().normalize());
        return dirs;
    }

    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.findAny().isEmpty();
        }
    }

    // File validation
    private void validateFile(MultipartFile file, Long maxSize, List<String> allowedContentTypes) {
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

        boolean allowed = allowedContentTypes.stream()
                .anyMatch(t -> t.equalsIgnoreCase(contentType));

        if (!allowed) {
            log.warn("Rejected upload: file={}, contentType={}, reason=not allowed",
                    file.getOriginalFilename(), contentType);
            throw new IllegalArgumentException("File content type not allowed: " + contentType);
        }
    }
}