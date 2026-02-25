package de.ait.homerent.property.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 25.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@DisplayName("PropertyFileStorageService unit tests")
class PropertyFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("storeFile(): saves file to <propertiesDir>/<propertyId> directory and returns full path")
    void storeFile_savesFileAndReturnsPath() throws Exception {
        PropertyFileStorageService service = new PropertyFileStorageService();
        ReflectionTestUtils.setField(service, "propertiesDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "maxFileSize", 10_000_000L);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "hello".getBytes()
        );

        String storedPath = service.storeFile(7L, file);

        assertThat(storedPath).isNotBlank();
        assertThat(Path.of(storedPath)).exists();
        assertThat(Path.of(storedPath).getParent().getFileName().toString()).isEqualTo("7");
    }

    @Test
    @DisplayName("storeFile(): when file is empty, throws IllegalArgumentException")
    void storeFile_whenEmpty_throws() {
        PropertyFileStorageService service = new PropertyFileStorageService();
        ReflectionTestUtils.setField(service, "propertiesDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "maxFileSize", 10_000_000L);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[0]
        );

        assertThatThrownBy(() -> service.storeFile(7L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    @DisplayName("storeFile(): when content type is not allowed, throws IllegalArgumentException")
    void storeFile_whenWrongContentType_throws() {
        PropertyFileStorageService service = new PropertyFileStorageService();
        ReflectionTestUtils.setField(service, "propertiesDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "maxFileSize", 10_000_000L);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.gif", "image/gif", "x".getBytes()
        );

        assertThatThrownBy(() -> service.storeFile(7L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only JPEG and PNG images are allowed");
    }

    @Test
    @DisplayName("deleteFile(): deletes file if it exists")
    void deleteFile_deletesExisting() throws Exception {
        PropertyFileStorageService service = new PropertyFileStorageService();
        ReflectionTestUtils.setField(service, "propertiesDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "maxFileSize", 10_000_000L);

        Path file = tempDir.resolve("x.txt");
        Files.writeString(file, "data");
        assertThat(file).exists();

        service.deleteFile(file.toString());

        assertThat(file).doesNotExist();
    }

    @Test
    @DisplayName("deleteFile(): when file does not exist, does not throw")
    void deleteFile_whenMissing_doesNotThrow() {
        PropertyFileStorageService service = new PropertyFileStorageService();
        ReflectionTestUtils.setField(service, "propertiesDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "maxFileSize", 10_000_000L);

        assertThatCode(() -> service.deleteFile(tempDir.resolve("missing.txt").toString()))
                .doesNotThrowAnyException();
    }
}
