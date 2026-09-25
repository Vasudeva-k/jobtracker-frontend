package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.ResumeResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResumeAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(ResumeAnalyzer.class);
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeAnalyzer(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public ResumeResponse analyze(String resumeText) {

        String safeResumeText = resumeText != null ? resumeText.trim() : "";

        if (safeResumeText.isBlank()) {
            return new ResumeResponse(0, List.of(), List.of("Add technical or professional skills"), List.of("Resume is empty. Please provide resume content for evaluation."));
        }

        log.info("[AI] Feature: Resume ATS Analyzer - Evaluating resume content (length: {} chars)", safeResumeText.length());

        String prompt = """
                You are an expert ATS Resume Reviewer. Analyze the following candidate resume across its specific career domain (e.g., Software Engineering, Data Science, Frontend, Mobile, QA, DevOps, Product Management, Finance, Customer Support, etc.).

                Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations.
                {
                  "atsScore": <integer 0-100 assessing structure, depth, relevance, and quantifiable results>,
                  "matchedSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill1", "skill2"],
                  "suggestions": ["suggestion1", "suggestion2"]
                }

                Critical Rules:
                - Do NOT assume the candidate is a Java developer unless the resume explicitly indicates Java background.
                - Evaluate candidate based strictly on their actual domain and level.
                - Guard against keyword stuffing: a list of tech buzzwords without project context, experience details, or quantifiable outcomes must NOT receive a high score (keep under 55).
                - Suggestions must be actionable, focusing on quantifiable metrics, clear section formatting, and domain-relevant enhancements.

                <candidate_resume>
                %s
                </candidate_resume>
                """.formatted(safeResumeText);

        if (geminiService != null && geminiService.isAvailable()) {
            try {
                log.info("[AI] Calling Gemini for Resume ATS Analysis...");
                String jsonResponse = geminiService.askGeminiJson(prompt);
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    String cleanJson = GeminiService.stripMarkdownFences(jsonResponse);
                    JsonNode root = objectMapper.readTree(cleanJson);

                    int atsScore = root.has("atsScore") ? root.get("atsScore").asInt() : 0;
                    atsScore = Math.min(100, Math.max(0, atsScore));

                    List<String> matchedSkills = extractStringList(root, "matchedSkills");
                    List<String> missingSkills = extractStringList(root, "missingSkills");
                    List<String> suggestions = extractStringList(root, "suggestions");

                    if (atsScore > 0 || !matchedSkills.isEmpty() || !suggestions.isEmpty()) {
                        log.info("[AI] Gemini JSON parsed successfully for Resume ATS Analysis (score: {})", atsScore);
                        log.info("[AI] Response source: GEMINI");
                        return new ResumeResponse(atsScore, matchedSkills, missingSkills, suggestions, "GEMINI");
                    }
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Resume ATS Analysis: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Resume ATS Analysis: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Resume ATS Analysis. Using domain-neutral fallback.");
        }

        log.info("[AI] Response source: FALLBACK");
        return generateDomainNeutralAnalysis(safeResumeText);
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

    private ResumeResponse generateDomainNeutralAnalysis(String resumeText) {
        String lower = resumeText.toLowerCase();

        boolean hasExperience = lower.contains("experience") || lower.contains("work history") || lower.contains("employment");
        boolean hasProjects = lower.contains("project") || lower.contains("portfolio");
        boolean hasEducation = lower.contains("education") || lower.contains("degree") || lower.contains("bachelor") || lower.contains("master") || lower.contains("university");
        boolean hasSkills = lower.contains("skill") || lower.contains("technologies") || lower.contains("proficiencies") || lower.contains("competencies");
        boolean hasAchievements = lower.contains("achievement") || lower.contains("award") || lower.contains("certification");

        // Check for quantifiable results (numbers, percentages, metrics)
        Pattern metricPattern = Pattern.compile("\\b\\d+(%|\\+|k|M|x)?\\b", Pattern.CASE_INSENSITIVE);
        Matcher metricMatcher = metricPattern.matcher(resumeText);
        int metricCount = 0;
        while (metricMatcher.find() && metricCount < 10) {
            metricCount++;
        }

        // Check word count and basic sentence structure for keyword stuffing detection
        String[] words = resumeText.split("\\s+");
        int wordCount = words.length;
        int sentenceCount = resumeText.split("[.!?\\n]+").length;
        boolean isKeywordStuffed = wordCount > 30 && (sentenceCount <= 2 || (!hasExperience && !hasProjects && !hasEducation));

        int score = 30; // base score for non-empty text
        if (hasExperience) score += 15;
        if (hasProjects) score += 15;
        if (hasEducation) score += 10;
        if (hasSkills) score += 10;
        if (hasAchievements) score += 5;
        score += Math.min(15, metricCount * 3);

        if (isKeywordStuffed) {
            score = Math.min(50, score - 25);
        }

        score = Math.min(95, Math.max(25, score));

        // Extract potential skills dynamically from resume text
        List<String> matchedSkills = extractKeywordsFromText(resumeText);
        if (matchedSkills.isEmpty()) {
            matchedSkills.add("Core Domain Experience");
        }

        List<String> missingSkills = new ArrayList<>();
        if (!hasExperience) missingSkills.add("Detailed Work Experience / Chronological History");
        if (!hasProjects) missingSkills.add("Demonstrated Projects / Practical Deliverables");
        if (!hasAchievements) missingSkills.add("Certifications / Measurable Achievements");

        List<String> suggestions = new ArrayList<>();
        if (metricCount < 3) {
            suggestions.add("Add measurable business metrics and quantifiable achievements (e.g. improved performance by X%, managed team of Y).");
        }
        if (!hasProjects) {
            suggestions.add("Add a dedicated Projects section detailing technical challenges, solutions, and technologies used.");
        }
        if (isKeywordStuffed) {
            suggestions.add("Restructure keywords into contextual job experience and project descriptions rather than a standalone keyword list.");
        } else {
            suggestions.add("Ensure your resume summary clearly mentions your target position and core domain competencies.");
        }

        return new ResumeResponse(
                score,
                matchedSkills,
                missingSkills,
                suggestions,
                "FALLBACK"
        );
    }

    private List<String> extractKeywordsFromText(String text) {
        Set<String> extracted = new HashSet<>();
        Pattern capitalWordPattern = Pattern.compile("\\b([A-Z][a-zA-Z0-9#+.]+(?:\\s+[A-Z][a-zA-Z0-9#+.]+)?)\\b");
        Matcher matcher = capitalWordPattern.matcher(text);

        Set<String> ignore = Set.of("Resume", "Curriculum", "Vitae", "Experience", "Education", "Projects", "Skills", "Summary", "Objective", "University", "College", "Email", "Phone", "Address", "Date", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

        while (matcher.find() && extracted.size() < 6) {
            String word = matcher.group(1).trim();
            if (!ignore.contains(word) && word.length() > 2) {
                extracted.add(word);
            }
        }
        return new ArrayList<>(extracted);
    }
}