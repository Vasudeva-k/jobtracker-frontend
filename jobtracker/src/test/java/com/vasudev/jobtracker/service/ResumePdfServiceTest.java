package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.ResumePdfRequest;
import com.vasudev.jobtracker.service.impl.ResumePdfServiceImpl;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResumePdfServiceTest {

    private ResumePdfServiceImpl resumePdfService;

    @BeforeEach
    void setUp() {
        resumePdfService = new ResumePdfServiceImpl();
    }

    @Test
    void testGenerateResumePdfFullData() throws IOException {
        ResumePdfRequest request = new ResumePdfRequest(
                "Jane Doe",
                "jane@example.com",
                "+1 555-0199",
                "Experienced Software Engineer specializing in Java and Spring Boot.",
                List.of("Java", "Spring Boot", "React", "PostgreSQL"),
                "B.S. in Computer Science, Tech University (2020)",
                "Senior Developer at Tech Corp (2020 - Present)\n- Built REST APIs\n- Maintained CI/CD",
                "JobTracker Cloud - Full-stack job application tracker"
        );

        byte[] pdfBytes = resumePdfService.generateResumePdf(request);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Verify PDF can be loaded and read by PDFBox
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    @Test
    void testGenerateResumePdfWithNullAndEmptyOptionalFields() throws IOException {
        ResumePdfRequest request = new ResumePdfRequest();
        request.setFullName("Minimal User");
        request.setEmail("minimal@example.com");

        byte[] pdfBytes = resumePdfService.generateResumePdf(request);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }
}
