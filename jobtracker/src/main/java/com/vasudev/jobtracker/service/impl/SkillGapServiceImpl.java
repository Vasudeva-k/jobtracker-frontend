package com.vasudev.jobtracker.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.SkillGapResponse;
import com.vasudev.jobtracker.entity.Resume;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.ResumeRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import com.vasudev.jobtracker.service.SkillGapService;
import com.vasudev.jobtracker.util.PdfTextExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class SkillGapServiceImpl implements SkillGapService {

    private static final Logger log = LoggerFactory.getLogger(SkillGapServiceImpl.class);

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final PdfTextExtractor pdfTextExtractor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${file.upload-dir}")
    private String uploadDir;

    public SkillGapServiceImpl(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            GeminiService geminiService,
            PdfTextExtractor pdfTextExtractor) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    @Override
    public SkillGapResponse analyzeSkillGap(
            String targetRole,
            String email) throws Exception {

        String safeRole = (targetRole != null && !targetRole.isBlank()) ? targetRole.trim() : "Software Engineer";
        log.info("[AI] Feature: Resume-linked Skill Gap - User: {}, Target Role: {}", email, safeRole);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Resume resume = resumeRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Please upload a resume first."));

        Path filePath = Paths.get(uploadDir)
                .resolve(resume.getFileName())
                .normalize();

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Resume file not found.");
        }

        String resumeText = pdfTextExtractor.extractText(filePath);

        if (resumeText.isBlank()) {
            throw new RuntimeException(
                    "Unable to extract text from the resume.");
        }

        if (geminiService != null && geminiService.isAvailable()) {
            String prompt = """
                    You are an expert career advisor and technical recruiter.
                    Analyze the candidate's resume against the target job role: "%s".

                    Return ONLY a valid JSON object. Do not include markdown formatting or explanations:
                    {
                      "targetRole": "%s",
                      "existingSkills": ["skill1", "skill2"],
                      "missingSkills": ["skill1", "skill2"],
                      "learningPlan": ["Step 1: ...", "Step 2: ..."],
                      "estimatedDuration": "4-8 Weeks"
                    }

                    Guidelines:
                    - Accurately evaluate existing skills strictly from the resume.
                    - Identify missing skills required specifically for "%s" without biasing toward Java unless relevant.

                    Target Role: %s
                    <candidate_resume>
                    %s
                    </candidate_resume>
                    """.formatted(safeRole, safeRole, safeRole, safeRole, resumeText);

            try {
                log.info("[AI] Calling Gemini for Resume-linked Skill Gap...");
                String aiResponse = geminiService.askGeminiJson(prompt);
                if (aiResponse != null && !aiResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(aiResponse);
                    String role = root.has("targetRole") ? root.get("targetRole").asText() : safeRole;
                    List<String> existing = extractList(root, "existingSkills");
                    List<String> missing = extractList(root, "missingSkills");
                    List<String> plan = extractList(root, "learningPlan");
                    String duration = root.has("estimatedDuration") ? root.get("estimatedDuration").asText() : "4-8 Weeks";

                    if (!existing.isEmpty() || !missing.isEmpty() || !plan.isEmpty()) {
                        log.info("[AI] Gemini JSON parsed successfully for Resume-linked Skill Gap");
                        SkillGapResponse response = new SkillGapResponse(role, existing, missing, plan, duration);
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Resume-linked Skill Gap: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Resume-linked Skill Gap: {}. Using fallback.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Resume-linked Skill Gap. Using domain-neutral fallback.");
        }

        return generateDomainNeutralSkillGap(resumeText, safeRole);
    }

    private List<String> extractList(JsonNode root, String field) {
        List<String> list = new ArrayList<>();
        if (root.has(field) && root.get(field).isArray()) {
            for (JsonNode n : root.get(field)) {
                String text = n.asText().trim();
                if (!text.isBlank()) {
                    list.add(text);
                }
            }
        }
        return list;
    }

    private SkillGapResponse generateDomainNeutralSkillGap(String resumeText, String targetRole) {
        String lowerResume = resumeText.toLowerCase();
        String lowerRole = targetRole.toLowerCase();

        List<String> requiredSkills;
        if (lowerRole.contains("python") || lowerRole.contains("data") || lowerRole.contains("ml") || lowerRole.contains("ai")) {
            requiredSkills = List.of("Python", "SQL", "Pandas", "Scikit-Learn", "Machine Learning", "Docker", "Model Deployment");
        } else if (lowerRole.contains("react") || lowerRole.contains("frontend") || lowerRole.contains("ui")) {
            requiredSkills = List.of("React", "TypeScript", "JavaScript ES6+", "HTML5/CSS3", "REST APIs", "State Management", "Automated UI Testing");
        } else if (lowerRole.contains("devops") || lowerRole.contains("cloud") || lowerRole.contains("sre")) {
            requiredSkills = List.of("Docker", "Kubernetes", "AWS / Cloud Architecture", "CI/CD Pipelines", "Terraform", "Linux");
        } else if (lowerRole.contains("product") || lowerRole.contains("manager") || lowerRole.contains("pm")) {
            requiredSkills = List.of("Product Strategy", "User Story Mapping", "A/B Testing & Analytics", "Roadmap Planning", "Agile/Scrum");
        } else if (lowerRole.contains("java") || lowerRole.contains("spring")) {
            requiredSkills = List.of("Java", "Spring Boot", "REST APIs", "SQL / Relational Databases", "Hibernate/JPA", "Docker", "JUnit");
        } else {
            requiredSkills = List.of("Core " + targetRole + " Methodologies", "Industry Standard Tooling", "Production Project Implementation", "Version Control (Git)");
        }

        List<String> existingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String req : requiredSkills) {
            if (lowerResume.contains(req.toLowerCase())) {
                existingSkills.add(req);
            } else {
                missingSkills.add(req);
            }
        }

        if (existingSkills.isEmpty()) {
            existingSkills.add("Core Domain Experience");
        }

        List<String> learningPlan = new ArrayList<>();
        int step = 1;
        for (String missing : missingSkills) {
            learningPlan.add("Step " + (step++) + ": Learn and build hands-on projects with " + missing + ".");
        }
        if (learningPlan.isEmpty()) {
            learningPlan.add("Step 1: Deepen domain mastery and contribute to production " + targetRole + " implementations.");
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

