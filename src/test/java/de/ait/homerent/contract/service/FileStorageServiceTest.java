package de.ait.homerent.contract.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@DisplayName("FileStorageService unit tests")
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("storeRentalContract(): stores PDF under <rentalContractsDir>/<bookingId> and returns path")
    void storeRentalContract_storesPdf_andReturnsPath() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "rentalContractsDir", tempDir.resolve("contracts").toString());
        ReflectionTestUtils.setField(service, "rentalContractMaxSize", 10_000_000L);

        // not used in this test, but set them to avoid surprises later
        ReflectionTestUtils.setField(service, "issuesDir", tempDir.resolve("issues").toString());
        ReflectionTestUtils.setField(service, "issuePhotoMaxSize", 5_000_000L);
        ReflectionTestUtils.setField(service, "issuePhotoAllowedTypes", "image/jpeg,image/png");

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "PDFDATA".getBytes()
        );

        String path = service.storeRentalContract(7L, pdf);

        assertThat(path).isNotBlank();
        assertThat(Path.of(path)).exists();
        assertThat(Path.of(path).getParent().getFileName().toString()).isEqualTo("7");
    }

    @Test
    @DisplayName("storeRentalContract(): rejects empty file")
    void storeRentalContract_rejectsEmptyFile() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "rentalContractsDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "rentalContractMaxSize", 10_000_000L);

        MockMultipartFile empty = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", new byte[0]
        );

        assertThatThrownBy(() -> service.storeRentalContract(7L, empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is empty");
    }

    @Test
    @DisplayName("storeRentalContract(): rejects non-PDF content type")
    void storeRentalContract_rejectsNonPdf() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "rentalContractsDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "rentalContractMaxSize", 10_000_000L);

        MockMultipartFile txt = new MockMultipartFile(
                "file", "contract.txt", "text/plain", "data".getBytes()
        );

        assertThatThrownBy(() -> service.storeRentalContract(7L, txt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("storeRentalContract(): rejects file larger than max size")
    void storeRentalContract_rejectsTooLarge() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "rentalContractsDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "rentalContractMaxSize", 3L);

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "TOO_BIG".getBytes() // 7 bytes
        );

        assertThatThrownBy(() -> service.storeRentalContract(7L, pdf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File too large");
    }

    @Test
    @DisplayName("storeIssuePhoto(): allows configured image content types (jpeg/png)")
    void storeIssuePhoto_allowsConfiguredTypes() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "issuesDir", tempDir.resolve("issues").toString());
        ReflectionTestUtils.setField(service, "issuePhotoMaxSize", 10_000_000L);
        ReflectionTestUtils.setField(service, "issuePhotoAllowedTypes", "image/jpeg,image/png");

        // set rental fields too (not used)
        ReflectionTestUtils.setField(service, "rentalContractsDir", tempDir.resolve("contracts").toString());
        ReflectionTestUtils.setField(service, "rentalContractMaxSize", 10_000_000L);

        MockMultipartFile jpg = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "IMG".getBytes()
        );

        String path = service.storeIssuePhoto(9L, jpg);

        assertThat(Path.of(path)).exists();
        assertThat(Path.of(path).getParent().getFileName().toString()).isEqualTo("9");
    }

    @Test
    @DisplayName("storeIssuePhoto(): rejects disallowed image content type")
    void storeIssuePhoto_rejectsDisallowedType() {
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "issuesDir", tempDir.resolve("issues").toString());
        ReflectionTestUtils.setField(service, "issuePhotoMaxSize", 10_000_000L);
        ReflectionTestUtils.setField(service, "issuePhotoAllowedTypes", "image/jpeg,image/png");

        MockMultipartFile gif = new MockMultipartFile(
                "file", "photo.gif", "image/gif", "IMG".getBytes()
        );

        assertThatThrownBy(() -> service.storeIssuePhoto(9L, gif))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }
}
