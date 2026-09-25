package com.vasudev.jobtracker.service.storage.impl;

import com.vasudev.jobtracker.service.storage.ResumeStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ResumeStorageServiceImpl implements ResumeStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String saveFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot save an empty or null file");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String sanitizedBaseName = sanitizeFilename(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + sanitizedBaseName;
        Path targetPath = uploadPath.resolve(fileName).normalize();

        // Safety check against path traversal - target must remain strictly inside upload directory
        if (!targetPath.startsWith(uploadPath) || !uploadPath.equals(targetPath.getParent())) {
            throw new IllegalArgumentException("Invalid file name: path traversal attempt detected");
        }

        Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return fileName;
    }

    @Override
    public void deleteFile(String fileName) throws Exception {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String sanitizedName = sanitizeFilename(fileName);
        Path filePath = uploadPath.resolve(sanitizedName).normalize();

        if (!filePath.startsWith(uploadPath) || !uploadPath.equals(filePath.getParent())) {
            throw new IllegalArgumentException("Invalid file path: deletion outside storage directory is forbidden");
        }

        Files.deleteIfExists(filePath);
    }

    /**
     * Sanitizes untrusted user filenames against path traversal and dangerous characters.
     */
    public static String sanitizeFilename(String rawFilename) {
        if (rawFilename == null || rawFilename.isBlank()) {
            return "resume.pdf";
        }

        String decoded;
        try {
            decoded = URLDecoder.decode(rawFilename, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            decoded = rawFilename;
        }

        // Remove null bytes and control chars
        decoded = decoded.replace("\0", "").trim();

        // Extract base name handling both Unix and Windows separators
        String baseName = decoded.replace('\\', '/');
        int lastSlash = baseName.lastIndexOf('/');
        if (lastSlash >= 0) {
            baseName = baseName.substring(lastSlash + 1);
        }

        // Remove path traversal tokens
        baseName = baseName.replace("..", "");

        // Keep only alphanumeric and safe filename characters
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Remove leading/trailing dots or underscores
        baseName = baseName.replaceAll("^[._]+", "").replaceAll("[._]+$", "");

        if (baseName.isBlank()) {
            return "resume.pdf";
        }

        if (!baseName.toLowerCase().endsWith(".pdf")) {
            baseName = baseName + ".pdf";
        }

        return baseName;
    }
}

