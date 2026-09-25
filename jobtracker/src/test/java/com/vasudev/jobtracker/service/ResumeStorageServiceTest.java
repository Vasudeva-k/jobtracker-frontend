package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.service.storage.impl.ResumeStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ResumeStorageServiceTest {

    private ResumeStorageServiceImpl resumeStorageService;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        resumeStorageService = new ResumeStorageServiceImpl();
        ReflectionTestUtils.setField(resumeStorageService, "uploadDir", tempUploadDir.toString());
    }

    @Test
    void testSaveNormalValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "john_doe_resume.pdf",
                "application/pdf",
                "Valid resume PDF bytes".getBytes()
        );

        String savedFileName = resumeStorageService.saveFile(file);

        assertNotNull(savedFileName);
        assertTrue(savedFileName.contains("john_doe_resume.pdf"));
        assertTrue(Files.exists(tempUploadDir.resolve(savedFileName)));
    }

    @Test
    void testSanitizeFilename_PathTraversalSequences() {
        String sanitized1 = ResumeStorageServiceImpl.sanitizeFilename("../../../etc/passwd");
        assertFalse(sanitized1.contains(".."));
        assertFalse(sanitized1.contains("/"));
        assertTrue(sanitized1.endsWith(".pdf"));

        String sanitized2 = ResumeStorageServiceImpl.sanitizeFilename("..\\..\\Windows\\System32\\cmd.exe");
        assertFalse(sanitized2.contains(".."));
        assertFalse(sanitized2.contains("\\"));
        assertTrue(sanitized2.endsWith(".pdf"));
    }

    @Test
    void testSanitizeFilename_AbsolutePathsAndSpecialChars() {
        String sanitizedWindows = ResumeStorageServiceImpl.sanitizeFilename("C:\\Users\\admin\\secret.pdf");
        assertFalse(sanitizedWindows.contains(":"));
        assertFalse(sanitizedWindows.contains("\\"));
        assertTrue(sanitizedWindows.endsWith(".pdf"));

        String sanitizedSpecial = ResumeStorageServiceImpl.sanitizeFilename("my resume (1) [final] #2.pdf");
        assertFalse(sanitizedSpecial.contains(" "));
        assertFalse(sanitizedSpecial.contains("#"));
        assertTrue(sanitizedSpecial.endsWith(".pdf"));
    }

    @Test
    void testSaveFile_TraversalAttemptIsSanitizedAndContained() throws Exception {
        MockMultipartFile traversalFile = new MockMultipartFile(
                "file",
                "../../../../evil_script.pdf",
                "application/pdf",
                "malicious content".getBytes()
        );

        String savedFileName = resumeStorageService.saveFile(traversalFile);

        assertNotNull(savedFileName);
        Path resolved = tempUploadDir.resolve(savedFileName).normalize();
        assertTrue(resolved.startsWith(tempUploadDir.toAbsolutePath().normalize()));
        assertEquals(tempUploadDir.toAbsolutePath().normalize(), resolved.getParent());
        assertTrue(Files.exists(resolved));
    }

    @Test
    void testSaveFile_NullAndEmptyFileThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeStorageService.saveFile(null)
        );

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () ->
                resumeStorageService.saveFile(emptyFile)
        );
    }

    @Test
    void testDeleteFile_ValidAndContained() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to_delete.pdf",
                "application/pdf",
                "Delete me".getBytes()
        );

        String savedFileName = resumeStorageService.saveFile(file);
        assertTrue(Files.exists(tempUploadDir.resolve(savedFileName)));

        resumeStorageService.deleteFile(savedFileName);
        assertFalse(Files.exists(tempUploadDir.resolve(savedFileName)));
    }
}
