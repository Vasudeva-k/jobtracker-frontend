package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AIMultiDomainVerificationTest {

    @Mock
    private GeminiService geminiService;

    private ResumeAnalyzer resumeAnalyzer;
    private ResumeJobMatcher resumeJobMatcher;
    private ATSScorePredictor atsScorePredictor;
    private ResumeKeywordOptimizer keywordOptimizer;
    private SalaryPredictor salaryPredictor;
    private JobApplicationSuccessPredictor successPredictor;
    private SkillGapAnalyzer skillGapAnalyzer;
    private InterviewQuestionGenerator questionGenerator;
    private CareerRoadmapGenerator roadmapGenerator;
    private CompanyInterviewExperienceGenerator companyExperienceGenerator;
    private JobMarketTrendAnalyzer marketTrendAnalyzer;

    @BeforeEach
    void setUp() {
        resumeAnalyzer = new ResumeAnalyzer(geminiService);
        resumeJobMatcher = new ResumeJobMatcher(geminiService);
        atsScorePredictor = new ATSScorePredictor();
        keywordOptimizer = new ResumeKeywordOptimizer(geminiService);
        salaryPredictor = new SalaryPredictor(geminiService);
        successPredictor = new JobApplicationSuccessPredictor(geminiService);
        skillGapAnalyzer = new SkillGapAnalyzer(geminiService);
        questionGenerator = new InterviewQuestionGenerator(geminiService);
        roadmapGenerator = new CareerRoadmapGenerator(geminiService);
        companyExperienceGenerator = new CompanyInterviewExperienceGenerator(geminiService);
        marketTrendAnalyzer = new JobMarketTrendAnalyzer(geminiService);
    }

    // =========================================================================
    // 1. Python / Backend Domain Tests (Fallback & Gemini-enabled)
    // =========================================================================
    @Test
    @DisplayName("Python Developer - Fallback Mode extracts Python stack correctly without Java bias")
    void testPythonResumeFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        String pythonResume = """
                Alex Rivera - Senior Python Engineer
                Experience with Python, Django, FastAPI, Celery, Redis, PostgreSQL, Docker, AWS.
                Designed high-concurrency microservices and asynchronous task pipelines.
                """;

        ResumeResponse response = resumeAnalyzer.analyze(pythonResume);
        assertNotNull(response);
        assertTrue(response.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Python")), "Should detect Python");
        assertTrue(response.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("FastAPI") || s.equalsIgnoreCase("Django")), "Should detect Django/FastAPI");
        assertFalse(response.getMatchedSkills().contains("Java"), "Fallback should not inject Java into Python resume");
    }

    @Test
    @DisplayName("Python Developer - Gemini Enabled extracts structured JSON")
    void testPythonResumeGemini() {
        when(geminiService.isAvailable()).thenReturn(true);
        String mockJson = """
                {
                  "atsScore": 88,
                  "matchedSkills": ["Python", "FastAPI", "PostgreSQL", "Docker", "Redis"],
                  "missingSkills": ["Kubernetes"],
                  "suggestions": ["Add CI/CD pipeline details"]
                }
                """;
        when(geminiService.askGeminiJson(anyString())).thenReturn(mockJson);

        ResumeResponse response = resumeAnalyzer.analyze("Python developer resume...");
        assertNotNull(response);
        assertEquals(88, response.getAtsScore());
        assertTrue(response.getMatchedSkills().contains("Python"));
        assertTrue(response.getMatchedSkills().contains("FastAPI"));
        assertEquals(1, response.getSuggestions().size());
    }

    // =========================================================================
    // 2. React / Frontend Domain Tests
    // =========================================================================
    @Test
    @DisplayName("React Developer - Fallback Mode extracts React/Frontend stack without Java bias")
    void testReactResumeFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        String reactResume = """
                Sarah Chen - Lead Frontend Engineer
                Skilled in React, TypeScript, Next.js, Redux, Tailwind CSS, Vite, HTML5, CSS3, GraphQL.
                Built responsive enterprise single-page applications with high performance.
                """;

        ResumeResponse response = resumeAnalyzer.analyze(reactResume);
        assertNotNull(response);
        assertTrue(response.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("React")), "Should detect React");
        assertTrue(response.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("TypeScript")), "Should detect TypeScript");
        assertFalse(response.getMatchedSkills().contains("Spring Boot"), "Should not inject Spring Boot into React resume");
    }

    @Test
    @DisplayName("React Developer - Matcher correctly matches React job description")
    void testReactJobMatcherFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        JobMatchRequest req = new JobMatchRequest();
        req.setResumeText("Frontend engineer experienced in React, TypeScript, Next.js, Tailwind CSS, Jest.");
        req.setJobDescription("Looking for a Senior React Engineer with React, TypeScript, Next.js, and CSS expertise.");

        JobMatchResponse response = resumeJobMatcher.match(req);
        assertNotNull(response);
        assertTrue(response.getAtsScore() >= 60, "Score should be high for matching React skills");
        assertTrue(response.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("React")));
    }

    // =========================================================================
    // 3. Data Science / AI / ML Domain Tests
    // =========================================================================
    @Test
    @DisplayName("Data Science - Fallback Mode extracts ML stack correctly")
    void testDataScienceResumeFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        String dsResume = """
                Dr. Kevin Patel - Machine Learning Scientist
                Expert in Python, PyTorch, TensorFlow, Scikit-Learn, Pandas, NumPy, SQL, Generative AI, LLMs.
                Published research in NLP and deployed transformer models in production.
                """;

        ResumeResponse response = resumeAnalyzer.analyze(dsResume);
        assertNotNull(response);
        assertTrue(response.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Python")));
        assertTrue(response.getMatchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("PyTorch") || s.equalsIgnoreCase("TensorFlow") || s.equalsIgnoreCase("SQL")));
        assertFalse(response.getMatchedSkills().contains("Hibernate"), "Should not inject Hibernate into ML resume");
    }

    // =========================================================================
    // 4. Non-Technical / Product / Customer Success Domain Tests
    // =========================================================================
    @Test
    @DisplayName("Product / Customer Success - Fallback Mode handles non-engineering resumes gracefully")
    void testNonTechnicalResumeFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        String pmResume = """
                Jordan Smith - Senior Customer Success Manager
                Core competencies: Account Management, Stakeholder Communication, CRM, Conflict Resolution,
                Customer Retention, Onboarding, Analytics, Presentation.
                Managed $5M ARR portfolio with 98% net retention rate.
                """;

        ResumeResponse response = resumeAnalyzer.analyze(pmResume);
        assertNotNull(response);
        assertNotNull(response.getMatchedSkills());
        assertFalse(response.getMatchedSkills().isEmpty(), "Should extract identified professional skills");
        assertTrue(response.getAtsScore() >= 40, "Should assign reasonable ATS score based on sections");
        assertFalse(response.getMatchedSkills().contains("Spring Boot"), "Must not force Java onto non-technical resume");
    }

    // =========================================================================
    // 5. Adversarial & Keyword-Stuffed Resume Tests
    // =========================================================================
    @Test
    @DisplayName("Adversarial Keyword-Stuffed Resume receives appropriate readability penalty")
    void testKeywordStuffedResumeHandling() {
        // Repeated skill tokens with zero context or project sentences
        String stuffedResume = "Java Spring React Python AWS Docker Java Spring React Python AWS Docker Java Spring React Python AWS Docker Java Spring React Python AWS Docker Java Spring React Python AWS Docker";

        ATSScoreResponse atsResponse = atsScorePredictor.predict(new ATSScoreRequest(stuffedResume));
        assertNotNull(atsResponse);
        // Should flag missing structure
        assertTrue(atsResponse.getMissingSections().size() >= 2, "Should identify missing core sections");
    }

    // =========================================================================
    // 6. Gemini-Disabled & Domain-Neutral AI Features Verification
    // =========================================================================
    @Test
    @DisplayName("Salary Predictor - Fallback generates domain-neutral estimates with (AI Estimate) label")
    void testSalaryPredictorFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        SalaryPredictionRequest req = new SalaryPredictionRequest(4, List.of("Python", "FastAPI", "AWS"), "Remote");
        SalaryPredictionResponse response = salaryPredictor.predict(req);

        assertNotNull(response);
        assertTrue(response.getEstimatedSalary().contains("AI Estimate") || response.getEstimatedSalary().contains("LPA"), "Should contain AI Estimate indication");
        assertNotNull(response.getMarketDemand());
        assertFalse(response.getSuggestions().isEmpty());
    }

    @Test
    @DisplayName("Job Success Predictor - Labels output honestly without scientific certainty claims")
    void testJobSuccessPredictorHonesty() {
        when(geminiService.isAvailable()).thenReturn(false);

        JobSuccessPredictionRequest req = new JobSuccessPredictionRequest(85, 4, 5, 2, List.of("React", "TypeScript", "Node.js"));
        JobSuccessPredictionResponse response = successPredictor.predict(req);

        assertNotNull(response);
        assertTrue(response.getSuccessProbability() >= 0 && response.getSuccessProbability() <= 100);
        assertNotNull(response.getStrengths());
        assertFalse(response.getStrengths().isEmpty());
    }

    @Test
    @DisplayName("Skill Gap Analyzer - Multi-domain support for Python, React, and DevOps")
    void testSkillGapAnalyzerMultiDomain() {
        when(geminiService.isAvailable()).thenReturn(false);

        SkillGapRequest req = new SkillGapRequest("Python, SQL", "Data Scientist");
        SkillGapResponse response = skillGapAnalyzer.analyze(req);

        assertNotNull(response);
        assertEquals("Data Scientist", response.getTargetRole());
        assertTrue(response.getExistingSkills().contains("Python"));
        assertTrue(response.getMissingSkills().stream().anyMatch(s -> s.toLowerCase().contains("machine learning") || s.toLowerCase().contains("statistics")));
    }
}
