package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.JobMatchRequest;
import com.vasudev.jobtracker.dto.JobMatchResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResumeJobMatcher {

    private static final Logger log = LoggerFactory.getLogger(ResumeJobMatcher.class);
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeJobMatcher(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public JobMatchResponse match(JobMatchRequest request) {

        String resumeText = (request != null && request.getResumeText() != null) ? request.getResumeText().trim() : "";
        String jobDescription = (request != null && request.getJobDescription() != null) ? request.getJobDescription().trim() : "";

        if (resumeText.isBlank() || jobDescription.isBlank()) {
            return new JobMatchResponse(0, List.of(), List.of("Provide both resume and job description"), List.of("Both resume and job description are required for matching."));
        }

        log.info("[AI] Feature: Resume vs Job Description Matcher - Comparing candidate resume and job description");

        String prompt = """
                You are an expert ATS Resume Matcher. Compare the candidate's resume against the target job description across their respective domain.

                Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations.
                {
                  "atsScore": <integer 0-100 representing honest semantic and qualification match percentage>,
                  "matchedSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill1", "skill2"],
                  "suggestions": ["suggestion1", "suggestion2"]
                }

                Critical Rules:
                - Do NOT default or bias towards Java/Spring Boot unless the job description explicitly requires them.
                - An unrelated resume (e.g. Graphic Designer vs Data Scientist) MUST receive a genuinely low match score (< 30).
                - Identify technical and domain skills actually mentioned in the job description and evaluate their presence in the resume.
                - Provide concrete tailoring suggestions to bridge any identified gaps.

                <candidate_resume>
                %s
                </candidate_resume>

                <target_job_description>
                %s
                </target_job_description>
                """.formatted(resumeText, jobDescription);

        if (geminiService != null && geminiService.isAvailable()) {
            try {
                log.info("[AI] Calling Gemini for Resume vs Job Description Match...");
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
                        log.info("[AI] Gemini JSON parsed successfully for Resume vs Job Description Match (score: {})", atsScore);
                        log.info("[AI] Response source: GEMINI");
                        return new JobMatchResponse(atsScore, matchedSkills, missingSkills, suggestions, "GEMINI");
                    }
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Resume vs Job Description Match: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Resume vs Job Description Match: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Resume vs Job Description Match. Using domain-neutral fallback.");
        }

        log.info("[AI] Response source: FALLBACK");
        return generateDomainNeutralJobMatch(resumeText, jobDescription);
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

    private JobMatchResponse generateDomainNeutralJobMatch(String resumeText, String jobDescription) {
        String lowerResume = resumeText.toLowerCase();
        String lowerJd = jobDescription.toLowerCase();

        Set<String> jdKeywords = extractKeywordsFromText(jobDescription);
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String kw : jdKeywords) {
            if (lowerResume.contains(kw.toLowerCase())) {
                matchedSkills.add(kw);
            } else {
                missingSkills.add(kw);
            }
        }

        int score = 35;
        if (!jdKeywords.isEmpty()) {
            score = Math.min(95, Math.max(15, (matchedSkills.size() * 100) / jdKeywords.size()));
        }

        List<String> suggestions = new ArrayList<>();
        if (!missingSkills.isEmpty()) {
            suggestions.add("Incorporate high-priority keywords from the job description: " + String.join(", ", missingSkills.subList(0, Math.min(3, missingSkills.size()))));
        }
        suggestions.add("Ensure project achievements demonstrate business outcomes matching the job requirements.");

        return new JobMatchResponse(
                score,
                matchedSkills,
                missingSkills,
                suggestions,
                "FALLBACK"
        );
    }

    private Set<String> extractKeywordsFromText(String text) {
        Set<String> extracted = new LinkedHashSet<>();
        Pattern capitalWordPattern = Pattern.compile("\\b([A-Z][a-zA-Z0-9#+.]+(?:\\s+[A-Z][a-zA-Z0-9#+.]+)?)\\b");
        Matcher matcher = capitalWordPattern.matcher(text);

        Set<String> ignore = Set.of(
                "The", "And", "With", "For", "You", "Our", "We", "Will", "Are", "Must",
                "Have", "Role", "Job", "Description", "Requirements", "Responsibilities",
                "Qualifications", "Benefits", "Company", "About", "Team", "Work", "Looking"
        );

        while (matcher.find() && extracted.size() < 12) {
            String word = matcher.group(1).trim();
            if (!ignore.contains(word) && word.length() > 2) {
                extracted.add(word);
            }
        }
        return extracted;
    }
}