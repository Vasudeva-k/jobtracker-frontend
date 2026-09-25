package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.ai.ResumeParser;
import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.service.AIService;
import com.vasudev.jobtracker.service.GeminiService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;
    private final ResumeParser resumeParser;
    private final GeminiService geminiService;

    public AIController(
            AIService aiService,
            ResumeParser resumeParser,
            GeminiService geminiService) {

        this.aiService = aiService;
        this.resumeParser = resumeParser;
        this.geminiService = geminiService;
    }

    // ===================================================
    // GEMINI STATUS & HEALTH DIAGNOSTIC
    // ===================================================
    @GetMapping("/gemini-status")
    public java.util.Map<String, Object> getGeminiStatus() {
        return geminiService.checkGeminiStatus();
    }

    // ===================================================
    // GEMINI LIVE RUNTIME TEST PROBE
    // ===================================================
    @PostMapping("/gemini-runtime-test")
    public java.util.Map<String, Object> runGeminiRuntimeTest(
            @RequestBody(required = false) String rawBody) {
        String token = null;
        if (rawBody != null && !rawBody.isBlank()) {
            String trimmed = rawBody.trim();
            if (trimmed.startsWith("{") && trimmed.contains("\"token\"")) {
                try {
                    com.fasterxml.jackson.databind.JsonNode node =
                            new com.fasterxml.jackson.databind.ObjectMapper().readTree(trimmed);
                    if (node.has("token")) {
                        token = node.get("token").asText();
                    }
                } catch (Exception ignored) {
                }
            } else if (!trimmed.startsWith("{")) {
                token = trimmed;
            }
        }
        return geminiService.executeLiveRuntimeProbe(token);
    }

    // ===================================================
    // TEST GEMINI API
    // ===================================================
    @PostMapping("/gemini")
    public String askGemini(@RequestBody String prompt) {
        return geminiService.askGemini(prompt);
    }

    // ===================================================
    // AI Resume vs Job Description Matcher
    // ===================================================
    @PostMapping("/resume-match")
    public JobMatchResponse matchResume(
            @RequestBody JobMatchRequest request) {

        return aiService.matchResume(request);
    }

    // ===================================================
    // AI Resume PDF Upload + Matcher
    // ===================================================
    @PostMapping(
            value = "/resume-match-pdf",
            consumes = "multipart/form-data")
    public JobMatchResponse matchResumePdf(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jobDescription") String jobDescription) {

        String resumeText = resumeParser.extractText(resume);

        JobMatchRequest request = new JobMatchRequest(
                resumeText,
                jobDescription
        );

        return aiService.matchResume(request);
    }

    // ===================================================
    // AI Resume Analysis
    // ===================================================
    @PostMapping(
            value = "/resume-analysis",
            consumes = "multipart/form-data")
    public ResumeResponse analyzeResume(
            @RequestParam("resume") MultipartFile resume) {

        String resumeText = resumeParser.extractText(resume);

        return aiService.analyzeResume(resumeText);
    }

    // ===================================================
    // AI Cover Letter
    // ===================================================
    @PostMapping("/cover-letter")
    public CoverLetterResponse generateCoverLetter(
            @RequestBody CoverLetterRequest request) {

        return aiService.generateCoverLetter(request);
    }

    // ===================================================
    // AI Interview Questions
    // ===================================================
    @PostMapping("/interview-questions")
    public InterviewQuestionResponse generateInterviewQuestions(
            @RequestBody InterviewQuestionRequest request) {

        return aiService.generateInterviewQuestions(request);
    }

    // ===================================================
    // AI Job Recommendation
    // ===================================================
    @PostMapping("/job-recommendation")
    public JobRecommendationResponse recommendJobs(
            @RequestBody JobRecommendationRequest request) {

        return aiService.recommendJobs(request);
    }

    // ===================================================
    // AI Career Roadmap
    // ===================================================
    @PostMapping("/career-roadmap")
    public CareerRoadmapResponse generateCareerRoadmap(
            @RequestBody CareerRoadmapRequest request) {

        return aiService.generateCareerRoadmap(request);
    }

    // ===================================================
    // AI Salary Predictor
    // ===================================================
    @PostMapping("/salary-predictor")
    public SalaryPredictionResponse predictSalary(
            @RequestBody SalaryPredictionRequest request) {

        return aiService.predictSalary(request);
    }

    // ===================================================
    // AI Mock Interview
    // ===================================================
    @PostMapping("/mock-interview")
    public MockInterviewResponse evaluateInterview(
            @RequestBody MockInterviewRequest request) {

        return aiService.evaluateInterview(request);
    }

    // ===================================================
    // AI Job Market Trends
    // ===================================================
    @PostMapping("/job-market-trends")
    public JobMarketTrendResponse analyzeJobMarket(
            @RequestBody JobMarketTrendRequest request) {

        return aiService.analyzeJobMarket(request);
    }

    // ===================================================
    // AI Resume Keyword Optimizer
    // ===================================================
    @PostMapping("/resume-keyword-optimizer")
    public ResumeKeywordResponse optimizeResumeKeywords(
            @RequestBody ResumeKeywordRequest request) {

        return aiService.optimizeResumeKeywords(request);
    }

    // ===================================================
    // AI ATS Score
    // ===================================================
    @PostMapping("/ats-score")
    public ATSScoreResponse predictATSScore(
            @RequestBody ATSScoreRequest request) {

        return aiService.predictATSScore(request);
    }

    // ===================================================
    // AI Company Interview Experience
    // ===================================================
    @PostMapping("/company-interview")
    public CompanyInterviewResponse generateInterviewExperience(
            @RequestBody CompanyInterviewRequest request) {

        return aiService.generateInterviewExperience(request);
    }

    // ===================================================
    // AI Skill Gap
    // ===================================================
    @PostMapping("/skill-gap")
    public SkillGapResponse analyzeSkillGap(
            @RequestBody SkillGapRequest request) {

        return aiService.analyzeSkillGap(request);
    }

    // ===================================================
    // AI Job Success Predictor
    // ===================================================
    @PostMapping("/job-success-predictor")
    public JobSuccessPredictionResponse predictJobSuccess(
            @RequestBody JobSuccessPredictionRequest request) {

        return aiService.predictJobSuccess(request);
    }

    // ===================================================
    // AI Job Success Predictor - From Saved User Profile
    // ===================================================
    @PostMapping("/job-success-predictor/profile")
    public JobSuccessPredictionResponse predictJobSuccessFromProfile(
            Authentication authentication) {

        return aiService.predictJobSuccessFromProfile(
                authentication.getName()
        );
    }
}