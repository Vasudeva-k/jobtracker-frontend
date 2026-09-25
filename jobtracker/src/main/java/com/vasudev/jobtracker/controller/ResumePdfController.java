package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.ResumePdfRequest;
import com.vasudev.jobtracker.service.ResumePdfService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
public class ResumePdfController {

    private final ResumePdfService resumePdfService;

    public ResumePdfController(ResumePdfService resumePdfService) {
        this.resumePdfService = resumePdfService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> generateResumePdf(
            @RequestBody ResumePdfRequest request) {

        byte[] pdf = resumePdfService.generateResumePdf(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("Resume.pdf")
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}
