package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.controller.AIChatController;
import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.entity.Resume;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.repository.ResumeRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.GeminiService;
import com.vasudev.jobtracker.service.impl.JobRecommendationServiceImpl;
import com.vasudev.jobtracker.service.impl.SkillGapServiceImpl;
import com.vasudev.jobtracker.util.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class All20AIToolsGeminiVerificationTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobApplicationRepository jobRepository;

    @Mock
    private PdfTextExtractor pdfTextExtractor;

    @TempDir
    Path tempDir;

    private ResumeAnalyzer resumeAnalyzer;
    private ResumeJobMatcher resumeJobMatcher;
    private ATSScorePredictor atsScorePredictor;
    private CoverLetterGenerator coverLetterGenerator;
    private InterviewQuestionGenerator interviewQuestionGenerator;
    private CompanyInterviewExperienceGenerator companyInterviewExperienceGenerator;
    private CareerRoadmapGenerator careerRoadmapGenerator;
    private SkillGapAnalyzer skillGapAnalyzer;
    private SkillGapServiceImpl skillGapService;
    private SalaryPredictor salaryPredictor;
    private JobApplicationSuccessPredictor jobApplicationSuccessPredictor;
    private MockInterviewSimulator mockInterviewSimulator;
    private JobMarketTrendAnalyzer jobMarketTrendAnalyzer;
    private ResumeKeywordOptimizer resumeKeywordOptimizer;
    private AIChatController aiChatController;
    private JobRecommendationEngine jobRecommendationEngine;
    private JobRecommendationServiceImpl jobRecommendationService;

    private User testUser;
    private Resume testResume;

    @BeforeEach
    void setUp() throws Exception {
        resumeAnalyzer = new ResumeAnalyzer(geminiService);
        resumeJobMatcher = new ResumeJobMatcher(geminiService);
        atsScorePredictor = new ATSScorePredictor(geminiService);
        coverLetterGenerator = new CoverLetterGenerator(geminiService);
        interviewQuestionGenerator = new InterviewQuestionGenerator(geminiService);
        companyInterviewExperienceGenerator = new CompanyInterviewExperienceGenerator(geminiService);
        careerRoadmapGenerator = new CareerRoadmapGenerator(geminiService);
        skillGapAnalyzer = new SkillGapAnalyzer(geminiService);
        skillGapService = new SkillGapServiceImpl(resumeRepository, userRepository, geminiService, pdfTextExtractor);
        salaryPredictor = new SalaryPredictor(geminiService);
        jobApplicationSuccessPredictor = new JobApplicationSuccessPredictor(geminiService);
        mockInterviewSimulator = new MockInterviewSimulator(geminiService);
        jobMarketTrendAnalyzer = new JobMarketTrendAnalyzer(geminiService);
        resumeKeywordOptimizer = new ResumeKeywordOptimizer(geminiService);
        aiChatController = new AIChatController(geminiService);
        jobRecommendationEngine = new JobRecommendationEngine(geminiService);
        jobRecommendationService = new JobRecommendationServiceImpl(userRepository, resumeRepository, jobRepository, geminiService, pdfTextExtractor);

        ReflectionTestUtils.setField(skillGapService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(jobRecommendationService, "uploadDir", tempDir.toString());

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("developer@example.com");

        testResume = new Resume();
        testResume.setId(10L);
        testResume.setUser(testUser);
        testResume.setFileName("test-resume.pdf");

        Files.writeString(tempDir.resolve("test-resume.pdf"), "Dummy PDF content");
    }

    // ==========================================
    // 1. Resume ATS Analyzer
    // ==========================================
    @Test
    @DisplayName("Tool 1: Resume ATS Analyzer calls Gemini and parses structured JSON")
    void testResumeATSAnalyzer_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "atsScore": 88,
                  "matchedSkills": ["Python", "Docker", "AWS"],
                  "missingSkills": ["Kubernetes"],
                  "suggestions": ["Add CI/CD pipeline metrics"]
                }
                """);

        ResumeResponse result = resumeAnalyzer.analyze("Python, Docker, AWS developer resume text with metrics");
        assertNotNull(result);
        assertEquals(88, result.getAtsScore());
        assertTrue(result.getMatchedSkills().contains("Python"));
        assertTrue(result.getMissingSkills().contains("Kubernetes"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 2. Resume vs Job Description Matcher
    // ==========================================
    @Test
    @DisplayName("Tool 4: Resume vs JD Matcher calls Gemini and computes match score")
    void testResumeJobMatcher_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "atsScore": 85,
                  "matchedSkills": ["Python", "Docker", "AWS"],
                  "missingSkills": ["Kubernetes"],
                  "suggestions": ["Highlight container orchestration experience"]
                }
                """);

        JobMatchResponse result = resumeJobMatcher.match(new JobMatchRequest("Python Docker AWS resume", "Looking for Python, Docker, AWS, Kubernetes engineer"));
        assertNotNull(result);
        assertEquals(85, result.getAtsScore());
        assertTrue(result.getMatchedSkills().contains("Python"));
        assertTrue(result.getMissingSkills().contains("Kubernetes"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 3. ATS Score Predictor
    // ==========================================
    @Test
    @DisplayName("Tool 6: ATS Score Predictor calls Gemini and parses JSON")
    void testATSScorePredictor_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "atsScore": 84,
                  "resumeStrength": "Strong",
                  "keywordCoverage": 88,
                  "readability": 85,
                  "formatScore": 90,
                  "missingSections": ["Certifications"],
                  "recommendations": ["Include cloud certifications in education section."]
                }
                """);

        ATSScoreResponse res = atsScorePredictor.predict(new ATSScoreRequest("React TypeScript resume with complete details"));
        assertNotNull(res);
        assertEquals(84, res.getAtsScore());
        assertEquals("Strong", res.getResumeStrength());
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 4. Cover Letter Generator
    // ==========================================
    @Test
    @DisplayName("Tool 7: Cover Letter Generator calls Gemini askGemini")
    void testCoverLetterGenerator_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn("Dear Hiring Team, I am thrilled to apply for the Senior Engineer role at Acme Corp...");

        CoverLetterRequest req = new CoverLetterRequest();
        req.setCompanyName("Acme Corp");
        req.setJobRole("Senior Engineer");

        String res = coverLetterGenerator.generate(req);
        assertNotNull(res);
        assertTrue(res.contains("Acme Corp"));
        verify(geminiService, times(1)).askGemini(anyString());
    }

    // ==========================================
    // 5. Interview Question Generator
    // ==========================================
    @Test
    @DisplayName("Tool 8: Interview Question Generator calls Gemini and parses questions")
    void testInterviewQuestionGenerator_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn("How do you optimize Postgres queries in FastAPI?\nExplain Docker networking.");

        InterviewQuestionRequest req = new InterviewQuestionRequest();
        req.setJobRole("Python Engineer");
        req.setExperience("3 years");
        req.setSkills("FastAPI, Postgres, Docker");

        InterviewQuestionResponse res = interviewQuestionGenerator.generateQuestions(req);
        assertNotNull(res);
        assertEquals(2, res.getQuestions().size());
        assertTrue(res.getQuestions().get(0).contains("Postgres"));
        verify(geminiService, times(1)).askGemini(anyString());
    }

    // ==========================================
    // 6. Company Interview Experience
    // ==========================================
    @Test
    @DisplayName("Tool 9: Company Interview Experience calls Gemini and parses JSON")
    void testCompanyInterviewExperience_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "company": "Google",
                  "codingTopics": ["Dynamic Programming", "Graph Traversal", "System Design"],
                  "systemDesignTopics": ["Global Distributed Cache", "Rate Limiter"],
                  "behavioralQuestions": ["Tell me about a time you resolved technical conflict"],
                  "tips": ["Communicate trade-offs clearly"]
                }
                """);

        CompanyInterviewResponse res = companyInterviewExperienceGenerator.generate(new CompanyInterviewRequest("Google", "Software Engineer"));
        assertNotNull(res);
        assertEquals("Google", res.getCompany());
        assertTrue(res.getCodingTopics().contains("Dynamic Programming"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 7. Career Roadmap
    // ==========================================
    @Test
    @DisplayName("Tool 10: Career Roadmap calls Gemini and returns milestones")
    void testCareerRoadmap_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn("Month 1: Advanced PyTorch & Transformer Architecture\nMonth 2: LLM Fine-Tuning\nMonth 3: Production Deployment");

        CareerRoadmapResponse res = careerRoadmapGenerator.generate(new CareerRoadmapRequest("AI Research Engineer"));
        assertNotNull(res);
        assertFalse(res.getRoadmap().isEmpty());
        assertTrue(res.getRoadmap().get(0).contains("PyTorch"));
        verify(geminiService, times(1)).askGemini(anyString());
    }

    // ==========================================
    // 8. Skill Gap Analyzer
    // ==========================================
    @Test
    @DisplayName("Tool 11: Skill Gap Analyzer calls Gemini and parses structured plan")
    void testSkillGapAnalyzer_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "targetRole": "ML Engineer",
                  "existingSkills": ["Python", "SQL"],
                  "missingSkills": ["PyTorch", "MLflow", "Kubeflow"],
                  "learningPlan": ["Step 1: Deep Learning fundamentals", "Step 2: MLOps pipelines"],
                  "estimatedDuration": "6 Weeks"
                }
                """);

        SkillGapResponse res = skillGapAnalyzer.analyze(new SkillGapRequest("Python, SQL", "ML Engineer"));
        assertNotNull(res);
        assertEquals("ML Engineer", res.getTargetRole());
        assertTrue(res.getMissingSkills().contains("PyTorch"));
        assertEquals("6 Weeks", res.getEstimatedDuration());
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 9. Resume-linked Skill Gap Service
    // ==========================================
    @Test
    @DisplayName("Tool 12: Resume-linked Skill Gap Service calls Gemini with structured JSON")
    void testResumeLinkedSkillGap_GeminiActive() throws Exception {
        when(userRepository.findByEmail("developer@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByUser(testUser)).thenReturn(Optional.of(testResume));
        when(pdfTextExtractor.extractText(any(Path.class))).thenReturn("Python developer with 4 years building backend APIs and Docker microservices.");
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "targetRole": "Cloud Architect",
                  "existingSkills": ["Docker", "AWS"],
                  "missingSkills": ["Terraform", "Kubernetes", "IAM Best Practices"],
                  "learningPlan": ["Master Infrastructure as Code", "Kubernetes Cluster Architecture"],
                  "estimatedDuration": "8 Weeks"
                }
                """);

        SkillGapResponse res = skillGapService.analyzeSkillGap("Cloud Architect", "developer@example.com");
        assertNotNull(res);
        assertEquals("Cloud Architect", res.getTargetRole());
        assertTrue(res.getMissingSkills().contains("Terraform"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 10. Salary Predictor
    // ==========================================
    @Test
    @DisplayName("Tool 13: Salary Predictor calls Gemini and parses estimate")
    void testSalaryPredictor_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "estimatedSalary": "$120k - $150k (AI Estimate)",
                  "experienceLevel": "Senior",
                  "marketDemand": "Very High",
                  "suggestions": ["Highlight system design leadership in interviews"]
                }
                """);

        SalaryPredictionResponse res = salaryPredictor.predict(new SalaryPredictionRequest(5, List.of("Python", "AWS", "Docker"), "US / Remote"));
        assertNotNull(res);
        assertEquals("$120k - $150k (AI Estimate)", res.getEstimatedSalary());
        assertEquals("Senior", res.getExperienceLevel());
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 11. Job Application Success Predictor
    // ==========================================
    @Test
    @DisplayName("Tool 14: Job Application Success Predictor calls Gemini and parses JSON")
    void testJobApplicationSuccessPredictor_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "successProbability": 82,
                  "strengths": ["High ATS alignment", "Diverse cloud stack"],
                  "weaknesses": ["Lack of public open source repos"],
                  "suggestions": ["Showcase open source contributions"],
                  "missingSkills": ["Kubernetes"]
                }
                """);

        JobSuccessPredictionResponse res = jobApplicationSuccessPredictor.predict(
                new JobSuccessPredictionRequest(88, 5, 6, 2, List.of("Python", "Docker", "AWS"))
        );
        assertNotNull(res);
        assertEquals(82, res.getSuccessProbability());
        assertTrue(res.getStrengths().contains("High ATS alignment"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 12. Mock Interview Simulator
    // ==========================================
    @Test
    @DisplayName("Tool 15: Mock Interview Simulator calls Gemini and returns qualitative evaluation")
    void testMockInterviewSimulator_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "score": 88,
                  "feedback": ["Clear explanation of database indexing tradeoffs"],
                  "strengths": ["Strong architectural reasoning"],
                  "improvements": ["Mention read replica lag handling"],
                  "recommendation": "Strong hire recommendation for backend engineering."
                }
                """);

        MockInterviewResponse res = mockInterviewSimulator.evaluate(
                new MockInterviewRequest("Backend Engineer", List.of("We use B-Trees for primary index and Hash index for key-value lookups."))
        );
        assertNotNull(res);
        assertEquals(88, res.getScore());
        assertTrue(res.getStrengths().contains("Strong architectural reasoning"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 13. Job Market Trends
    // ==========================================
    @Test
    @DisplayName("Tool 16: Job Market Trends calls Gemini and returns dynamic market data")
    void testJobMarketTrends_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "role": "Rust Systems Developer",
                  "topSkills": ["Rust", "Tokio", "WebAssembly", "C++ Interop"],
                  "trendingTechnologies": ["Async Rust", "Wasmtime", "eBPF"],
                  "averageSalaryRange": "$140k - $190k (AI Estimate)",
                  "marketDemand": "High",
                  "hiringGrowth": "Excellent"
                }
                """);

        JobMarketTrendResponse res = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("Rust Systems Developer"));
        assertNotNull(res);
        assertEquals("Rust Systems Developer", res.getRole());
        assertTrue(res.getTopSkills().contains("Rust"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 14. Resume Keyword Optimizer
    // ==========================================
    @Test
    @DisplayName("Tool 17: Resume Keyword Optimizer calls Gemini and extracts strategic keywords")
    void testResumeKeywordOptimizer_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "targetRole": "React Specialist",
                  "presentKeywords": ["React", "JavaScript", "Redux"],
                  "missingKeywords": ["Next.js", "Zustand", "Tailwind CSS", "Jest"],
                  "recommendedKeywords": ["Add Next.js server component experience in projects"],
                  "optimizationScore": 72
                }
                """);

        ResumeKeywordResponse res = resumeKeywordOptimizer.optimize(
                new ResumeKeywordRequest("Proficient in React and JavaScript web applications.", "React Specialist")
        );
        assertNotNull(res);
        assertEquals(72, res.getOptimizationScore());
        assertTrue(res.getMissingKeywords().contains("Next.js"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 15. AI Career Chat
    // ==========================================
    @Test
    @DisplayName("Tool 18: AI Career Chat calls Gemini and returns conversational answer")
    void testAIChat_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn("To transition to DevOps, start by mastering Docker containerization and GitHub Actions CI/CD workflows.");

        String chatRes = aiChatController.chat("How do I transition to DevOps?");
        assertNotNull(chatRes);
        assertTrue(chatRes.contains("Docker containerization"));
        verify(geminiService, times(1)).askGemini(anyString());
    }

    // ==========================================
    // 16. Job Recommendation Engine
    // ==========================================
    @Test
    @DisplayName("Tool 19: Job Recommendation Engine calls Gemini and calculates match score")
    void testJobRecommendationEngine_GeminiActive() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "matchScore": 91,
                  "matchedSkills": ["Python", "FastAPI", "PostgreSQL"],
                  "missingSkills": ["GraphQL"],
                  "recommendation": "Outstanding candidate alignment for this position."
                }
                """);

        JobRecommendationResponse res = jobRecommendationEngine.recommend(
                new JobRecommendationRequest(List.of("Python", "FastAPI", "PostgreSQL"), List.of("Python", "FastAPI", "PostgreSQL", "GraphQL"))
        );
        assertNotNull(res);
        assertEquals(91, res.getMatchScore());
        assertTrue(res.getMatchedSkills().contains("Python"));
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 17. Database Job Recommendations Service
    // ==========================================
    @Test
    @DisplayName("Tool 20: Database Job Recommendations Service scores jobs with Gemini")
    void testDatabaseJobRecommendations_GeminiActive() throws Exception {
        when(userRepository.findByEmail("developer@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByUser(testUser)).thenReturn(Optional.of(testResume));
        when(pdfTextExtractor.extractText(any(Path.class))).thenReturn("Python developer with 4 years building backend APIs and Docker microservices.");

        JobApplication job1 = new JobApplication();
        job1.setId(101L);
        job1.setJobTitle("Senior Python Engineer");
        job1.setCompanyName("Stripe");
        job1.setJobDescription("We need Python, FastAPI, and Docker experience.");

        when(jobRepository.findAll()).thenReturn(List.of(job1));
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGeminiJson(anyString())).thenReturn("""
                {
                  "matchScore": 89,
                  "matchedSkills": ["Python", "FastAPI", "Docker"],
                  "missingSkills": [],
                  "recommendation": "Strong match for Stripe role."
                }
                """);

        JobRecommendationResponse recommendation = jobRecommendationService.recommendJobs("developer@example.com");
        assertNotNull(recommendation);
        assertEquals(89, recommendation.getMatchScore());
        verify(geminiService, times(1)).askGeminiJson(anyString());
    }

    // ==========================================
    // 18. Graceful Domain-Neutral Fallback Tests
    // ==========================================
    @Test
    @DisplayName("All tools fall back cleanly without errors when Gemini is unavailable")
    void testAllTools_FallbackWhenGeminiUnavailable() {
        when(geminiService.isAvailable()).thenReturn(false);

        // Resume Analyzer Fallback
        ResumeResponse atsRes = resumeAnalyzer.analyze("Python, Django, AWS, Postgres resume with projects");
        assertNotNull(atsRes);
        assertTrue(atsRes.getAtsScore() > 0);

        // Job Matcher Fallback
        JobMatchResponse matchRes = resumeJobMatcher.match(new JobMatchRequest("React TypeScript Frontend", "React TypeScript GraphQL"));
        assertNotNull(matchRes);
        assertTrue(matchRes.getAtsScore() > 0);

        // ATS Score Fallback
        ATSScoreResponse atsScoreRes = atsScorePredictor.predict(new ATSScoreRequest("Python developer with full background"));
        assertNotNull(atsScoreRes);
        assertTrue(atsScoreRes.getAtsScore() > 0);

        // Salary Predictor Fallback
        SalaryPredictionResponse salaryRes = salaryPredictor.predict(new SalaryPredictionRequest(4, List.of("React", "TypeScript"), "Remote"));
        assertNotNull(salaryRes);
        assertNotNull(salaryRes.getEstimatedSalary());

        // Mock Interview Fallback
        MockInterviewResponse mockRes = mockInterviewSimulator.evaluate(new MockInterviewRequest("Frontend Engineer", List.of("I structure React apps with clean architecture and hooks.")));
        assertNotNull(mockRes);
        assertTrue(mockRes.getScore() > 0);

        // Market Trends Fallback
        JobMarketTrendResponse trendRes = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("Data Scientist"));
        assertNotNull(trendRes);
        assertFalse(trendRes.getTopSkills().isEmpty());

        // Career Chat Fallback
        String chatRes = aiChatController.chat("How to prepare for tech interview?");
        assertNotNull(chatRes);
        assertFalse(chatRes.isBlank());
    }

    // ==========================================
    // 20. Specific Career Chat Query Verification (Part 4)
    // ==========================================
    @Test
    @DisplayName("Tool 18: Specific Career Chat Prompts test diverse queries")
    void testCareerChat_SpecificQueries_FallbackAndGemini() {
        when(geminiService.isAvailable()).thenReturn(false);

        // 1. "roadmap for backend developer"
        ChatResponse res1 = aiChatController.chatJson("roadmap for backend developer").getBody();
        assertNotNull(res1);
        assertTrue(res1.getResponse().contains("Backend Developer Career Roadmap"));
        assertEquals("FALLBACK", res1.getSource());

        // 2. "roadmap for fullstack developer"
        ChatResponse res2 = aiChatController.chatJson("roadmap for fullstack developer").getBody();
        assertNotNull(res2);
        assertTrue(res2.getResponse().contains("Full Stack Developer Career Roadmap"));
        assertEquals("FALLBACK", res2.getSource());

        // 2b. "road map for fullstack developer" (with space)
        ChatResponse res2b = aiChatController.chatJson("road map for fullstack developer").getBody();
        assertNotNull(res2b);
        assertTrue(res2b.getResponse().contains("Full Stack Developer Career Roadmap"));
        assertFalse(res2b.getResponse().contains("Focus on Quantifiable Impact"));

        // 3. "roadmap for python developer"
        ChatResponse res3 = aiChatController.chatJson("roadmap for python developer").getBody();
        assertNotNull(res3);
        assertTrue(res3.getResponse().contains("Python & Data Science"));
        assertEquals("FALLBACK", res3.getSource());

        // 4. "roadmap for react developer"
        ChatResponse res4 = aiChatController.chatJson("roadmap for react developer").getBody();
        assertNotNull(res4);
        assertTrue(res4.getResponse().contains("Frontend Developer Career Roadmap"));
        assertEquals("FALLBACK", res4.getSource());

        // 5. "how should I prepare for an AWS interview?"
        ChatResponse res5 = aiChatController.chatJson("how should I prepare for an AWS interview?").getBody();
        assertNotNull(res5);
        assertTrue(res5.getResponse().contains("AWS & Cloud Interview Preparation Guide"));
        assertEquals("FALLBACK", res5.getSource());

        // 6. "how can I improve my resume for a data scientist role?"
        ChatResponse res6 = aiChatController.chatJson("how can I improve my resume for a data scientist role?").getBody();
        assertNotNull(res6);
        assertTrue(res6.getResponse().contains("Data Scientist Resume Optimization Guide"));
        assertEquals("FALLBACK", res6.getSource());

        // 7. "career roadmap for customer support"
        ChatResponse res7 = aiChatController.chatJson("career roadmap for customer support").getBody();
        assertNotNull(res7);
        assertTrue(res7.getResponse().contains("Customer Support & Customer Success"));
        assertEquals("FALLBACK", res7.getSource());
    }

    // ==========================================
    // 21. Output Quality Domain Tests (Part 9)
    // ==========================================
    @Test
    @DisplayName("Part 9: Resume Analysis Output Quality across domains")
    void testOutputQuality_DomainNeutrality() {
        when(geminiService.isAvailable()).thenReturn(false);

        // TEST A: Python Resume - should NOT recommend Java/Spring Boot
        String pythonResume = """
                Senior Python Engineer with 5 years experience in machine learning and data pipelines.
                Skills: Python, Pandas, NumPy, TensorFlow, PyTorch, Scikit-learn, SQL, Docker.
                Projects: Built real-time recommendation engine processing 10k events/sec.
                Experience: Led ML engineering team developing fraud detection models.
                Education: B.S. Computer Science.
                """;
        ResumeResponse pythonResult = resumeAnalyzer.analyze(pythonResume);
        assertNotNull(pythonResult);
        assertTrue(pythonResult.getMatchedSkills().stream().noneMatch(s -> s.toLowerCase().contains("spring boot") || s.toLowerCase().contains("hibernate")));

        // TEST B: React Resume - should focus on frontend
        String reactResume = """
                Frontend Developer with 3 years building web applications.
                Skills: React, TypeScript, JavaScript, Next.js, Tailwind CSS, HTML5, CSS3, Redux.
                Projects: Developed responsive SaaS dashboard with real-time charts.
                Experience: Software Engineer at WebTech Inc.
                Education: B.Tech Information Technology.
                """;
        ResumeResponse reactResult = resumeAnalyzer.analyze(reactResume);
        assertNotNull(reactResult);
        assertTrue(reactResult.getMatchedSkills().stream().noneMatch(s -> s.toLowerCase().contains("spring boot")));

        // TEST C: Customer Support Resume
        String csResume = """
                Customer Support Specialist with 4 years resolving technical inquiries.
                Skills: Zendesk, Intercom, Customer Success, Ticket Resolution, JIRA, CRM.
                Experience: Handled 60+ tickets daily maintaining 98% CSAT rating.
                Education: B.A. Communications.
                """;
        ResumeResponse csResult = resumeAnalyzer.analyze(csResume);
        assertNotNull(csResult);
        assertTrue(csResult.getMatchedSkills().stream().noneMatch(s -> s.toLowerCase().contains("spring boot")));

        // TEST E: Keyword Stuffed Resume (without projects/experience details)
        String stuffedResume = "Java Python React AWS Docker Kubernetes MySQL Spring Boot Node Kafka Redis GraphQL TypeScript Terraform Linux";
        ResumeResponse stuffedResult = resumeAnalyzer.analyze(stuffedResume);
        assertNotNull(stuffedResult);
        assertTrue(stuffedResult.getAtsScore() <= 55, "Keyword-stuffed resume should not receive an artificially high score");
    }

    // ==========================================
    // 22. Cover Letter Source Verification
    // ==========================================
    @Test
    @DisplayName("Tool 7: Cover Letter Response accurately reflects GEMINI vs FALLBACK source")
    void testCoverLetterSourceTracking() {
        // When Gemini is active
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn("Dear Hiring Manager,\n\nI am thrilled to apply for the position at TechCorp.\n\nSincerely,\nApplicant");

        CoverLetterRequest req = new CoverLetterRequest();
        req.setCompanyName("TechCorp");
        req.setJobRole("Cloud Engineer");

        CoverLetterResponse geminiRes = coverLetterGenerator.generateResponse(req);
        assertEquals("GEMINI", geminiRes.getSource());
        assertTrue(geminiRes.getCoverLetter().contains("TechCorp"));

        // When Gemini is inactive
        when(geminiService.isAvailable()).thenReturn(false);
        CoverLetterResponse fallbackRes = coverLetterGenerator.generateResponse(req);
        assertEquals("FALLBACK", fallbackRes.getSource());
        assertTrue(fallbackRes.getCoverLetter().contains("TechCorp"));
    }
}
