package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.dto.ResumePdfRequest;
import com.vasudev.jobtracker.service.ResumePdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ResumePdfServiceImpl implements ResumePdfService {

    @Override
    public byte[] generateResumePdf(ResumePdfRequest request) {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                float y = 750;

                // ===========================
                // Name
                // ===========================
                contentStream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
                        22);

                String name = request.getFullName() != null ? request.getFullName() : "Candidate Resume";
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText(sanitizeText(name));
                contentStream.endText();

                y -= 25;

                // ===========================
                // Contact Details
                // ===========================
                contentStream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        11);

                String email = request.getEmail() != null ? request.getEmail() : "";
                String phone = request.getPhone() != null ? request.getPhone() : "";
                String contactLine = email;
                if (!phone.isBlank()) {
                    contactLine = contactLine.isBlank() ? phone : contactLine + " | " + phone;
                }

                if (!contactLine.isBlank()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(sanitizeText(contactLine));
                    contentStream.endText();
                    y -= 25;
                }

                // ===========================
                // Summary
                // ===========================
                if (request.getSummary() != null && !request.getSummary().isBlank()) {
                    y = writeSectionHeader(contentStream, "Professional Summary", y);
                    contentStream.setFont(
                            new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                            10);

                    y = writeMultiLineText(contentStream, request.getSummary(), 50, y, 14);
                    y -= 10;
                }

                // ===========================
                // Skills
                // ===========================
                if (request.getSkills() != null && !request.getSkills().isEmpty()) {
                    y = writeSectionHeader(contentStream, "Skills & Competencies", y);
                    contentStream.setFont(
                            new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                            10);

                    for (String skill : request.getSkills()) {
                        if (skill != null && !skill.isBlank()) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(60, y);
                            contentStream.showText("- " + sanitizeText(skill));
                            contentStream.endText();
                            y -= 14;
                        }
                    }
                    y -= 10;
                }

                // ===========================
                // Experience
                // ===========================
                if (request.getExperience() != null && !request.getExperience().isBlank()) {
                    y = writeSectionHeader(contentStream, "Work Experience", y);
                    contentStream.setFont(
                            new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                            10);

                    y = writeMultiLineText(contentStream, request.getExperience(), 50, y, 14);
                    y -= 10;
                }

                // ===========================
                // Education
                // ===========================
                if (request.getEducation() != null && !request.getEducation().isBlank()) {
                    y = writeSectionHeader(contentStream, "Education", y);
                    contentStream.setFont(
                            new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                            10);

                    y = writeMultiLineText(contentStream, request.getEducation(), 50, y, 14);
                    y -= 10;
                }

                // ===========================
                // Projects
                // ===========================
                if (request.getProjects() != null && !request.getProjects().isBlank()) {
                    y = writeSectionHeader(contentStream, "Projects", y);
                    contentStream.setFont(
                            new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                            10);

                    y = writeMultiLineText(contentStream, request.getProjects(), 50, y, 14);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private float writeSectionHeader(PDPageContentStream contentStream, String title, float y) throws IOException {
        contentStream.setFont(
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
                13);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(sanitizeText(title));
        contentStream.endText();
        return y - 16;
    }

    private float writeMultiLineText(PDPageContentStream contentStream, String text, float x, float y, float lineSpacing) throws IOException {
        if (text == null) return y;
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(sanitizeText(trimmed));
                contentStream.endText();
                y -= lineSpacing;
            }
        }
        return y;
    }

    private String sanitizeText(String text) {
        if (text == null) return "";
        // Replace non-ASCII and bullet characters with standard characters compatible with Standard14Fonts
        return text.replace("•", "-")
                .replace("—", "-")
                .replace("–", "-")
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("[^\\x20-\\x7E]", " ");
    }
}