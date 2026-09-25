package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.ai.*;
import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.entity.JobSeekerProfile;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.JobSeekerProfileRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.impl.AIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private CoverLetterGenerator coverLetterGenerator;

    @Mock
    private InterviewQuestionGenerator interviewQuestionGenerator;

    @Mock
    private JobRecommendationEngine jobRecommendationEngine;

    @Mock
    private ResumeJobMatcher resumeJobMatcher;

    @Mock
    private ResumeAnalyzer resumeAnalyzer;

    @Mock
    private CareerRoadmapGenerator careerRoadmapGenerator;

    @Mock
    private SalaryPredictor salaryPredictor;

    @Mock
    private JobApplicationSuccessPredictor jobApplicationSuccessPredictor;

    @Mock
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AIServiceImpl aiService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .role("USER")
                .active(true)
                .build();
    }

    @Test
    void testGenerateCoverLetter() {
        CoverLetterRequest request = new CoverLetterRequest();
        request.setCompanyName("Google");
        request.setJobRole("Backend Engineer");

        when(coverLetterGenerator.generateResponse(request))
                .thenReturn(new CoverLetterResponse("Dear Hiring Manager...", "GEMINI"));

        CoverLetterResponse response = aiService.generateCoverLetter(request);

        assertNotNull(response);
        assertEquals("Dear Hiring Manager...", response.getCoverLetter());
        assertEquals("GEMINI", response.getSource());
    }

    @Test
    void testGenerateInterviewQuestions() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobRole("Backend Engineer");
        request.setSkills("Java, Spring");
        request.setExperience("3");

        when(interviewQuestionGenerator.generateQuestions(request))
                .thenReturn(new InterviewQuestionResponse(List.of("What is Spring Boot?", "Explain Dependency Injection.")));

        InterviewQuestionResponse response = aiService.generateInterviewQuestions(request);

        assertNotNull(response);
        assertEquals(2, response.getQuestions().size());
    }

    @Test
    void testPredictSalary() {
        SalaryPredictionRequest request = new SalaryPredictionRequest(3, List.of("Java", "Spring Boot"), "Bangalore");
        when(salaryPredictor.predict(request))
                .thenReturn(new SalaryPredictionResponse("6 - 10 LPA", "Junior Developer", "High", List.of("Learn Docker")));

        SalaryPredictionResponse response = aiService.predictSalary(request);

        assertNotNull(response);
        assertEquals("6 - 10 LPA", response.getEstimatedSalary());
    }

    @Test
    void testPredictJobSuccessFromProfile() {
        JobSeekerProfile profile = JobSeekerProfile.builder()
                .id(5L)
                .atsScore(85)
                .yearsOfExperience(3)
                .projects(4)
                .certifications(2)
                .skills("Java, Spring Boot, MySQL")
                .user(user)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        when(jobApplicationSuccessPredictor.predict(any(JobSuccessPredictionRequest.class)))
                .thenReturn(new JobSuccessPredictionResponse(80, List.of("Strong Java background"), List.of(), List.of("Learn Kubernetes"), List.of()));

        JobSuccessPredictionResponse response = aiService.predictJobSuccessFromProfile("user@example.com");

        assertNotNull(response);
        assertEquals(80, response.getSuccessProbability());
    }

    @Test
    void testPredictJobSuccessFromProfileWhenNoProfileSaved() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jobSeekerProfileRepository.findByUser(user)).thenReturn(Optional.empty());

        when(jobApplicationSuccessPredictor.predict(any(JobSuccessPredictionRequest.class)))
                .thenReturn(new JobSuccessPredictionResponse(50, List.of(), List.of("Limited Experience"), List.of("Build projects"), List.of("Java")));

        JobSuccessPredictionResponse response = aiService.predictJobSuccessFromProfile("user@example.com");

        assertNotNull(response);
        assertEquals(50, response.getSuccessProbability());
    }
}
