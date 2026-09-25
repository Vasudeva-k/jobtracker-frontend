package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.dto.CoverLetterRequest;
import com.vasudev.jobtracker.dto.CoverLetterResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CoverLetterGenerator {

    private static final Logger log = LoggerFactory.getLogger(CoverLetterGenerator.class);
    private final GeminiService geminiService;

    public CoverLetterGenerator(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public CoverLetterResponse generateResponse(CoverLetterRequest request) {
        String jobRole = (request != null && request.getJobRole() != null && !request.getJobRole().isBlank())
                ? request.getJobRole().trim()
                : "Professional";
        String companyName = (request != null && request.getCompanyName() != null && !request.getCompanyName().isBlank())
                ? request.getCompanyName().trim()
                : "Hiring Team";

        log.info("[AI] Feature: Cover Letter Generator - Generating cover letter for Role: {}, Company: {}", jobRole, companyName);

        String prompt = """
                You are an expert executive career advisor and professional writer.

                Write a compelling, professional, ATS-friendly cover letter tailored specifically to the given target role and company.

                Rules:
                - Return ONLY the cover letter text.
                - Address it to the Hiring Manager or Hiring Team.
                - Focus on competencies, leadership, and problem-solving relevant to the position of "%s".
                - Do NOT force Java, Spring Boot, or unrelated tech stacks unless "%s" explicitly specifies them.
                - Length: approximately 200-250 words.
                - Finish with:
                Sincerely,
                Applicant

                Job Role: %s
                Company: %s
                """.formatted(
                jobRole,
                jobRole,
                jobRole,
                companyName
        );

        if (geminiService != null && geminiService.isAvailable()) {
            try {
                log.info("[AI] Calling Gemini for Cover Letter Generation...");
                String response = geminiService.askGemini(prompt);
                if (response != null && !response.isBlank()) {
                    log.info("[AI] Gemini response received for Cover Letter Generation");
                    log.info("[AI] Response source: GEMINI");
                    return new CoverLetterResponse(GeminiService.stripMarkdownFences(response), "GEMINI");
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Cover Letter: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Cover Letter: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Cover Letter. Using domain-neutral fallback.");
        }

        log.info("[AI] Response source: FALLBACK");
        String fallback = """
                Dear Hiring Manager,

                I am writing to express my enthusiastic interest in the %s opportunity at %s. With a solid foundation in delivering impactful solutions, optimizing workflows, and collaborating across multidisciplinary teams, I am eager to contribute meaningfully to your strategic initiatives.

                Throughout my professional journey, I have developed strong competencies aligned with %s responsibilities. I take pride in tackling complex challenges, maintaining high standards of quality, and driving measurable outcomes that support organizational objectives.

                I am particularly drawn to %s because of your commitment to excellence and innovation in the industry. I welcome the opportunity to discuss how my skills and background can add immediate value to your team.

                Thank you for your time and consideration.

                Sincerely,
                Applicant
                """.formatted(jobRole, companyName, jobRole, companyName).trim();

        return new CoverLetterResponse(fallback, "FALLBACK");
    }

    public String generate(CoverLetterRequest request) {
        return generateResponse(request).getCoverLetter();
    }
}