package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.ai.ResumeAnalyzer;
import com.vasudev.jobtracker.dto.ResumeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ResumeAnalyzerController {

    private final ResumeAnalyzer resumeAnalyzer;

    public ResumeAnalyzerController(ResumeAnalyzer resumeAnalyzer) {
        this.resumeAnalyzer = resumeAnalyzer;
    }

    @PostMapping("/resume-analyzer")
    public ResponseEntity<ResumeResponse> analyzeResume(
            @RequestBody Map<String, String> request) {

        String resumeText = request != null ? request.get("resumeText") : null;

        if (resumeText == null || resumeText.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ResumeResponse response = resumeAnalyzer.analyze(resumeText);
        return ResponseEntity.ok(response);
    }
}