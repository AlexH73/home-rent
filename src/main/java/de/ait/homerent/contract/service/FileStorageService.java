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

    public String storeRentalContract(Long bookingId, MultipartFile file) {
        validateFile(file);

        try {
            // creating a directory for a specific booking
            Path targetDir = Paths.get(rentalContractsDir, bookingId.toString());
            Files.createDirectories(targetDir);

            // generating a unique file name
            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String storedFilename = UUID.randomUUID() + "_" + sanitizedFilename;
            Path targetPath = targetDir.resolve(storedFilename);

            // copying the file
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Rental contract saved to {}", targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            log.error("Error storing rental contract for booking {}", bookingId, e);
            throw new RuntimeException("Could not store rental contract", e);
        }
    }

    // checking the file before saving
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Rejected rental contract upload: file={}, reason={}",
                    file != null ? file.getOriginalFilename() : "null",
                    "file is empty");
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            log.warn("Rejected rental contract upload: file={}, reason={}",
                    file.getOriginalFilename(), "content type is empty");
            throw new IllegalArgumentException("File content type is empty");
        }

        boolean allowed = contentType.equals("application/pdf"); // only PDF files allowed for rental contracts
        if (!allowed) {
            log.warn("Rejected rental contract upload: file={}, contentType={}, reason={}",
                    file.getOriginalFilename(), contentType, "content type not allowed");
            throw new IllegalArgumentException("File content type is not allowed: " + contentType);
        }

        if (file.getSize() > rentalContractMaxSize) {
            log.warn("Rejected rental contract upload: file={}, size={}, maxAllowed={}",
                    file.getOriginalFilename(), file.getSize(), rentalContractMaxSize);
            throw new IllegalArgumentException("File too large");
        }
    }
}


