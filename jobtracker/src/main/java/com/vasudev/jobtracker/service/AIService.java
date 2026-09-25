package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.*;
import com.vasudev.jobtracker.dto.SkillGapRequest;
import com.vasudev.jobtracker.dto.SkillGapResponse;
import com.vasudev.jobtracker.dto.JobSuccessPredictionRequest;
import com.vasudev.jobtracker.dto.JobSuccessPredictionResponse;

public interface AIService {

    // AI Cover Letter Generator
    CoverLetterResponse generateCoverLetter(
            CoverLetterRequest request);

    // AI Interview Question Generator
    InterviewQuestionResponse generateInterviewQuestions(
            InterviewQuestionRequest request);

    // AI Job Recommendation
    JobRecommendationResponse recommendJobs(
            JobRecommendationRequest request);

    // AI Resume vs Job Description Matcher
    JobMatchResponse matchResume(
            JobMatchRequest request);

    // AI Resume Improvement Suggestions
    ResumeResponse analyzeResume(
            String resumeText);

    // AI Career Roadmap Generator
    CareerRoadmapResponse generateCareerRoadmap(
            CareerRoadmapRequest request);

    // AI Salary Predictor
    SalaryPredictionResponse predictSalary(
            SalaryPredictionRequest request);

    // AI Mock Interview Simulator
    MockInterviewResponse evaluateInterview(
            MockInterviewRequest request);

    // AI Job Market Trends Analyzer
    JobMarketTrendResponse analyzeJobMarket(
            JobMarketTrendRequest request);

    // AI Resume Keyword Optimizer
    ResumeKeywordResponse optimizeResumeKeywords(
            ResumeKeywordRequest request);

    // AI Resume ATS Score Predictor
    ATSScoreResponse predictATSScore(
            ATSScoreRequest request);

    // AI Company Interview Experience Generator
    CompanyInterviewResponse generateInterviewExperience(
            CompanyInterviewRequest request);

    // AI Skill Gap Analyzer
    SkillGapResponse analyzeSkillGap(
            SkillGapRequest request);

    // AI Job Application Success Predictor
    JobSuccessPredictionResponse predictJobSuccess(
            JobSuccessPredictionRequest request);

    // AI Job Application Success Predictor - From User Profile
    JobSuccessPredictionResponse predictJobSuccessFromProfile(
            String email);

}