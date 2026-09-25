package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.ai.ResumeAnalyzer;
import com.vasudev.jobtracker.dto.ResumeFileResponse;
import com.vasudev.jobtracker.dto.ResumeResponse;
import com.vasudev.jobtracker.entity.Resume;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.ResumeRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.impl.ResumeServiceImpl;
import com.vasudev.jobtracker.service.storage.ResumeStorageService;
import com.vasudev.jobtracker.util.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeAnalyzer resumeAnalyzer;

    @Mock
    private ResumeStorageService storageService;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PdfTextExtractor pdfTextExtractor;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(resumeService, "uploadDir", "uploads/resumes");
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .role("USER")
                .active(true)
                .build();
    }

    @Test
    void testAnalyzeResumeSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "my_resume.pdf",
                "application/pdf",
                "PDF test content".getBytes()
        );

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUser(user)).thenReturn(Optional.empty());
        when(storageService.saveFile(file)).thenReturn("uuid-my_resume.pdf");
        when(pdfTextExtractor.extractText(file)).thenReturn("Extracted Java Spring Boot skills");

        ResumeResponse expectedAiResponse = new ResumeResponse(
                85,
                List.of("Java", "Spring Boot"),
                List.of("Docker"),
                List.of("Add containerization details")
        );
        when(resumeAnalyzer.analyze("Extracted Java Spring Boot skills")).thenReturn(expectedAiResponse);

        ResumeResponse response = resumeService.analyzeResume(file, "user@example.com");

        assertNotNull(response);
        assertEquals(85, response.getAtsScore());
        assertEquals(2, response.getMatchedSkills().size());
        assertEquals(1, response.getMissingSkills().size());

        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    void testAnalyzeResumeReplacesOldResume() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new_resume.pdf",
                "application/pdf",
                "New PDF content".getBytes()
        );

        Resume oldResume = Resume.builder()
                .id(99L)
                .fileName("old-resume.pdf")
                .user(user)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUser(user)).thenReturn(Optional.of(oldResume));
        when(storageService.saveFile(file)).thenReturn("uuid-new_resume.pdf");
        when(pdfTextExtractor.extractText(file)).thenReturn("New resume text");
        when(resumeAnalyzer.analyze("New resume text")).thenReturn(new ResumeResponse(90, List.of("Java"), List.of(), List.of()));

        ResumeResponse response = resumeService.analyzeResume(file, "user@example.com");

        assertNotNull(response);
        verify(storageService, times(1)).deleteFile("old-resume.pdf");
        verify(resumeRepository, times(1)).delete(oldResume);
        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    void testAnalyzeResumeRejectsNonPdf() {
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "resume.docx",
                "application/msword",
                "Word content".getBytes()
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                resumeService.analyzeResume(txtFile, "user@example.com")
        );
        assertTrue(ex.getMessage().contains("PDF resumes are supported"));
    }

    @Test
    void testAnalyzeResumeRejectsOversizedFile() {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large_resume.pdf",
                "application/pdf",
                largeBytes
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                resumeService.analyzeResume(largeFile, "user@example.com")
        );
        assertTrue(ex.getMessage().contains("must be less than 5 MB"));
    }

    @Test
    void testGetMyResumeMetadataSuccess() {
        Resume resume = Resume.builder()
                .id(10L)
                .fileName("my_resume.pdf")
                .fileType("application/pdf")
                .fileSize(102400L)
                .filePath("uploads/resumes/my_resume.pdf")
                .uploadedAt(LocalDateTime.now())
                .user(user)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUser(user)).thenReturn(Optional.of(resume));

        ResumeFileResponse metadata = resumeService.getMyResume("user@example.com");

        assertNotNull(metadata);
        assertEquals(10L, metadata.getId());
        assertEquals("my_resume.pdf", metadata.getFileName());
        assertEquals("application/pdf", metadata.getFileType());
        assertEquals(102400L, metadata.getFileSize());
        assertNotNull(metadata.getUploadedAt());
    }

    @Test
    void testGetMyResumeNotFound() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                resumeService.getMyResume("user@example.com")
        );
    }
}
