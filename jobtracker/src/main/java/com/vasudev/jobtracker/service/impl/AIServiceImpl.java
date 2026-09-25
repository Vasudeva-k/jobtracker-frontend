package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.ai.ATSScorePredictor;
import com.vasudev.jobtracker.ai.CareerRoadmapGenerator;
import com.vasudev.jobtracker.ai.CompanyInterviewExperienceGenerator;
import com.vasudev.jobtracker.ai.CoverLetterGenerator;
import com.vasudev.jobtracker.ai.InterviewQuestionGenerator;
import com.vasudev.jobtracker.ai.JobApplicationSuccessPredictor;
import com.vasudev.jobtracker.ai.JobMarketTrendAnalyzer;
import com.vasudev.jobtracker.ai.JobRecommendationEngine;
import com.vasudev.jobtracker.ai.MockInterviewSimulator;
import com.vasudev.jobtracker.ai.ResumeAnalyzer;
import com.vasudev.jobtracker.ai.ResumeJobMatcher;
import com.vasudev.jobtracker.ai.ResumeKeywordOptimizer;
import com.vasudev.jobtracker.ai.SalaryPredictor;
import com.vasudev.jobtracker.ai.SkillGapAnalyzer;

import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.entity.JobSeekerProfile;
import com.vasudev.jobtracker.repository.JobSeekerProfileRepository;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.service.AIService;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class AIServiceImpl implements AIService {

    private final CoverLetterGenerator coverLetterGenerator;
    private final InterviewQuestionGenerator interviewQuestionGenerator;
    private final JobRecommendationEngine jobRecommendationEngine;
    private final ResumeJobMatcher resumeJobMatcher;
    private final ResumeAnalyzer resumeAnalyzer;
    private final CareerRoadmapGenerator careerRoadmapGenerator;
    private final SalaryPredictor salaryPredictor;
    private final MockInterviewSimulator mockInterviewSimulator;
    private final JobMarketTrendAnalyzer jobMarketTrendAnalyzer;
    private final ResumeKeywordOptimizer resumeKeywordOptimizer;
    private final ATSScorePredictor atsScorePredictor;
    private final CompanyInterviewExperienceGenerator companyInterviewExperienceGenerator;
    private final SkillGapAnalyzer skillGapAnalyzer;
    private final JobApplicationSuccessPredictor jobApplicationSuccessPredictor;

    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final UserRepository userRepository;

    public AIServiceImpl(
            CoverLetterGenerator coverLetterGenerator,
            InterviewQuestionGenerator interviewQuestionGenerator,
            JobRecommendationEngine jobRecommendationEngine,
            ResumeJobMatcher resumeJobMatcher,
            ResumeAnalyzer resumeAnalyzer,
            CareerRoadmapGenerator careerRoadmapGenerator,
            SalaryPredictor salaryPredictor,
            MockInterviewSimulator mockInterviewSimulator,
            JobMarketTrendAnalyzer jobMarketTrendAnalyzer,
            ResumeKeywordOptimizer resumeKeywordOptimizer,
            ATSScorePredictor atsScorePredictor,
            CompanyInterviewExperienceGenerator companyInterviewExperienceGenerator,
            SkillGapAnalyzer skillGapAnalyzer,
            JobApplicationSuccessPredictor jobApplicationSuccessPredictor,
            JobSeekerProfileRepository jobSeekerProfileRepository,
            UserRepository userRepository) {

        this.coverLetterGenerator = coverLetterGenerator;
        this.interviewQuestionGenerator = interviewQuestionGenerator;
        this.jobRecommendationEngine = jobRecommendationEngine;
        this.resumeJobMatcher = resumeJobMatcher;
        this.resumeAnalyzer = resumeAnalyzer;
        this.careerRoadmapGenerator = careerRoadmapGenerator;
        this.salaryPredictor = salaryPredictor;
        this.mockInterviewSimulator = mockInterviewSimulator;
        this.jobMarketTrendAnalyzer = jobMarketTrendAnalyzer;
        this.resumeKeywordOptimizer = resumeKeywordOptimizer;
        this.atsScorePredictor = atsScorePredictor;
        this.companyInterviewExperienceGenerator =
                companyInterviewExperienceGenerator;
        this.skillGapAnalyzer = skillGapAnalyzer;
        this.jobApplicationSuccessPredictor =
                jobApplicationSuccessPredictor;

        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CoverLetterResponse generateCoverLetter(
            CoverLetterRequest request) {

        return coverLetterGenerator.generateResponse(request);
    }

    @Override
    public InterviewQuestionResponse generateInterviewQuestions(
            InterviewQuestionRequest request) {

        return interviewQuestionGenerator.generateQuestions(request);
    }

    @Override
    public JobRecommendationResponse recommendJobs(
            JobRecommendationRequest request) {

        return jobRecommendationEngine.recommend(request);
    }

    @Override
    public JobMatchResponse matchResume(JobMatchRequest request) {

        return resumeJobMatcher.match(request);
    }

    @Override
    public ResumeResponse analyzeResume(String resumeText) {

        return resumeAnalyzer.analyze(resumeText);
    }

    @Override
    public CareerRoadmapResponse generateCareerRoadmap(
            CareerRoadmapRequest request) {

        return careerRoadmapGenerator.generate(request);
    }

    @Override
    public SalaryPredictionResponse predictSalary(
            SalaryPredictionRequest request) {

        return salaryPredictor.predict(request);
    }

    @Override
    public MockInterviewResponse evaluateInterview(
            MockInterviewRequest request) {

        return mockInterviewSimulator.evaluate(request);
    }

    @Override
    public JobMarketTrendResponse analyzeJobMarket(
            JobMarketTrendRequest request) {

        return jobMarketTrendAnalyzer.analyze(request);
    }

    @Override
    public ResumeKeywordResponse optimizeResumeKeywords(
            ResumeKeywordRequest request) {

        return resumeKeywordOptimizer.optimize(request);
    }

    @Override
    public ATSScoreResponse predictATSScore(
            ATSScoreRequest request) {

        return atsScorePredictor.predict(request);
    }

    @Override
    public CompanyInterviewResponse generateInterviewExperience(
            CompanyInterviewRequest request) {

        return companyInterviewExperienceGenerator.generate(request);
    }

    @Override
    public SkillGapResponse analyzeSkillGap(
            SkillGapRequest request) {

        return skillGapAnalyzer.analyze(request);
    }

    // ==========================================
    // AI JOB APPLICATION SUCCESS PREDICTOR
    // ==========================================

    @Override
    public JobSuccessPredictionResponse predictJobSuccess(
            JobSuccessPredictionRequest request) {

        return jobApplicationSuccessPredictor.predict(request);
    }

    // ==========================================
    // AI JOB SUCCESS PREDICTOR FROM USER PROFILE
    // ==========================================

    @Override
    public JobSuccessPredictionResponse predictJobSuccessFromProfile(
            String email) {

        // Find logged-in user
        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Find user's job seeker profile (or default to empty if new user)
        JobSeekerProfile profile =
                jobSeekerProfileRepository.findByUser(user)
                        .orElse(JobSeekerProfile.builder()
                                .user(user)
                                .atsScore(0)
                                .yearsOfExperience(0)
                                .projects(0)
                                .certifications(0)
                                .skills("")
                                .build());

        // Convert stored skills string into List<String>
        List<String> skills;

        if (profile.getSkills() == null ||
                profile.getSkills().trim().isEmpty()) {

            skills = Collections.emptyList();

        } else {

            skills = Arrays.stream(profile.getSkills().split(","))
                    .map(String::trim)
                    .filter(skill -> !skill.isEmpty())
                    .toList();
        }

        // Create prediction request from saved profile
        JobSuccessPredictionRequest request =
                new JobSuccessPredictionRequest(
                        profile.getAtsScore() == null
                                ? 0
                                : profile.getAtsScore(),

                        profile.getYearsOfExperience() == null
                                ? 0
                                : profile.getYearsOfExperience(),

                        profile.getProjects() == null
                                ? 0
                                : profile.getProjects(),

                        profile.getCertifications() == null
                                ? 0
                                : profile.getCertifications(),

                        skills
                );

        // Reuse existing prediction logic
        return jobApplicationSuccessPredictor.predict(request);
    }
}