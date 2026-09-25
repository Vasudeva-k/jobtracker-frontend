package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIEngineEdgeCasesTest {

    @Mock
    private GeminiService geminiService;

    private JobMarketTrendAnalyzer jobMarketTrendAnalyzer;
    private CompanyInterviewExperienceGenerator companyInterviewExperienceGenerator;
    private ATSScorePredictor atsScorePredictor;
    private MockInterviewSimulator mockInterviewSimulator;
    private SkillGapAnalyzer skillGapAnalyzer;
    private JobApplicationSuccessPredictor jobApplicationSuccessPredictor;
    private CareerRoadmapGenerator careerRoadmapGenerator;
    private InterviewQuestionGenerator interviewQuestionGenerator;

    @BeforeEach
    void setUp() {
        jobMarketTrendAnalyzer = new JobMarketTrendAnalyzer();
        companyInterviewExperienceGenerator = new CompanyInterviewExperienceGenerator();
        atsScorePredictor = new ATSScorePredictor();
        mockInterviewSimulator = new MockInterviewSimulator();
        skillGapAnalyzer = new SkillGapAnalyzer();
        jobApplicationSuccessPredictor = new JobApplicationSuccessPredictor();
        careerRoadmapGenerator = new CareerRoadmapGenerator(geminiService);
        interviewQuestionGenerator = new InterviewQuestionGenerator(geminiService);
    }

    // ==========================================
    // 1. JobMarketTrendAnalyzer Tests
    // ==========================================

    @Test
    void testJobMarketTrendAnalyzer_NullRequest() {
        JobMarketTrendResponse response = jobMarketTrendAnalyzer.analyze(null);
        assertNotNull(response);
        assertEquals("Software Engineer", response.getRole());
        assertNotNull(response.getTopSkills());
        assertNotNull(response.getTrendingTechnologies());
    }

    @Test
    void testJobMarketTrendAnalyzer_EmptyAndWhitespaceRole() {
        JobMarketTrendResponse responseEmpty = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest(""));
        assertNotNull(responseEmpty);
        assertEquals("Software Engineer", responseEmpty.getRole());

        JobMarketTrendResponse responseWhitespace = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("   "));
        assertNotNull(responseWhitespace);
        assertEquals("Software Engineer", responseWhitespace.getRole());
    }

    @Test
    void testJobMarketTrendAnalyzer_KnownAndUnknownRoles() {
        JobMarketTrendResponse javaResp = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("Senior Java Developer"));
        assertNotNull(javaResp);
        assertEquals("Java Developer", javaResp.getRole());

        JobMarketTrendResponse fullStackResp = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("MERN Stack Developer"));
        assertNotNull(fullStackResp);
        assertEquals("Full Stack Developer", fullStackResp.getRole());

        JobMarketTrendResponse dataResp = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("AI/ML Engineer"));
        assertNotNull(dataResp);
        assertEquals("Data Scientist", dataResp.getRole());

        JobMarketTrendResponse devopsResp = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("Cloud SRE Engineer"));
        assertNotNull(devopsResp);
        assertEquals("DevOps Engineer", devopsResp.getRole());

        JobMarketTrendResponse customResp = jobMarketTrendAnalyzer.analyze(new JobMarketTrendRequest("Cybersecurity Analyst"));
        assertNotNull(customResp);
        assertEquals("Cybersecurity Analyst", customResp.getRole());
    }

    // ==========================================
    // 2. CompanyInterviewExperienceGenerator Tests
    // ==========================================

    @Test
    void testCompanyInterviewExperienceGenerator_NullAndBlankCompany() {
        CompanyInterviewResponse nullResp = companyInterviewExperienceGenerator.generate(null);
        assertNotNull(nullResp);
        assertEquals("General", nullResp.getCompany());
        assertFalse(nullResp.getTechnicalQuestions().isEmpty());
        assertFalse(nullResp.getHrQuestions().isEmpty());

        CompanyInterviewResponse blankResp = companyInterviewExperienceGenerator.generate(new CompanyInterviewRequest("   ", "Software Engineer"));
        assertNotNull(blankResp);
        assertEquals("General", blankResp.getCompany());
    }

    @Test
    void testCompanyInterviewExperienceGenerator_KnownCompanies() {
        CompanyInterviewResponse amazon = companyInterviewExperienceGenerator.generate(new CompanyInterviewRequest("Amazon", "SDE"));
        assertNotNull(amazon);
        assertTrue(amazon.getHrQuestions().stream().anyMatch(q -> q.contains("Leadership Principles")));

        CompanyInterviewResponse google = companyInterviewExperienceGenerator.generate(new CompanyInterviewRequest("Google Inc", "Software Engineer"));
        assertNotNull(google);
        assertTrue(google.getTips().stream().anyMatch(t -> t.contains("LeetCode")));

        CompanyInterviewResponse microsoft = companyInterviewExperienceGenerator.generate(new CompanyInterviewRequest("Microsoft Corporation", "Backend Developer"));
        assertNotNull(microsoft);
        assertTrue(microsoft.getTechnicalQuestions().stream().anyMatch(q -> q.contains("OOP")));
    }

    // ==========================================
    // 3. ATSScorePredictor Tests
    // ==========================================

    @Test
    void testATSScorePredictor_NullAndEmptyResume() {
        ATSScoreResponse nullResp = atsScorePredictor.predict(null);
        assertNotNull(nullResp);
        assertTrue(nullResp.getAtsScore() >= 0 && nullResp.getAtsScore() <= 100);
        assertNotNull(nullResp.getMissingSections());
        assertNotNull(nullResp.getRecommendations());

        ATSScoreResponse emptyResp = atsScorePredictor.predict(new ATSScoreRequest("   "));
        assertNotNull(emptyResp);
        assertEquals("Needs Improvement", emptyResp.getResumeStrength());
    }

    @Test
    void testATSScorePredictor_RichResume() {
        String fullResume = "Senior Engineer with extensive experience in microservices architecture, cloud systems, containerization, and relational databases. Delivered 15+ major production projects with 99.9% uptime. Certifications and achievements include AWS Certified Solutions Architect.";
        ATSScoreResponse response = atsScorePredictor.predict(new ATSScoreRequest(fullResume));
        assertNotNull(response);
        assertTrue(response.getKeywordCoverage() >= 75);
        assertTrue(response.getAtsScore() >= 70);
    }

    // ==========================================
    // 4. MockInterviewSimulator Tests
    // ==========================================

    @Test
    void testMockInterviewSimulator_NullAndEmptyAnswers() {
        MockInterviewResponse nullResp = mockInterviewSimulator.evaluate(null);
        assertNotNull(nullResp);
        assertEquals(0, nullResp.getScore());
        assertFalse(nullResp.getImprovements().isEmpty());

        MockInterviewRequest emptyReq = new MockInterviewRequest("Java Developer", Collections.emptyList());
        MockInterviewResponse emptyResp = mockInterviewSimulator.evaluate(emptyReq);
        assertNotNull(emptyResp);
        assertEquals(0, emptyResp.getScore());
    }

    @Test
    void testMockInterviewSimulator_ListContainingNullsAndBlanks() {
        List<String> answers = new ArrayList<>();
        answers.add(null);
        answers.add("   ");
        answers.add("I use Python and FastAPI for building high-throughput distributed microservices with PostgreSQL database and Docker containerization on AWS cloud");

        MockInterviewResponse response = mockInterviewSimulator.evaluate(new MockInterviewRequest("Python Engineer", answers));
        assertNotNull(response);
        assertTrue(response.getScore() > 0);
        assertNotNull(response.getStrengths());
        assertFalse(response.getFeedback().isEmpty());
    }

    // ==========================================
    // 5. SkillGapAnalyzer Tests
    // ==========================================

    @Test
    void testSkillGapAnalyzer_NullAndEmptyInputs() {
        SkillGapResponse nullResp = skillGapAnalyzer.analyze(null);
        assertNotNull(nullResp);
        assertEquals("Software Developer", nullResp.getTargetRole());
        assertTrue(nullResp.getExistingSkills().isEmpty());
        assertFalse(nullResp.getMissingSkills().isEmpty());

        SkillGapResponse emptyReq = skillGapAnalyzer.analyze(new SkillGapRequest("   ", "   "));
        assertNotNull(emptyReq);
        assertEquals("Software Developer", emptyReq.getTargetRole());
    }

    @Test
    void testSkillGapAnalyzer_ValidMatching() {
        SkillGapRequest req = new SkillGapRequest("Java, Spring Boot, Git, Docker", "Java Backend Engineer");
        SkillGapResponse resp = skillGapAnalyzer.analyze(req);
        assertNotNull(resp);
        assertEquals("Java Backend Engineer", resp.getTargetRole());
        assertTrue(resp.getExistingSkills().contains("Java"));
        assertTrue(resp.getExistingSkills().contains("Spring Boot"));
        assertNotNull(resp.getMissingSkills());
        assertNotNull(resp.getEstimatedDuration());
    }

    // ==========================================
    // 6. JobApplicationSuccessPredictor Tests
    // ==========================================

    @Test
    void testJobApplicationSuccessPredictor_NullAndNegativeValues() {
        JobSuccessPredictionResponse nullResp = jobApplicationSuccessPredictor.predict(null);
        assertNotNull(nullResp);
        assertTrue(nullResp.getSuccessProbability() >= 0 && nullResp.getSuccessProbability() <= 100);

        JobSuccessPredictionRequest negativeReq = new JobSuccessPredictionRequest(-10, -5, -2, -1, null);
        JobSuccessPredictionResponse negResp = jobApplicationSuccessPredictor.predict(negativeReq);
        assertNotNull(negResp);
        assertTrue(negResp.getSuccessProbability() >= 0);
    }

    @Test
    void testJobApplicationSuccessPredictor_HighStatsAndSkills() {
        List<String> skills = Arrays.asList("Java", "Spring Boot", "Hibernate", "Spring Security", "JWT", "Microservices", "Docker", "AWS", null, "  ");
        JobSuccessPredictionRequest highReq = new JobSuccessPredictionRequest(95, 6, 8, 4, skills);
        JobSuccessPredictionResponse response = jobApplicationSuccessPredictor.predict(highReq);

        assertNotNull(response);
        assertTrue(response.getSuccessProbability() >= 80);
        assertTrue(response.getStrengths().contains("Strong Professional Experience"));
        assertTrue(response.getStrengths().contains("Strong Project Portfolio"));
        assertTrue(response.getStrengths().contains("Good Certifications"));
    }

    // ==========================================
    // 7. CareerRoadmapGenerator Tests
    // ==========================================

    @Test
    void testCareerRoadmapGenerator_NullAndEmptyGeminiResponse() {
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn(null);

        CareerRoadmapResponse response = careerRoadmapGenerator.generate(new CareerRoadmapRequest("DevOps Engineer"));
        assertNotNull(response);
        assertEquals("DevOps Engineer", response.getTargetRole());
        assertFalse(response.getRoadmap().isEmpty());
    }

    @Test
    void testCareerRoadmapGenerator_MarkdownFencedGeminiResponse() {
        String markdownOutput = """
                ```markdown
                Target Role: Full Stack Developer
                
                Roadmap:
                - Step 1: Master HTML, CSS, JavaScript, and TypeScript
                - Step 2: Learn React and state management
                - Step 3: Build backend APIs with Java and Spring Boot
                - Step 4: Work with relational and NoSQL databases
                - Step 5: Containerize and deploy to cloud
                ```
                """;
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn(markdownOutput);

        CareerRoadmapResponse response = careerRoadmapGenerator.generate(new CareerRoadmapRequest("Full Stack Developer"));
        assertNotNull(response);
        assertEquals("Full Stack Developer", response.getTargetRole());
        assertEquals(5, response.getRoadmap().size());
        assertTrue(response.getRoadmap().get(0).contains("HTML"));
    }

    // ==========================================
    // 8. InterviewQuestionGenerator Tests
    // ==========================================

    @Test
    void testInterviewQuestionGenerator_NullAndEmptyGeminiResponse() {
        when(geminiService.isAvailable()).thenReturn(false);

        InterviewQuestionRequest req = new InterviewQuestionRequest();
        req.setJobRole("Cloud Architect");
        req.setExperience("5");
        req.setSkills("AWS, Kubernetes");

        InterviewQuestionResponse response = interviewQuestionGenerator.generateQuestions(req);
        assertNotNull(response);
        assertTrue(response.getQuestions().size() >= 5);
    }

    @Test
    void testInterviewQuestionGenerator_MarkdownFencedAndNumberedResponse() {
        String aiOutput = """
                ```
                1. What is the difference between monolithic and microservices architecture?
                2) How does Spring Boot manage auto-configuration under the hood?
                - 3. Explain how optimistic locking works in JPA/Hibernate.
                • 4. Describe how you would implement distributed tracing with OpenTelemetry.
                5. Tell me about a time you resolved a high-severity production incident.
                ```
                """;
        when(geminiService.isAvailable()).thenReturn(true);
        when(geminiService.askGemini(anyString())).thenReturn(aiOutput);

        InterviewQuestionRequest req = new InterviewQuestionRequest();
        req.setJobRole("Backend Lead");
        req.setExperience("7");
        req.setSkills("Java, Spring Boot, Microservices");

        InterviewQuestionResponse response = interviewQuestionGenerator.generateQuestions(req);
        assertNotNull(response);
        assertEquals(5, response.getQuestions().size());
        assertFalse(response.getQuestions().get(0).startsWith("1."));
        assertTrue(response.getQuestions().get(0).contains("monolithic and microservices"));
    }
}
