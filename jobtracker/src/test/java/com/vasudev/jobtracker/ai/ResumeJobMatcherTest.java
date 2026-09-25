package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.dto.JobMatchRequest;
import com.vasudev.jobtracker.dto.JobMatchResponse;
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
class ResumeJobMatcherTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private ResumeJobMatcher resumeJobMatcher;

    @Test
    void testMatchParsesJsonGeminiResponse() {
        when(geminiService.isAvailable()).thenReturn(true);
        String json = """
                {
                  "atsScore": 85,
                  "matchedSkills": ["Python", "FastAPI", "Docker", "PostgreSQL"],
                  "missingSkills": ["Kubernetes", "Redis"],
                  "suggestions": ["Highlight distributed caching experience", "Add async IO metrics"]
                }
                """;

        when(geminiService.askGeminiJson(anyString())).thenReturn(json);

        JobMatchResponse response = resumeJobMatcher.match(new JobMatchRequest(
                "Python FastAPI Developer resume",
                "Senior Backend Engineer - Python FastAPI Redis"
        ));

        assertNotNull(response);
        assertEquals(85, response.getAtsScore());
        assertEquals(4, response.getMatchedSkills().size());
        assertTrue(response.getMatchedSkills().contains("Python"));
    }

    @Test
    void testMatchUnrelatedResumeReceivesLowScoreInFallback() {
        when(geminiService.isAvailable()).thenReturn(false);

        String nurseResume = "Registered Nurse with 6 years experience in patient triage, emergency care, EHR charting, and vital sign monitoring.";
        String devJob = "Senior Kubernetes Platform Engineer: Required skills Golang, Terraform, AWS, EKS, Prometheus, Helm, CI/CD.";

        JobMatchResponse response = resumeJobMatcher.match(new JobMatchRequest(nurseResume, devJob));

        assertNotNull(response);
        assertTrue(response.getAtsScore() <= 35, "Completely unrelated resume should receive a low match score");
    }

    @Test
    void testMatchMatchingPythonResumeAndJd() {
        when(geminiService.isAvailable()).thenReturn(false);

        String pythonResume = "Data Scientist with Python, Pandas, NumPy, Scikit-Learn, TensorFlow, SQL, Docker, and Tableau.";
        String pythonJd = "Machine Learning Engineer. Required: Python, Scikit-Learn, Pandas, TensorFlow, Docker, SQL.";

        JobMatchResponse response = resumeJobMatcher.match(new JobMatchRequest(pythonResume, pythonJd));

        assertNotNull(response);
        assertTrue(response.getAtsScore() >= 65, "Matching Python ML resume should score well");
        assertTrue(response.getMatchedSkills().contains("Python"));
    }
}
