package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.JobSuccessPredictionRequest;
import com.vasudev.jobtracker.dto.JobSuccessPredictionResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobApplicationSuccessPredictor {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationSuccessPredictor.class);
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobApplicationSuccessPredictor(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public JobApplicationSuccessPredictor() {
        this.geminiService = null;
    }

    public JobSuccessPredictionResponse predict(JobSuccessPredictionRequest request) {

        if (request == null) {
            request = new JobSuccessPredictionRequest(0, 0, 0, 0, new ArrayList<>());
        }

        int atsScore = Math.max(0, Math.min(request.getAtsScore(), 100));
        int yearsOfExperience = Math.max(0, request.getYearsOfExperience());
        int projects = Math.max(0, request.getProjects());
        int certifications = Math.max(0, request.getCertifications());
        List<String> rawSkills = request.getSkills() != null ? request.getSkills() : List.of();

        List<String> userSkills = new ArrayList<>();
        for (String s : rawSkills) {
            if (s != null && !s.trim().isBlank()) {
                userSkills.add(s.trim());
            }
        }

        log.info("[AI] Feature: Job Application Success Predictor - Assessing readiness (ATS: {}, Exp: {} yrs)", atsScore, yearsOfExperience);

        if (geminiService != null && geminiService.isAvailable()) {
            String prompt = """
                    You are an expert Talent Acquisition Strategist. Perform an AI application-readiness assessment for a candidate with the following qualifications:

                    Candidate Profile:
                    - ATS Score: %d / 100
                    - Years of Professional Experience: %d
                    - Verified Projects: %d
                    - Professional Certifications: %d
                    - Stated Core Skills: %s

                    Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations:
                    {
                      "successProbability": <integer 0-100 indicating application readiness estimate>,
                      "strengths": ["Key candidate strength 1", "Strength 2"],
                      "weaknesses": ["Identified gap 1", "Gap 2"],
                      "suggestions": ["Actionable recommendation 1", "Recommendation 2"],
                      "missingSkills": ["Suggested high-yield skill 1", "Skill 2"]
                    }

                    Guidelines:
                    - Evaluate readiness across the candidate's actual skill domain (do not force Java).
                    - This is an AI application-readiness assessment, not a statistical guarantee.
                    """.formatted(atsScore, yearsOfExperience, projects, certifications, userSkills.isEmpty() ? "None specified" : String.join(", ", userSkills));

            try {
                log.info("[AI] Calling Gemini for Job Application Success Prediction...");
                String jsonResponse = geminiService.askGeminiJson(prompt);
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(jsonResponse);

                    int score = root.has("successProbability") ? Math.min(100, Math.max(0, root.get("successProbability").asInt())) : 70;
                    List<String> strengths = extractStringList(root, "strengths");
                    List<String> weaknesses = extractStringList(root, "weaknesses");
                    List<String> suggestions = extractStringList(root, "suggestions");
                    List<String> missingSkills = extractStringList(root, "missingSkills");

                    if (!strengths.isEmpty() || !suggestions.isEmpty()) {
                        log.info("[AI] Gemini JSON parsed successfully for Job Application Success Prediction (score: {})", score);
                        JobSuccessPredictionResponse response = new JobSuccessPredictionResponse(score, strengths, weaknesses, suggestions, missingSkills);
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Job Application Success Prediction: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Job Application Success Prediction: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Job Application Success Prediction. Using domain-neutral fallback.");
        }

        return generateDomainNeutralSuccessPrediction(atsScore, yearsOfExperience, projects, certifications, userSkills);
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

    private JobSuccessPredictionResponse generateDomainNeutralSuccessPrediction(
            int atsScore, int yearsOfExperience, int projects, int certifications, List<String> userSkills) {

        int score = 0;
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        // ATS Score (up to 30 points)
        score += atsScore * 30 / 100;
        if (atsScore >= 80) {
            strengths.add("Strong ATS Keyword & Format Alignment");
        } else if (atsScore >= 60) {
            strengths.add("Good Baseline ATS Compatibility");
        } else {
            weaknesses.add("Resume ATS Optimization Needed");
            suggestions.add("Enhance your resume format and keywords to improve automated screening scores.");
        }

        // Experience (up to 25 points)
        if (yearsOfExperience >= 5) {
            score += 25;
            strengths.add("Strong Professional Experience");
        } else if (yearsOfExperience >= 2) {
            score += 18;
            strengths.add("Relevant Industry Experience");
        } else {
            score += Math.min(yearsOfExperience * 6, 12);
            weaknesses.add("Early Career / Limited Experience");
            suggestions.add("Gain practical internship, freelance, or open-source contribution experience.");
        }

        // Projects (up to 25 points)
        if (projects >= 5) {
            score += 25;
            strengths.add("Strong Project Portfolio");
        } else if (projects >= 2) {
            score += 16;
            strengths.add("Active Project Deliverables");
        } else {
            score += Math.min(projects * 6, 10);
            weaknesses.add("Few Documented Projects");
            suggestions.add("Build and publish 2-3 end-to-end capstone projects with public repositories.");
        }

        // Certifications (up to 10 points)
        if (certifications >= 2) {
            score += 10;
            strengths.add("Good Certifications");
        } else if (certifications == 1) {
            score += 6;
            strengths.add("Relevant Industry Certification");
        } else {
            suggestions.add("Consider obtaining recognized cloud or domain certifications to stand out.");
        }

        // Skill Breadth (up to 10 points)
        if (userSkills.size() >= 6) {
            score += 10;
            strengths.add("Broad Technical & Domain Skillset");
        } else if (userSkills.size() >= 3) {
            score += 6;
            strengths.add("Solid Core Domain Competencies");
        } else {
            score += Math.min(userSkills.size() * 2, 4);
            weaknesses.add("Limited Documented Skills");
            suggestions.add("Expand your profile skills with industry-standard tooling and modern frameworks.");
        }

        if (userSkills.size() < 4) {
            missingSkills.add("Cloud / Container Tooling");
            missingSkills.add("Automated Testing Methodologies");
        }

        score = Math.max(15, Math.min(score, 98));

        JobSuccessPredictionResponse response = new JobSuccessPredictionResponse(
                score,
                strengths,
                weaknesses,
                suggestions,
                missingSkills
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}