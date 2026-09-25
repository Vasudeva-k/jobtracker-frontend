package com.vasudev.jobtracker.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PdfTextExtractor {

    /**
     * Extract text from a Spring MultipartFile
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file cannot be null or empty.");
        }
        try {
            return extractText(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded PDF file: " + e.getMessage(), e);
        }
    }

    /**
     * Extract text from byte array
     */
    public String extractText(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("PDF byte content cannot be empty.");
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator(System.lineSeparator());
            stripper.setWordSeparator(" ");
            String text = stripper.getText(document);
            if (text == null) {
                return "";
            }
            // Normalize excessive empty lines while preserving section breaks
            return text.replaceAll("(?m)^[ \\t]*\\r?\\n", "\n")
                       .replaceAll("\\n{3,}", "\n\n")
                       .trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF content: " + e.getMessage(), e);
        }
    }

    /**
     * Extract text from a filesystem Path
     */
    public String extractText(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("PDF file does not exist at path: " + filePath);
        }
        try {
            return extractText(Files.readAllBytes(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF from path " + filePath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Extract text from a File object
     */
    public String extractText(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("PDF file cannot be null and must exist.");
        }
        return extractText(file.toPath());
    }

    /**
     * Extract text from an InputStream
     */
    public String extractText(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null.");
        }
        try {
            return extractText(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF from stream: " + e.getMessage(), e);
        }
    }
}
