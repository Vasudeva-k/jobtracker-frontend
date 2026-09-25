package com.vasudev.jobtracker.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextExtractorTest {

    private PdfTextExtractor pdfTextExtractor;

    @BeforeEach
    void setUp() {
        pdfTextExtractor = new PdfTextExtractor();
    }

    private byte[] createSamplePdf(String content) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(content);
                cs.endText();
            }
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    void testExtractTextFromBytes() throws IOException {
        byte[] pdfBytes = createSamplePdf("John Doe - Senior Java Developer");
        String extracted = pdfTextExtractor.extractText(pdfBytes);
        assertNotNull(extracted);
        assertTrue(extracted.contains("John Doe"));
        assertTrue(extracted.contains("Senior Java Developer"));
    }

    @Test
    void testExtractTextFromMultipartFile() throws IOException {
        byte[] pdfBytes = createSamplePdf("Skills: Java, Spring Boot, Microservices");
        MockMultipartFile file = new MockMultipartFile("resume", "resume.pdf", "application/pdf", pdfBytes);
        String extracted = pdfTextExtractor.extractText(file);
        assertNotNull(extracted);
        assertTrue(extracted.contains("Spring Boot"));
    }

    @Test
    void testExtractTextNullFileThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> pdfTextExtractor.extractText((MockMultipartFile) null));
    }

    @Test
    void testExtractTextEmptyBytesThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> pdfTextExtractor.extractText(new byte[0]));
    }
}
