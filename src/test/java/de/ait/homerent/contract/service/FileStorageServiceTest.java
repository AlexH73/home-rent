package de.ait.homerent.contract.service;

import de.ait.homerent.utils.FileStorageUtilService;
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

    private FileStorageUtilService createService() {
        FileStorageUtilService service = new FileStorageUtilService();
        ReflectionTestUtils.setField(service, "propertiesDir", tempDir.resolve("properties").toString());
        ReflectionTestUtils.setField(service, "propertyMaxSize", 10_000_000L);
        ReflectionTestUtils.setField(service, "issuesDir", tempDir.resolve("issues").toString());
        ReflectionTestUtils.setField(service, "issueMaxSize", 5_000_000L);
        ReflectionTestUtils.setField(service, "issuePhotoAllowedTypes", "image/jpeg,image/png");
        ReflectionTestUtils.setField(service, "rentalContractsDir", tempDir.resolve("contracts").toString());
        ReflectionTestUtils.setField(service, "rentalContractMaxSize", 10_000_000L);
        return service;
    }

    @Test
    @DisplayName("storeRentalContractFile(): stores PDF under <rentalContractsDir>/<bookingId> and returns path")
    void storeRentalContractFile_storesPdf_andReturnsPath() {
        FileStorageUtilService service = createService();

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "PDFDATA".getBytes()
        );

        String path = service.storeRentalContractFile(7L, pdf);

        assertThat(path).isNotBlank();
        assertThat(Path.of(path)).exists();
        assertThat(Path.of(path).getParent().getFileName().toString()).isEqualTo("7");
    }

    @Test
    @DisplayName("storeRentalContractFile(): rejects empty file")
    void storeRentalContractFile_rejectsEmptyFile() {
        FileStorageUtilService service = createService();

        MockMultipartFile empty = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", new byte[0]
        );

        assertThatThrownBy(() -> service.storeRentalContractFile(7L, empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is empty");
    }

    @Test
    @DisplayName("storeRentalContractFile(): rejects non-PDF content type")
    void storeRentalContractFile_rejectsNonPdf() {
        FileStorageUtilService service = createService();

        MockMultipartFile txt = new MockMultipartFile(
                "file", "contract.txt", "text/plain", "data".getBytes()
        );

        assertThatThrownBy(() -> service.storeRentalContractFile(7L, txt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("storeRentalContractFile(): rejects file larger than max size")
    void storeRentalContractFile_rejectsTooLarge() {
        FileStorageUtilService service = createService();
        ReflectionTestUtils.setField(service, "rentalContractMaxSize", 3L);

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "TOO_BIG".getBytes() // 7 bytes
        );

        assertThatThrownBy(() -> service.storeRentalContractFile(7L, pdf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File too large");
    }

    @Test
    @DisplayName("storeIssueFile(): allows configured image content types (jpeg/png)")
    void storeIssueFile_allowsConfiguredTypes() {
        FileStorageUtilService service = createService();

        MockMultipartFile jpg = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "IMG".getBytes()
        );

        String path = service.storeIssueFile(9L, jpg);

        assertThat(Path.of(path)).exists();
        assertThat(Path.of(path).getParent().getFileName().toString()).isEqualTo("9");
    }

    @Test
    @DisplayName("storeIssueFile(): rejects disallowed image content type")
    void storeIssueFile_rejectsDisallowedType() {
        FileStorageUtilService service = createService();

        MockMultipartFile gif = new MockMultipartFile(
                "file", "photo.gif", "image/gif", "IMG".getBytes()
        );

        assertThatThrownBy(() -> service.storeIssueFile(9L, gif))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not allowed");
    }
}
