package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.dto.ResumeResponse;
import com.vasudev.jobtracker.service.GeminiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeAnalyzerTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private ResumeAnalyzer resumeAnalyzer;

    @Test
    void testAnalyzeParsesStructuredJsonGeminiResponse() {
        when(geminiService.isAvailable()).thenReturn(true);
        String jsonResponse = """
                {
                  "atsScore": 88,
                  "matchedSkills": ["Python", "PyTorch", "Pandas", "Scikit-Learn"],
                  "missingSkills": ["MLOps", "Kubeflow"],
                  "suggestions": ["Add quantifiable model accuracy metrics", "Highlight production deployment"]
                }
                """;

        when(geminiService.askGeminiJson(anyString())).thenReturn(jsonResponse);

        ResumeResponse response = resumeAnalyzer.analyze("Resume text for Python Machine Learning Engineer");

        assertNotNull(response);
        assertEquals(88, response.getAtsScore());
        assertEquals(4, response.getMatchedSkills().size());
        assertTrue(response.getMatchedSkills().contains("Python"));
        assertEquals(2, response.getMissingSkills().size());
        assertEquals(2, response.getSuggestions().size());
    }

    @Test
    void testAnalyzeHandlesReactFrontendResume() {
        when(geminiService.isAvailable()).thenReturn(true);
        String jsonResponse = """
                {
                  "atsScore": 90,
                  "matchedSkills": ["React", "TypeScript", "Redux", "Tailwind CSS"],
                  "missingSkills": ["Next.js", "GraphQL"],
                  "suggestions": ["Include web vitals performance optimization achievements"]
                }
                """;

        when(geminiService.askGeminiJson(anyString())).thenReturn(jsonResponse);

        ResumeResponse response = resumeAnalyzer.analyze("Senior React TypeScript Frontend Engineer");

        assertNotNull(response);
        assertEquals(90, response.getAtsScore());
        assertTrue(response.getMatchedSkills().contains("React"));
    }

    @Test
    void testAnalyzeHandlesEmptyOrBlankAiResponseSafely() {
        when(geminiService.isAvailable()).thenReturn(false);

        String resumeText = """
                Jane Doe - Customer Success Manager
                Experience:
                Managed 45+ enterprise accounts with 98% retention rate.
                Education:
                B.S. in Communications.
                Skills:
                Zendesk, Salesforce, Client Retention, Escalation Management.
                """;

        ResumeResponse response = resumeAnalyzer.analyze(resumeText);

        assertNotNull(response);
        assertTrue(response.getAtsScore() >= 50);
        assertNotNull(response.getMatchedSkills());
        assertNotNull(response.getSuggestions());
    }

    @Test
    void testAnalyzePenalizesKeywordStuffingInFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        String keywordStuffed = "Java Python React Angular Node Docker AWS Kubernetes MySQL MongoDB Spring Boot Django Flask C++ C# Ruby Redis Kafka Terraform GraphQL Linux HTML CSS JavaScript TypeScript";

        ResumeResponse response = resumeAnalyzer.analyze(keywordStuffed);

        assertNotNull(response);
        assertTrue(response.getAtsScore() <= 55, "Keyword-stuffed resume should not receive high score");
    }
}
