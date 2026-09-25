package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.dto.InterviewQuestionRequest;
import com.vasudev.jobtracker.dto.InterviewQuestionResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterviewQuestionGenerator {

    private static final Logger log = LoggerFactory.getLogger(InterviewQuestionGenerator.class);
    private final GeminiService geminiService;

    public InterviewQuestionGenerator(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public InterviewQuestionResponse generateQuestions(InterviewQuestionRequest request) {

        String rawRole = (request != null && request.getJobRole() != null && !request.getJobRole().isBlank())
                ? request.getJobRole().trim()
                : "Professional";
        String rawSkills = (request != null && request.getSkills() != null && !request.getSkills().isBlank())
                ? request.getSkills().trim()
                : "";

        String sanitizedRole = rawRole.replaceAll("[\\r\\n]", " ").replaceAll("[<>{}]", "").trim();
        String sanitizedSkills = rawSkills.replaceAll("[\\r\\n]", " ").replaceAll("[<>{}]", "").trim();

        log.info("[AI] Feature: Interview Question Generator - Generating questions for Role: {}, Skills: {}", sanitizedRole, sanitizedSkills);

        String prompt = """
                You are an experienced hiring manager and technical interviewer.

                Generate 10 insightful interview questions tailored specifically to the target role and candidate skills.

                Rules:
                - Return ONLY the questions, one question per line.
                - Do not number the questions or use bullet points.
                - Mix technical/domain questions (60%%) and behavioral/situational questions (40%%).
                - Tailor questions specifically to the domain of "%s". Do NOT inject unrelated programming languages or frameworks.

                Target Role: %s
                Candidate/Required Skills: %s
                """.formatted(
                sanitizedRole,
                sanitizedRole,
                sanitizedSkills.isBlank() ? "Standard industry skills for " + sanitizedRole : sanitizedSkills
        );

        if (geminiService != null && geminiService.isAvailable()) {
            try {
                log.info("[AI] Calling Gemini for Interview Questions...");
                String aiResponse = geminiService.askGemini(prompt);
                if (aiResponse != null && !aiResponse.isBlank()) {
                    String cleanedResponse = GeminiService.stripMarkdownFences(aiResponse);
                    List<String> questions = new ArrayList<>();

                    for (String line : cleanedResponse.split("\\R")) {
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        // Remove numbering like 1. or 1) or Q1:
                        line = line.replaceFirst("^(?i)(Q\\d+:|\\d+[.)])\\s*", "");

                        // Remove bullets like -, *, •
                        if (line.startsWith("-") || line.startsWith("*") || line.startsWith("•")) {
                            line = line.substring(1).trim();
                        }

                        if (!line.isBlank()) {
                            questions.add(line);
                        }
                    }

                    if (!questions.isEmpty()) {
                        log.info("[AI] Gemini questions extracted successfully (count: {})", questions.size());
                        InterviewQuestionResponse response = new InterviewQuestionResponse(sanitizedRole, questions);
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Interview Questions: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Interview Questions: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Interview Questions. Using domain-neutral fallback.");
        }

        return getFallbackQuestions(sanitizedRole, sanitizedSkills);
    }

    private InterviewQuestionResponse getFallbackQuestions(String role, String skills) {
        String lowerRole = role.toLowerCase();
        List<String> questions = new ArrayList<>();

        questions.add("Can you introduce yourself and walk us through your most impactful project as a " + role + "?");

        if (lowerRole.contains("python") || lowerRole.contains("data") || lowerRole.contains("ml") || lowerRole.contains("machine learning")) {
            questions.add("How do you handle data preprocessing, missing features, and outliers in your analytics pipeline?");
            questions.add("Explain the trade-offs between different evaluation metrics (e.g. precision vs recall, AUC-ROC) for imbalanced datasets.");
            questions.add("How do you optimize memory and runtime performance when processing large datasets with Python/SQL?");
            questions.add("Describe a scenario where a machine learning model degraded in production and how you monitored and resolved it.");
        } else if (lowerRole.contains("react") || lowerRole.contains("frontend") || lowerRole.contains("ui") || lowerRole.contains("javascript")) {
            questions.add("How do you optimize web application performance, Core Web Vitals, and reduce bundle size in modern frontend apps?");
            questions.add("Explain how state management architecture and re-render optimization work in large-scale client applications.");
            questions.add("How do you approach cross-browser compatibility, responsive CSS layouts, and web accessibility (a11y)?");
            questions.add("Describe your strategy for writing robust unit and end-to-end frontend tests.");
        } else if (lowerRole.contains("devops") || lowerRole.contains("cloud") || lowerRole.contains("sre") || lowerRole.contains("infrastructure")) {
            questions.add("How do you design high-availability, fault-tolerant infrastructure deployments in the cloud?");
            questions.add("Describe your experience automating zero-downtime CI/CD deployment pipelines.");
            questions.add("How do you manage infrastructure as code (IaC) and drift detection across multiple environments?");
            questions.add("Walk us through an incident response scenario where critical production services went down.");
        } else if (lowerRole.contains("product") || lowerRole.contains("manager") || lowerRole.contains("pm")) {
            questions.add("How do you prioritize conflicting feature requests from high-value stakeholders and engineering constraints?");
            questions.add("Describe how you define and track key product success metrics and KPIs for a new launch.");
            questions.add("How do you conduct customer discovery interviews and translate feedback into actionable user stories?");
            questions.add("Tell us about a time you had to make a critical product decision with incomplete data.");
        } else if (lowerRole.contains("java") || lowerRole.contains("spring")) {
            questions.add("Explain how dependency injection and bean lifecycle management operate within Spring applications.");
            questions.add("How do you handle database transaction management, isolation levels, and concurrency under high load?");
            questions.add("What patterns do you implement for building resilient, scalable microservices and RESTful APIs?");
            questions.add("Describe your experience diagnosing production memory leaks or JVM performance bottlenecks.");
        } else {
            questions.add("What are the key technical or domain methodologies you consider critical for success in this " + role + " role?");
            questions.add("How do you stay current with industry trends and integrate new tooling into your workflow?");
            questions.add("Describe a challenging technical problem you encountered and the step-by-step approach you took to resolve it.");
            questions.add("How do you ensure high quality, maintainability, and thorough testing in your deliverables?");
        }

        questions.add("Describe a situation where you had a technical disagreement with a colleague and how you resolved it collaboratively.");
        questions.add("How do you manage tight deadlines when scope or requirements change unexpectedly?");
        questions.add("Where do you see your technical trajectory evolving over the next 3 to 5 years as a " + role + "?");

        InterviewQuestionResponse response = new InterviewQuestionResponse(role, questions);
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}