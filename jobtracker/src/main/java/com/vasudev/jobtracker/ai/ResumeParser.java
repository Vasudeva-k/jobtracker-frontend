package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.util.PdfTextExtractor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ResumeParser {

    private final PdfTextExtractor pdfTextExtractor;

    public ResumeParser(PdfTextExtractor pdfTextExtractor) {
        this.pdfTextExtractor = pdfTextExtractor;
    }

    public String extractText(MultipartFile file) {
        return pdfTextExtractor.extractText(file);
    }
}
