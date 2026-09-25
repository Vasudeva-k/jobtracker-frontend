package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.SkillGapRequest;
import com.vasudev.jobtracker.dto.SkillGapResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SkillGapAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SkillGapAnalyzer.class);

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SkillGapAnalyzer(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public SkillGapAnalyzer() {
        this.geminiService = null;
    }

    public SkillGapResponse analyze(SkillGapRequest request) {

        String targetRole = (request != null && request.getTargetRole() != null && !request.getTargetRole().isBlank())
                ? request.getTargetRole().trim()
                : "Software Developer";

        String currentSkills = (request != null && request.getCurrentSkills() != null)
                ? request.getCurrentSkills().trim()
                : "";

        boolean geminiAvailable = geminiService != null && geminiService.isAvailable();
        log.info("[AI] Feature: Skill Gap Analyzer");
        log.info("[AI] Gemini available: {}", geminiAvailable);

        if (geminiAvailable) {
            String prompt = """
                    You are an expert Career Transition & Technical Skill Advisor. Analyze the candidate's current skills against the target role: "%s".

                    Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations:
                    {
                      "targetRole": "%s",
                      "existingSkills": ["skill1", "skill2"],
                      "missingSkills": ["skill1", "skill2"],
                      "learningPlan": ["Step 1: Master...", "Step 2: Build..."],
                      "estimatedDuration": "X Weeks"
                    }

                    Guidelines:
                    - Accurately identify skills relevant to "%s" (e.g. if Python Data Scientist, evaluate Python, ML, SQL; do not inject Java unless target role is Java).
                    - Create a sequenced, actionable learning roadmap for the missing skills.

                    Target Role: %s
                    Candidate's Current Skills: %s
                    """.formatted(targetRole, targetRole, targetRole, targetRole, currentSkills.isBlank() ? "None specified" : currentSkills);

            try {
                log.info("[AI] Calling Gemini...");
                String jsonResponse = geminiService.askGeminiJson(prompt);
                log.info("[AI] Gemini response received");
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(jsonResponse);
                    log.info("[AI] Gemini JSON parsed successfully");

                    String role = root.has("targetRole") ? root.get("targetRole").asText() : targetRole;
                    List<String> existing = extractStringList(root, "existingSkills");
                    List<String> missing = extractStringList(root, "missingSkills");
                    List<String> plan = extractStringList(root, "learningPlan");
                    String duration = root.has("estimatedDuration") ? root.get("estimatedDuration").asText() : "4-8 Weeks";

                    if (!existing.isEmpty() || !missing.isEmpty() || !plan.isEmpty()) {
                        SkillGapResponse response = new SkillGapResponse(role, existing, missing, plan, duration);
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException e) {
                log.error("[AI] Gemini API error for Skill Gap Analyzer: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.warn("[AI] Gemini call failed for Skill Gap Analyzer: {}. Using domain-neutral fallback", e.getMessage());
            }
        }

        log.info("[AI] Using domain-neutral fallback");
        return generateDomainNeutralSkillGap(currentSkills, targetRole);
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

    private SkillGapResponse generateDomainNeutralSkillGap(String currentSkillsStr, String targetRole) {
        List<String> existingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        if (!currentSkillsStr.isBlank()) {
            String[] tokens = currentSkillsStr.split("[,;\\n]+");
            for (String token : tokens) {
                String trimmed = token.trim();
                if (!trimmed.isBlank()) {
                    existingSkills.add(trimmed);
                }
            }
        }

        String lowerRole = targetRole.toLowerCase();
        List<String> domainRecommendedSkills;

        if (lowerRole.contains("python") || lowerRole.contains("data") || lowerRole.contains("ml") || lowerRole.contains("machine learning") || lowerRole.contains("ai")) {
            domainRecommendedSkills = List.of("Python", "Pandas", "NumPy", "SQL", "Machine Learning", "Scikit-Learn", "Statistics", "Data Visualization", "Git");
        } else if (lowerRole.contains("react") || lowerRole.contains("frontend") || lowerRole.contains("ui") || lowerRole.contains("javascript")) {
            domainRecommendedSkills = List.of("JavaScript (ES6+)", "TypeScript", "React", "HTML5 & CSS3", "State Management", "REST APIs", "Git");
        } else if (lowerRole.contains("devops") || lowerRole.contains("cloud") || lowerRole.contains("sre") || lowerRole.contains("infrastructure")) {
            domainRecommendedSkills = List.of("Linux", "Docker", "Kubernetes", "CI/CD (GitHub Actions)", "AWS/Cloud", "Terraform", "Monitoring");
        } else if (lowerRole.contains("qa") || lowerRole.contains("test")) {
            domainRecommendedSkills = List.of("Test Automation", "Selenium/Cypress", "API Testing (Postman)", "JUnit/PyTest", "CI/CD Integration");
        } else if (lowerRole.contains("java") || lowerRole.contains("spring")) {
            domainRecommendedSkills = List.of("Java", "Spring Boot", "REST APIs", "Relational Databases (SQL)", "Hibernate/JPA", "Docker", "JUnit");
        } else {
            domainRecommendedSkills = List.of("Core " + targetRole + " Fundamentals", "Industry Tooling & Workflows", "Production Project Implementation", "Collaborative Version Control (Git)");
        }

        String lowerExisting = currentSkillsStr.toLowerCase();
        for (String skill : domainRecommendedSkills) {
            if (!lowerExisting.contains(skill.toLowerCase())) {
                missingSkills.add(skill);
            }
        }

        List<String> learningPlan = new ArrayList<>();
        int step = 1;
        for (String missing : missingSkills) {
            learningPlan.add("Step " + (step++) + ": Learn and build hands-on projects with " + missing + ".");
        }
        if (learningPlan.isEmpty()) {
            learningPlan.add("Step 1: Deepen domain expertise and contribute to advanced " + targetRole + " open-source projects.");
        }

        String estimatedDuration = missingSkills.isEmpty() ? "2-4 Weeks" : (missingSkills.size() * 2) + " Weeks";

        SkillGapResponse response = new SkillGapResponse(
                targetRole,
                existingSkills,
                missingSkills,
                learningPlan,
                estimatedDuration
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}
