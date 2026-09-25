package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.ResumeKeywordRequest;
import com.vasudev.jobtracker.dto.ResumeKeywordResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResumeKeywordOptimizer {

    private static final Logger log = LoggerFactory.getLogger(ResumeKeywordOptimizer.class);

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeKeywordOptimizer(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public ResumeKeywordOptimizer() {
        this.geminiService = null;
    }

    public ResumeKeywordResponse optimize(ResumeKeywordRequest request) {

        String resume = (request != null && request.getResumeText() != null) ? request.getResumeText().trim() : "";
        String targetRole = (request != null && request.getTargetRole() != null && !request.getTargetRole().isBlank())
                ? request.getTargetRole().trim()
                : "Professional";

        if (resume.isBlank()) {
            return new ResumeKeywordResponse(
                    targetRole,
                    List.of(),
                    List.of("Relevant industry keywords for " + targetRole),
                    List.of("Please provide resume text to perform keyword optimization."),
                    0
            );
        }

        boolean geminiAvailable = geminiService != null && geminiService.isAvailable();
        log.info("[AI] Feature: Resume Keyword Optimizer");
        log.info("[AI] Gemini available: {}", geminiAvailable);

        if (geminiAvailable) {
            String prompt = """
                    You are an expert Resume ATS Keyword Strategist. Analyze the candidate resume for the target role: "%s".

                    Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations:
                    {
                      "targetRole": "%s",
                      "presentKeywords": ["keyword1", "keyword2"],
                      "missingKeywords": ["keyword1", "keyword2"],
                      "recommendedKeywords": ["suggestion or project integration tip 1", "tip 2"],
                      "optimizationScore": <integer 0-100>
                    }

                    Guidelines:
                    - Extract keywords specifically relevant to the target role "%s" (do not default to Java unless requested).
                    - Identify high-impact skills, frameworks, tools, and methodologies.

                    <candidate_resume>
                    %s
                    </candidate_resume>
                    """.formatted(targetRole, targetRole, targetRole, resume);

            try {
                log.info("[AI] Calling Gemini...");
                String jsonResponse = geminiService.askGeminiJson(prompt);
                log.info("[AI] Gemini response received");
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(jsonResponse);
                    log.info("[AI] Gemini JSON parsed successfully");

                    String role = root.has("targetRole") ? root.get("targetRole").asText() : targetRole;
                    List<String> present = extractStringList(root, "presentKeywords");
                    List<String> missing = extractStringList(root, "missingKeywords");
                    List<String> recommended = extractStringList(root, "recommendedKeywords");
                    int score = root.has("optimizationScore") ? Math.min(100, Math.max(0, root.get("optimizationScore").asInt())) : 75;

                    if (!present.isEmpty() || !missing.isEmpty() || !recommended.isEmpty()) {
                        ResumeKeywordResponse response = new ResumeKeywordResponse(role, present, missing, recommended, score);
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException e) {
                log.error("[AI] Gemini API error for Resume Keyword Optimizer: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.warn("[AI] Gemini call failed for Resume Keyword Optimizer: {}. Using domain-neutral fallback", e.getMessage());
            }
        }

        log.info("[AI] Using domain-neutral fallback");
        return generateDomainNeutralOptimization(resume, targetRole);
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

    private ResumeKeywordResponse generateDomainNeutralOptimization(String resume, String targetRole) {
        List<String> presentKeywords = new ArrayList<>(extractKeywordsFromText(resume));
        List<String> missingKeywords = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        String roleLower = targetRole.toLowerCase();

        if (roleLower.contains("python") || roleLower.contains("data") || roleLower.contains("ml")) {
            missingKeywords.addAll(List.of("Python", "Pandas", "SQL", "Machine Learning", "Docker"));
        } else if (roleLower.contains("react") || roleLower.contains("frontend") || roleLower.contains("javascript")) {
            missingKeywords.addAll(List.of("React", "TypeScript", "State Management", "REST APIs", "CSS3"));
        } else if (roleLower.contains("devops") || roleLower.contains("cloud")) {
            missingKeywords.addAll(List.of("Docker", "Kubernetes", "CI/CD", "AWS", "Terraform"));
        } else if (roleLower.contains("java") || roleLower.contains("spring")) {
            missingKeywords.addAll(List.of("Java 21", "Spring Boot", "Hibernate", "REST APIs", "Microservices"));
        } else {
            missingKeywords.addAll(List.of("Domain Methodologies", "System Design", "Quality Assurance", "Agile / Scrum"));
        }

        // Filter out what's already present
        missingKeywords.removeIf(m -> presentKeywords.stream().anyMatch(p -> p.equalsIgnoreCase(m)));

        if (missingKeywords.isEmpty()) {
            missingKeywords.add("Cloud / Modern Collaboration Tools");
        }

        for (String missing : missingKeywords) {
            recommendations.add("Highlight projects, certifications, or accomplishments demonstrating " + missing + ".");
        }
        recommendations.add("Ensure technical and role-specific keywords appear organically within project bullet points.");

        int score = Math.min(95, Math.max(30, presentKeywords.size() * 12));

        ResumeKeywordResponse response = new ResumeKeywordResponse(
                targetRole,
                presentKeywords,
                missingKeywords,
                recommendations,
                score
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }

    private Set<String> extractKeywordsFromText(String text) {
        Set<String> extracted = new LinkedHashSet<>();
        Pattern capitalWordPattern = Pattern.compile("\\b([A-Z][a-zA-Z0-9#+.]+(?:\\s+[A-Z][a-zA-Z0-9#+.]+)?)\\b");
        Matcher matcher = capitalWordPattern.matcher(text);

        Set<String> ignore = Set.of(
                "Resume", "Curriculum", "Vitae", "Experience", "Education", "Projects",
                "Skills", "Summary", "Objective", "University", "College", "Email", "Phone",
                "Address", "January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"
        );

        while (matcher.find() && extracted.size() < 8) {
            String word = matcher.group(1).trim();
            if (!ignore.contains(word) && word.length() > 2) {
                extracted.add(word);
            }
        }
        return extracted;
    }
}