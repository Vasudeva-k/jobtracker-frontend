package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class ResumeAIController {

    private static final Logger log = LoggerFactory.getLogger(ResumeAIController.class);
    private final GeminiService geminiService;

    public ResumeAIController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/resume-review")
    public ResponseEntity<String> reviewResume(@RequestBody(required = false) String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return ResponseEntity.badRequest().body("Resume text cannot be empty.");
        }

        log.info("[AI] Feature: Resume AI Review - Starting analysis");

        if (geminiService != null && geminiService.isAvailable()) {
            String prompt = """
                    You are an experienced HR recruiter.

                    Review the following resume.

                    Give feedback under these headings:

                    1. Overall Score (out of 10)
                    2. Strengths
                    3. Weaknesses
                    4. Missing Skills
                    5. Suggestions for Improvement

                    Resume:

                    """ + resumeText;

            try {
                log.info("[AI] Calling Gemini for Resume Review...");
                String result = geminiService.askGemini(prompt);
                if (result != null && !result.isBlank()) {
                    log.info("[AI] Gemini response received for Resume Review");
                    log.info("[AI] Response source: GEMINI");
                    return ResponseEntity.ok(result);
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Resume Review: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Resume Review: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Resume Review. Using domain-neutral fallback.");
        }

        log.info("[AI] Response source: FALLBACK");
        return ResponseEntity.ok(generateDomainNeutralReview(resumeText));
    }

    private String generateDomainNeutralReview(String resumeText) {
        String lower = resumeText.toLowerCase();
        int score = 6;
        if (lower.contains("experience") || lower.contains("work")) score++;
        if (lower.contains("project")) score++;
        if (lower.contains("education") || lower.contains("degree")) score++;
        score = Math.min(9, Math.max(4, score));

        return """
                1. Overall Score (Rule-Based Evaluation): %d/10
                2. Strengths: Documented background and technical/domain competency.
                3. Weaknesses: Consider adding more quantifiable business metrics and project scope details.
                4. Missing Skills: Advanced domain tooling, automated workflows, and modern cloud/collaboration platforms.
                5. Suggestions for Improvement: Ensure your accomplishments emphasize measurable business impact and optimize section keywords for ATS scanners.
                """.formatted(score);
    }
}

