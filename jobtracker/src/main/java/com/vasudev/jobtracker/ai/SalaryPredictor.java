package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.SalaryPredictionRequest;
import com.vasudev.jobtracker.dto.SalaryPredictionResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SalaryPredictor {

    private static final Logger log = LoggerFactory.getLogger(SalaryPredictor.class);

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SalaryPredictor(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public SalaryPredictor() {
        this.geminiService = null;
    }

    public SalaryPredictionResponse predict(SalaryPredictionRequest request) {

        int experience = request != null ? Math.max(0, request.getExperience()) : 0;
        List<String> rawSkills = (request != null && request.getSkills() != null)
                ? request.getSkills()
                : List.of();

        List<String> skills = new ArrayList<>();
        for (String s : rawSkills) {
            if (s != null && !s.trim().isBlank()) {
                skills.add(s.trim());
            }
        }

        String location = (request != null && request.getLocation() != null && !request.getLocation().isBlank())
                ? request.getLocation().trim()
                : "India / Remote";

        boolean geminiAvailable = geminiService != null && geminiService.isAvailable();
        log.info("[AI] Feature: Salary Predictor");
        log.info("[AI] Gemini available: {}", geminiAvailable);

        if (geminiAvailable) {
            String prompt = """
                    You are an expert tech compensation analyst. Provide an AI-generated salary estimate based on the candidate profile.

                    Candidate Details:
                    - Years of Experience: %d
                    - Stated Skills: %s
                    - Location/Market: %s

                    Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations:
                    {
                      "estimatedSalary": "<Estimated range with currency and (AI Estimate) label, e.g. '8 - 14 LPA (AI Estimate)' or '$85k - $115k (AI Estimate)'>",
                      "experienceLevel": "<Fresher | Junior | Mid-Level | Senior | Lead/Principal>",
                      "marketDemand": "<Very High | High | Medium | Moderate>",
                      "suggestions": ["suggestion 1 to increase compensation", "suggestion 2"]
                    }

                    Guidelines:
                    - Do NOT claim this is live verified payroll data; provide a qualitative market estimate.
                    - Tailor suggestions specifically to the candidate's actual skills and experience level.
                    """.formatted(experience, skills.isEmpty() ? "General Professional Skills" : String.join(", ", skills), location);

            try {
                log.info("[AI] Calling Gemini...");
                String jsonResponse = geminiService.askGeminiJson(prompt);
                log.info("[AI] Gemini response received");
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(jsonResponse);
                    log.info("[AI] Gemini JSON parsed successfully");

                    String estimatedSalary = root.has("estimatedSalary") ? root.get("estimatedSalary").asText() : "";
                    String experienceLevel = root.has("experienceLevel") ? root.get("experienceLevel").asText() : "Mid-Level";
                    String marketDemand = root.has("marketDemand") ? root.get("marketDemand").asText() : "High";
                    List<String> suggestions = extractStringList(root, "suggestions");

                    if (!estimatedSalary.isBlank()) {
                        SalaryPredictionResponse response = new SalaryPredictionResponse(estimatedSalary, experienceLevel, marketDemand, suggestions);
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException e) {
                log.error("[AI] Gemini API error for Salary Predictor: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.warn("[AI] Gemini call failed for Salary Predictor: {}. Using domain-neutral fallback", e.getMessage());
            }
        }

        log.info("[AI] Using domain-neutral fallback");
        return generateDomainNeutralSalaryEstimate(experience, skills);
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

    private SalaryPredictionResponse generateDomainNeutralSalaryEstimate(int experience, List<String> skills) {
        int baseMin;
        int baseMax;
        String experienceLevel;

        if (experience <= 1) {
            experienceLevel = "Entry-Level / Fresher";
            baseMin = 4;
            baseMax = 8;
        } else if (experience <= 3) {
            experienceLevel = "Junior Professional";
            baseMin = 7;
            baseMax = 12;
        } else if (experience <= 6) {
            experienceLevel = "Mid-Level Professional";
            baseMin = 11;
            baseMax = 19;
        } else {
            experienceLevel = "Senior Professional";
            baseMin = 19;
            baseMax = 32;
        }

        // Skill depth bonus (1.0 to 1.3 based on skill diversity)
        double multiplier = 1.0 + Math.min(0.3, skills.size() * 0.05);
        int finalMin = (int) Math.round(baseMin * multiplier);
        int finalMax = (int) Math.round(baseMax * multiplier);

        String estimatedSalary = finalMin + " - " + finalMax + " LPA (Market Estimate)";

        String marketDemand = skills.size() >= 5 ? "High" : (skills.size() >= 2 ? "Medium" : "Moderate");

        List<String> suggestions = new ArrayList<>();
        suggestions.add("Lead high-impact projects that deliver quantifiable business outcomes to negotiate top-of-band compensation.");
        suggestions.add("Obtain industry-recognized cloud or architecture certifications relevant to your core domain.");
        if (skills.size() < 4) {
            suggestions.add("Expand your portfolio with system design, automated testing, and cloud deployment competencies.");
        }

        SalaryPredictionResponse response = new SalaryPredictionResponse(
                estimatedSalary,
                experienceLevel,
                marketDemand,
                suggestions
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}