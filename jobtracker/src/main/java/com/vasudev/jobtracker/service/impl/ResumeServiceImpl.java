package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.ai.ResumeAnalyzer;
import com.vasudev.jobtracker.dto.ResumeFileResponse;
import com.vasudev.jobtracker.dto.ResumeResponse;
import com.vasudev.jobtracker.entity.Resume;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.ResumeRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.ResumeService;
import com.vasudev.jobtracker.service.storage.ResumeStorageService;
import com.vasudev.jobtracker.util.PdfTextExtractor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeAnalyzer resumeAnalyzer;
    private final ResumeStorageService storageService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PdfTextExtractor pdfTextExtractor;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ResumeServiceImpl(
            ResumeAnalyzer resumeAnalyzer,
            ResumeStorageService storageService,
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            PdfTextExtractor pdfTextExtractor) {

        this.resumeAnalyzer = resumeAnalyzer;
        this.storageService = storageService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    // =========================================================
    // ANALYZE + UPLOAD RESUME
    // =========================================================
    @Override
    public ResumeResponse analyzeResume(
            MultipartFile file,
            String email) throws Exception {

        validateFile(file);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Delete old resume if it exists
        resumeRepository.findByUser(user).ifPresent(oldResume -> {

            try {
                storageService.deleteFile(
                        oldResume.getFileName()
                );
            } catch (Exception ignored) {
            }

            resumeRepository.delete(oldResume);
        });

        // Save new PDF
        String savedFileName =
                storageService.saveFile(file);

        // Save resume information in database
        Resume resume = Resume.builder()
                .fileName(savedFileName)
                .fileType(file.getContentType())
                .filePath(
                        "uploads/resumes/" + savedFileName
                )
                .fileSize(file.getSize())
                .user(user)
                .build();

        resumeRepository.save(resume);

        // Extract PDF text using shared extractor
        String resumeText = pdfTextExtractor.extractText(file);

        if (resumeText.isBlank()) {
            throw new RuntimeException(
                    "Unable to extract text from the uploaded resume."
            );
        }

        // Send resume text to Gemini
        return resumeAnalyzer.analyze(resumeText);
    }

    // =========================================================
    // GET MY RESUME DETAILS
    // =========================================================
    @Override
    public ResumeFileResponse getMyResume(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Resume resume = resumeRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Resume not found"));

        return new ResumeFileResponse(
                resume.getId(),
                resume.getFileName(),
                resume.getFileType(),
                resume.getFileSize(),
                resume.getUploadedAt()
        );
    }

    // =========================================================
    // DOWNLOAD MY RESUME
    // =========================================================
    @Override
    public byte[] downloadMyResume(String email)
            throws Exception {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Resume resume = resumeRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Resume not found"));

        Path filePath = Paths
                .get(uploadDir)
                .resolve(resume.getFileName())
                .normalize();

        // Security check
        Path uploadPath = Paths
                .get(uploadDir)
                .toAbsolutePath()
                .normalize();

        if (!filePath.toAbsolutePath().startsWith(uploadPath)) {
            throw new RuntimeException(
                    "Invalid resume file path");
        }

        if (!Files.exists(filePath)) {
            throw new RuntimeException(
                    "Resume file not found");
        }

        return Files.readAllBytes(filePath);
    }

    // =========================================================
    // VALIDATE PDF
    // =========================================================
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Please upload a resume.");
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null ||
                !fileName.toLowerCase().endsWith(".pdf")) {

            throw new RuntimeException(
                    "Only PDF resumes are supported.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {

            throw new RuntimeException(
                    "Resume size must be less than 5 MB.");
        }
    }
}