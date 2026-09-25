package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.controller.AIChatController;
import com.vasudev.jobtracker.controller.ResumeAIController;
import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.exception.GeminiQuotaExceededException;
import com.vasudev.jobtracker.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiQuotaExceptionTest {

    @Mock
    private GeminiService geminiService;

    private CoverLetterGenerator coverLetterGenerator;
    private InterviewQuestionGenerator interviewQuestionGenerator;
    private CareerRoadmapGenerator careerRoadmapGenerator;
    private SkillGapAnalyzer skillGapAnalyzer;
    private SalaryPredictor salaryPredictor;
    private ResumeJobMatcher resumeJobMatcher;
    private ResumeAnalyzer resumeAnalyzer;
    private ATSScorePredictor atsScorePredictor;
    private ResumeKeywordOptimizer resumeKeywordOptimizer;
    private JobMarketTrendAnalyzer jobMarketTrendAnalyzer;
    private MockInterviewSimulator mockInterviewSimulator;
    private JobApplicationSuccessPredictor jobApplicationSuccessPredictor;
    private CompanyInterviewExperienceGenerator companyInterviewExperienceGenerator;
    private JobRecommendationEngine jobRecommendationEngine;
    private AIChatController aiChatController;
    private ResumeAIController resumeAIController;

    @BeforeEach
    void setUp() {
        coverLetterGenerator = new CoverLetterGenerator(geminiService);
        interviewQuestionGenerator = new InterviewQuestionGenerator(geminiService);
        careerRoadmapGenerator = new CareerRoadmapGenerator(geminiService);
        skillGapAnalyzer = new SkillGapAnalyzer(geminiService);
        salaryPredictor = new SalaryPredictor(geminiService);
        resumeJobMatcher = new ResumeJobMatcher(geminiService);
        resumeAnalyzer = new ResumeAnalyzer(geminiService);
        atsScorePredictor = new ATSScorePredictor(geminiService);
        resumeKeywordOptimizer = new ResumeKeywordOptimizer(geminiService);
        jobMarketTrendAnalyzer = new JobMarketTrendAnalyzer(geminiService);
        mockInterviewSimulator = new MockInterviewSimulator(geminiService);
        jobApplicationSuccessPredictor = new JobApplicationSuccessPredictor(geminiService);
        companyInterviewExperienceGenerator = new CompanyInterviewExperienceGenerator(geminiService);
        jobRecommendationEngine = new JobRecommendationEngine(geminiService);
        aiChatController = new AIChatController(geminiService);
        resumeAIController = new ResumeAIController(geminiService);
    }

    private void mockGemini429() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString()))
                .thenThrow(new GeminiQuotaExceededException("Gemini AI has reached its usage limit. Please try again later."));
    }

    private void mockGeminiJson429() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString()))
                .thenThrow(new GeminiQuotaExceededException("Gemini AI has reached its usage limit. Please try again later."));
    }

    @Test
    void testCoverLetterGenerator_ThrowsQuotaExceeded() {
        mockGemini429();
        CoverLetterRequest req = new CoverLetterRequest();
        req.setJobRole("Backend Engineer");
        req.setCompanyName("Acme Corp");

        GeminiQuotaExceededException ex = assertThrows(GeminiQuotaExceededException.class, () ->
                coverLetterGenerator.generateResponse(req)
        );
        assertEquals("GEMINI_QUOTA_EXCEEDED", ex.getErrorCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getHttpStatus());
    }

    @Test
    void testInterviewQuestionGenerator_ThrowsQuotaExceeded() {
        mockGemini429();
        InterviewQuestionRequest req = new InterviewQuestionRequest();
        req.setJobRole("Frontend Lead");
        req.setSkills("React, TypeScript");

        assertThrows(GeminiQuotaExceededException.class, () ->
                interviewQuestionGenerator.generateQuestions(req)
        );
    }

    @Test
    void testCareerRoadmapGenerator_ThrowsQuotaExceeded() {
        mockGemini429();
        CareerRoadmapRequest req = new CareerRoadmapRequest("DevOps Engineer");

        assertThrows(GeminiQuotaExceededException.class, () ->
                careerRoadmapGenerator.generate(req)
        );
    }

    @Test
    void testSkillGapAnalyzer_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        SkillGapRequest req = new SkillGapRequest();
        req.setTargetRole("Data Scientist");
        req.setCurrentSkills("Python, SQL");

        assertThrows(GeminiQuotaExceededException.class, () ->
                skillGapAnalyzer.analyze(req)
        );
    }

    @Test
    void testSalaryPredictor_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        SalaryPredictionRequest req = new SalaryPredictionRequest(3, List.of("Java", "AWS"), "Remote");

        assertThrows(GeminiQuotaExceededException.class, () ->
                salaryPredictor.predict(req)
        );
    }

    @Test
    void testResumeJobMatcher_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        JobMatchRequest req = new JobMatchRequest("Experienced Engineer with Java and SQL", "Looking for Java Engineer");

        assertThrows(GeminiQuotaExceededException.class, () ->
                resumeJobMatcher.match(req)
        );
    }

    @Test
    void testResumeAnalyzer_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        assertThrows(GeminiQuotaExceededException.class, () ->
                resumeAnalyzer.analyze("Experienced Software Engineer with 5 years experience in building APIs")
        );
    }

    @Test
    void testATSScorePredictor_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        ATSScoreRequest req = new ATSScoreRequest();
        req.setResumeText("Experience at TechCorp building backend services.");

        assertThrows(GeminiQuotaExceededException.class, () ->
                atsScorePredictor.predict(req)
        );
    }

    @Test
    void testResumeKeywordOptimizer_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        ResumeKeywordRequest req = new ResumeKeywordRequest();
        req.setTargetRole("Cloud Architect");
        req.setResumeText("Experience with AWS, Docker, Kubernetes.");

        assertThrows(GeminiQuotaExceededException.class, () ->
                resumeKeywordOptimizer.optimize(req)
        );
    }

    @Test
    void testJobMarketTrendAnalyzer_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        JobMarketTrendRequest req = new JobMarketTrendRequest("Full Stack Developer");

        assertThrows(GeminiQuotaExceededException.class, () ->
                jobMarketTrendAnalyzer.analyze(req)
        );
    }

    @Test
    void testMockInterviewSimulator_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        MockInterviewRequest req = new MockInterviewRequest();
        req.setRole("Security Engineer");
        req.setAnswers(List.of("I implement OAuth2 and mTLS for service communications."));

        assertThrows(GeminiQuotaExceededException.class, () ->
                mockInterviewSimulator.evaluate(req)
        );
    }

    @Test
    void testJobApplicationSuccessPredictor_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        JobSuccessPredictionRequest req = new JobSuccessPredictionRequest(85, 3, 4, 2, List.of("Java", "Spring"));

        assertThrows(GeminiQuotaExceededException.class, () ->
                jobApplicationSuccessPredictor.predict(req)
        );
    }

    @Test
    void testCompanyInterviewExperienceGenerator_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        CompanyInterviewRequest req = new CompanyInterviewRequest("Google", "Software Engineer");

        assertThrows(GeminiQuotaExceededException.class, () ->
                companyInterviewExperienceGenerator.generate(req)
        );
    }

    @Test
    void testJobRecommendationEngine_ThrowsQuotaExceeded() {
        mockGeminiJson429();
        JobRecommendationRequest req = new JobRecommendationRequest(List.of("Python", "Django"), List.of("Python", "FastAPI"));

        assertThrows(GeminiQuotaExceededException.class, () ->
                jobRecommendationEngine.recommend(req)
        );
    }

    @Test
    void testAIChatController_ThrowsQuotaExceeded() {
        mockGemini429();
        assertThrows(GeminiQuotaExceededException.class, () ->
                aiChatController.chatJson("{\"message\": \"How can I prepare for Java interview?\"}")
        );
    }

    @Test
    void testResumeAIController_ThrowsQuotaExceeded() {
        mockGemini429();
        assertThrows(GeminiQuotaExceededException.class, () ->
                resumeAIController.reviewResume("Software engineer resume sample content.")
        );
    }
}
