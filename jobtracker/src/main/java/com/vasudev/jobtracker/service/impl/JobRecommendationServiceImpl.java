package com.vasudev.jobtracker.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.JobRecommendationResponse;
import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.Resume;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.ResumeRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.GeminiService;
import com.vasudev.jobtracker.service.JobRecommendationService;
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
public class JobRecommendationServiceImpl
        implements JobRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(JobRecommendationServiceImpl.class);

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobApplicationRepository jobRepository;
    private final GeminiService geminiService;
    private final PdfTextExtractor pdfTextExtractor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${file.upload-dir}")
    private String uploadDir;

    public JobRecommendationServiceImpl(
            UserRepository userRepository,
            ResumeRepository resumeRepository,
            JobApplicationRepository jobRepository,
            GeminiService geminiService,
            PdfTextExtractor pdfTextExtractor) {

        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.geminiService = geminiService;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    @Override
    public JobRecommendationResponse recommendJobs(
            String email) throws Exception {

        log.info("[AI] Feature: Database Job Recommendations - User: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Resume resume = resumeRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Please upload a resume first."));

        Path resumePath = Paths.get(uploadDir)
                .resolve(resume.getFileName())
                .normalize();

        if (!Files.exists(resumePath)) {
            throw new RuntimeException(
                    "Resume file not found.");
        }

        String resumeText = pdfTextExtractor.extractText(resumePath);

        if (resumeText.isBlank()) {
            throw new RuntimeException(
                    "Unable to extract text from resume.");
        }

        List<JobApplication> jobs =
                jobRepository.findAll();

        if (jobs.isEmpty()) {
            throw new RuntimeException(
                    "No jobs available for recommendation.");
        }

        StringBuilder jobDetails = new StringBuilder();

        for (JobApplication job : jobs) {
            jobDetails.append("""
                    Job ID: %d
                    Company: %s
                    Role: %s
                    Description: %s
                    Location: %s
                    Status: %s
                    ---
                    """.formatted(
                    job.getId(),
                    job.getCompanyName(),
                    job.getJobTitle(),
                    job.getJobDescription() != null ? job.getJobDescription() : "",
                    job.getLocation() != null ? job.getLocation() : "",
                    job.getStatus() != null ? job.getStatus() : ""
            ));
        }

        if (geminiService != null && geminiService.isAvailable()) {
            String prompt = """
                    You are an expert technical recruiter and career placement advisor.
                    Analyze the candidate resume against the available jobs in the database.

                    Candidate Resume:
                    %s

                    Available Jobs:
                    %s

                    Select the SINGLE BEST matching job.

                    Return ONLY a valid JSON object. Do not include markdown code blocks or explanations:
                    {
                      "matchScore": <integer 0-100 representing honest semantic match>,
                      "matchedSkills": ["skill1", "skill2"],
                      "missingSkills": ["skill1", "skill2"],
                      "recommendation": "Detailed actionable recommendation explaining why this specific job is the best match and how the candidate should prepare."
                    }
                    """.formatted(resumeText, jobDetails.toString());

            try {
                log.info("[AI] Calling Gemini for Database Job Recommendations...");
                String aiResponse = geminiService.askGeminiJson(prompt);
                if (aiResponse != null && !aiResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(aiResponse);
                    int matchScore = root.has("matchScore") ? Math.min(100, Math.max(0, root.get("matchScore").asInt())) : 75;
                    List<String> matched = extractList(root, "matchedSkills");
                    List<String> missing = extractList(root, "missingSkills");
                    String recommendation = root.has("recommendation") ? root.get("recommendation").asText() : "";

                    if (!recommendation.isBlank() || !matched.isEmpty()) {
                        log.info("[AI] Gemini JSON parsed successfully for Database Job Recommendations");
                        JobRecommendationResponse response = new JobRecommendationResponse(
                                matchScore,
                                matched,
                                missing,
                                recommendation.isBlank() ? "AI application-readiness assessment completed." : recommendation
                        );
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Database Job Recommendations: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Database Job Recommendations: {}. Using fallback.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Database Job Recommendations. Using domain-neutral fallback.");
        }

        return generateDomainNeutralRecommendation(resumeText, jobs);
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

    private JobRecommendationResponse generateDomainNeutralRecommendation(String resumeText, List<JobApplication> jobs) {
        String lowerResume = resumeText.toLowerCase();

        JobApplication bestJob = jobs.get(0);
        int maxMatches = -1;
        List<String> bestMatched = new ArrayList<>();
        List<String> bestMissing = new ArrayList<>();

        for (JobApplication job : jobs) {
            String combinedJobText = (job.getJobTitle() + " " + (job.getJobDescription() != null ? job.getJobDescription() : "")).toLowerCase();
            List<String> matched = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            String[] jobWords = combinedJobText.split("[^a-zA-Z0-9#+.-]+");
            for (String w : jobWords) {
                if (w.length() >= 4 && !isStopWord(w)) {
                    if (lowerResume.contains(w)) {
                        if (!matched.contains(w) && matched.size() < 6) {
                            matched.add(w);
                        }
                    } else {
                        if (!missing.contains(w) && missing.size() < 5) {
                            missing.add(w);
                        }
                    }
                }
            }

            if (matched.size() > maxMatches) {
                maxMatches = matched.size();
                bestJob = job;
                bestMatched = matched;
                bestMissing = missing;
            }
        }

        int score = Math.min(95, Math.max(35, 45 + (bestMatched.size() * 10)));
        String rec = "Recommended Position: " + bestJob.getJobTitle() + " at " + bestJob.getCompanyName() +
                ". Your background shows strong alignment with their core role competencies.";

        JobRecommendationResponse response = new JobRecommendationResponse(
                score,
                bestMatched.isEmpty() ? List.of("Core Domain Competency") : bestMatched,
                bestMissing,
                rec
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }

    private boolean isStopWord(String word) {
        return List.of("with", "have", "this", "that", "from", "they", "will", "your", "about", "their", "description", "requirements", "responsibilities").contains(word);
    }
}

