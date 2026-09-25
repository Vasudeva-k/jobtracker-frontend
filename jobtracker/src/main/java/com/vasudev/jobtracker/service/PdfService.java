package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.util.PdfTextExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfService {

    private final PdfTextExtractor pdfTextExtractor;

    public PdfService(PdfTextExtractor pdfTextExtractor) {
        this.pdfTextExtractor = pdfTextExtractor;
    }

    public String extractText(MultipartFile file) {
        return pdfTextExtractor.extractText(file);
    }
}
