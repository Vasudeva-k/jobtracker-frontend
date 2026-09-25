package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.ATSScoreRequest;
import com.vasudev.jobtracker.dto.ATSScoreResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ATSScorePredictor {

    private static final Logger log = LoggerFactory.getLogger(ATSScorePredictor.class);
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ATSScorePredictor(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public ATSScorePredictor() {
        this.geminiService = null;
    }

    public ATSScoreResponse predict(ATSScoreRequest request) {

        String resume = (request != null && request.getResumeText() != null)
                ? request.getResumeText().trim()
                : "";

        if (resume.isBlank()) {
            return new ATSScoreResponse(
                    0,
                    "Needs Improvement",
                    0,
                    0,
                    0,
                    List.of("Experience", "Projects", "Education", "Skills"),
                    List.of("Please provide resume text to analyze ATS compatibility.")
            );
        }

        log.info("[AI] Feature: ATS Score Predictor - Scoring candidate resume ATS compatibility");

        if (geminiService != null && geminiService.isAvailable()) {
            String prompt = """
                    You are an expert ATS (Applicant Tracking System) parser and evaluator. Analyze the candidate resume across whatever professional domain it represents.

                    Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations:
                    {
                      "atsScore": <integer 0-100>,
                      "resumeStrength": "<Excellent | Good | Average | Needs Improvement>",
                      "keywordCoverage": <integer 0-100 indicating keyword depth for its domain>,
                      "readability": <integer 0-100 assessing structure, bullet clarity, formatting>,
                      "formatScore": <integer 0-100 assessing section completeness>,
                      "missingSections": ["Section1", "Section2"],
                      "recommendations": ["Recommendation1", "Recommendation2"]
                    }

                    <candidate_resume>
                    %s
                    </candidate_resume>
                    """.formatted(resume);

            try {
                log.info("[AI] Calling Gemini for ATS Score Prediction...");
                String jsonResponse = geminiService.askGeminiJson(prompt);
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(jsonResponse);

                    int atsScore = root.has("atsScore") ? Math.min(100, Math.max(0, root.get("atsScore").asInt())) : 0;
                    String strength = root.has("resumeStrength") ? root.get("resumeStrength").asText() : "Average";
                    int keywordCoverage = root.has("keywordCoverage") ? root.get("keywordCoverage").asInt() : 75;
                    int readability = root.has("readability") ? root.get("readability").asInt() : 80;
                    int formatScore = root.has("formatScore") ? root.get("formatScore").asInt() : 80;

                    List<String> missingSections = extractStringList(root, "missingSections");
                    List<String> recommendations = extractStringList(root, "recommendations");

                    if (atsScore > 0 || !recommendations.isEmpty()) {
                        log.info("[AI] Gemini JSON parsed successfully for ATS Score Prediction (score: {})", atsScore);
                        ATSScoreResponse response = new ATSScoreResponse(
                                atsScore,
                                strength,
                                keywordCoverage,
                                readability,
                                formatScore,
                                missingSections,
                                recommendations
                        );
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for ATS Score Prediction: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for ATS Score Prediction: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for ATS Score Prediction. Using domain-neutral fallback.");
        }

        return generateDomainNeutralScore(resume);
    }

    private List<String> extractStringList(JsonNode root, String fieldName) {
        List<String> list = new ArrayList<>();
        if (root.has(fieldName) && root.get(fieldName).isArray()) {
            for (JsonNode item : root.get(fieldName)) {
                String text = item.asText().trim();
                if (!text.isBlank()) {
                    list.add(text);
                }
            }
        }
        return list;
    }

    private ATSScoreResponse generateDomainNeutralScore(String resume) {
        String lower = resume.toLowerCase();

        List<String> missingSections = new ArrayList<>();
        if (!lower.contains("experience") && !lower.contains("work history") && !lower.contains("employment")) {
            missingSections.add("Experience");
        }
        if (!lower.contains("project") && !lower.contains("portfolio")) {
            missingSections.add("Projects");
        }
        if (!lower.contains("education") && !lower.contains("degree") && !lower.contains("university")) {
            missingSections.add("Education");
        }
        if (!lower.contains("skill") && !lower.contains("technologies")) {
            missingSections.add("Skills");
        }
        if (!lower.contains("certification") && !lower.contains("achievement") && !lower.contains("award")) {
            missingSections.add("Certifications / Achievements");
        }

        // Calculate format score based on section completeness
        int formatScore = Math.max(20, 100 - (missingSections.size() * 15));

        // Readability evaluation based on line count, sentence length, and structure
        String[] lines = resume.split("\\R");
        int readability = lines.length >= 8 ? 90 : (resume.length() >= 50 ? 85 : Math.max(40, resume.length()));

        // Keyword coverage based on substantive token density
        Pattern wordPattern = Pattern.compile("\\b[a-zA-Z0-9#+.-]{3,}\\b");
        Matcher wordMatcher = wordPattern.matcher(resume);
        int wordCount = 0;
        while (wordMatcher.find()) {
            wordCount++;
        }
        int keywordCoverage = wordCount >= 30 ? 88 : Math.min(85, Math.max(30, wordCount * 3));

        int atsScore = (keywordCoverage + readability + formatScore) / 3;
        atsScore = Math.min(95, Math.max(25, atsScore));

        String strength;
        if (atsScore >= 85) {
            strength = "Excellent";
        } else if (atsScore >= 70) {
            strength = "Good";
        } else if (atsScore >= 50) {
            strength = "Average";
        } else {
            strength = "Needs Improvement";
        }

        List<String> recommendations = new ArrayList<>();
        for (String section : missingSections) {
            recommendations.add("Add a clearly labeled " + section + " section to improve automated ATS parsing.");
        }
        if (keywordCoverage < 75) {
            recommendations.add("Expand role-specific skills, industry tools, and technical terms in your work history.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Your resume demonstrates strong structural ATS readiness and comprehensive section organization.");
        }

        ATSScoreResponse response = new ATSScoreResponse(
                atsScore,
                strength,
                keywordCoverage,
                readability,
                formatScore,
                missingSections,
                recommendations
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}