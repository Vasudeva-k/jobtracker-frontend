package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.ResumeFileResponse;
import com.vasudev.jobtracker.dto.ResumeResponse;
import com.vasudev.jobtracker.service.ResumeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResumeResponse analyzeResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {

        return resumeService.analyzeResume(
                file,
                authentication.getName()
        );
    }

    @GetMapping
    public ResumeFileResponse getMyResume(
            Authentication authentication) {

        return resumeService.getMyResume(
                authentication.getName()
        );
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadMyResume(
            Authentication authentication) throws Exception {

        byte[] file = resumeService.downloadMyResume(
                authentication.getName()
        );

        ByteArrayResource resource =
                new ByteArrayResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.pdf\""
                )
                .contentLength(file.length)
                .body(resource);
    }
}