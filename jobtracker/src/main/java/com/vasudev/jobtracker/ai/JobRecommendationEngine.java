package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.JobRecommendationRequest;
import com.vasudev.jobtracker.dto.JobRecommendationResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobRecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(JobRecommendationEngine.class);

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobRecommendationEngine(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public JobRecommendationEngine() {
        this.geminiService = null;
    }

    public JobRecommendationResponse recommend(JobRecommendationRequest request) {
        List<String> resumeSkills = (request != null && request.getResumeSkills() != null)
                ? request.getResumeSkills().stream().filter(s -> s != null && !s.isBlank()).map(String::trim).toList()
                : List.of();

        List<String> jobSkills = (request != null && request.getJobSkills() != null)
                ? request.getJobSkills().stream().filter(s -> s != null && !s.isBlank()).map(String::trim).toList()
                : List.of();

        boolean geminiAvailable = geminiService != null && geminiService.isAvailable();
        log.info("[AI] Feature: Job Recommendation Engine");
        log.info("[AI] Gemini available: {}", geminiAvailable);

        if (geminiAvailable && (!resumeSkills.isEmpty() || !jobSkills.isEmpty())) {
            try {
                log.info("[AI] Calling Gemini...");
                String prompt = """
                        You are an expert talent acquisition and semantic job-matching engine.
                        Compare the candidate's skills with the job requirements.
                        
                        Candidate Skills:
                        %s
                        
                        Job Required Skills:
                        %s
                        
                        Return a valid JSON object matching this exact schema:
                        {
                          "matchScore": <integer 0-100>,
                          "matchedSkills": ["skill1", "skill2"],
                          "missingSkills": ["skill3", "skill4"],
                          "recommendation": "Concise actionable summary for the applicant"
                        }
                        
                        Evaluation guidelines:
                        - Match semantically even with minor syntax/casing differences (e.g., "JS" / "JavaScript", "K8s" / "Kubernetes", "Postgres" / "PostgreSQL").
                        - Match score must be between 0 and 100.
                        """.formatted(
                        resumeSkills.isEmpty() ? "(None provided)" : String.join(", ", resumeSkills),
                        jobSkills.isEmpty() ? "(None provided)" : String.join(", ", jobSkills)
                );

                String jsonStr = geminiService.askGeminiJson(prompt);
                log.info("[AI] Gemini response received");
                JsonNode json = objectMapper.readTree(jsonStr);
                log.info("[AI] Gemini JSON parsed successfully");

                int matchScore = json.has("matchScore") ? json.get("matchScore").asInt() : 0;
                matchScore = Math.max(0, Math.min(100, matchScore));

                List<String> matchedSkills = new ArrayList<>();
                if (json.has("matchedSkills") && json.get("matchedSkills").isArray()) {
                    for (JsonNode node : json.get("matchedSkills")) {
                        matchedSkills.add(node.asText());
                    }
                }

                List<String> missingSkills = new ArrayList<>();
                if (json.has("missingSkills") && json.get("missingSkills").isArray()) {
                    for (JsonNode node : json.get("missingSkills")) {
                        missingSkills.add(node.asText());
                    }
                }

                String recommendation = json.has("recommendation") ? json.get("recommendation").asText() : "";

                JobRecommendationResponse response = new JobRecommendationResponse(
                        matchScore,
                        matchedSkills,
                        missingSkills,
                        recommendation.isBlank() ? "AI application-readiness assessment completed." : recommendation
                );
                response.setSource("GEMINI");
                log.info("[AI] Response source: GEMINI");
                return response;
            } catch (GeminiException e) {
                log.error("[AI] Gemini API error for Job Recommendation Engine: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.warn("[AI] Gemini call failed for Job Recommendation Engine: {}. Using domain-neutral fallback", e.getMessage());
            }
        }

        log.info("[AI] Using domain-neutral fallback");
        return generateRuleBasedRecommendation(resumeSkills, jobSkills);
    }

    private JobRecommendationResponse generateRuleBasedRecommendation(List<String> resumeSkills, List<String> jobSkills) {
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String js : jobSkills) {
            if (js == null || js.isBlank()) continue;
            boolean found = false;
            for (String rs : resumeSkills) {
                if (rs != null && (rs.trim().equalsIgnoreCase(js.trim())
                        || rs.toLowerCase().contains(js.toLowerCase())
                        || js.toLowerCase().contains(rs.toLowerCase()))) {
                    found = true;
                    break;
                }
            }
            if (found) {
                matched.add(js.trim());
            } else {
                missing.add(js.trim());
            }
        }

        int score = jobSkills.isEmpty() ? 75 : Math.min(100, Math.max(20, (matched.size() * 100) / jobSkills.size()));

        String recommendation;
        if (score >= 80) {
            recommendation = "Strong skill match. Recommended for application.";
        } else if (score >= 50) {
            recommendation = "Moderate skill match. Review missing skills before proceeding.";
        } else {
            recommendation = "Low skill match. Upskilling recommended in target technologies.";
        }

        JobRecommendationResponse response = new JobRecommendationResponse(
                score,
                matched,
                missing,
                recommendation
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}