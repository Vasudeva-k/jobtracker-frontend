package com.vasudev.jobtracker.ai;

import com.vasudev.jobtracker.dto.CareerRoadmapRequest;
import com.vasudev.jobtracker.dto.CareerRoadmapResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CareerRoadmapGenerator {

    private static final Logger log = LoggerFactory.getLogger(CareerRoadmapGenerator.class);
    private final GeminiService geminiService;

    public CareerRoadmapGenerator(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public CareerRoadmapResponse generate(CareerRoadmapRequest request) {

        String targetRole = (request != null && request.getTargetRole() != null && !request.getTargetRole().isBlank())
                ? request.getTargetRole().trim()
                : "Software Engineer";

        // Sanitize input
        String sanitizedRole = targetRole.replaceAll("[\\r\\n]", " ").replaceAll("[<>{}]", "").trim();
        if (sanitizedRole.isBlank()) {
            sanitizedRole = "Software Engineer";
        }

        log.info("[AI] Feature: Career Roadmap - Generating roadmap for role: {}", sanitizedRole);

        String prompt = """
You are an expert Career Mentor.

Create a learning roadmap for the following career.

Return ONLY in this format.

Target Role:
<role>

Roadmap:

- Step 1
- Step 2
- Step 3
- Step 4
- Step 5
- Step 6
- Step 7
- Step 8

Target Role:
%s
""".formatted(sanitizedRole);

        String aiResponse = "";
        if (geminiService != null && geminiService.isAvailable()) {
            try {
                log.info("[AI] Calling Gemini for Career Roadmap...");
                aiResponse = geminiService.askGemini(prompt);
                if (aiResponse != null && !aiResponse.isBlank()) {
                    log.info("[AI] Gemini response received for Career Roadmap");
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Career Roadmap: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Career Roadmap: {}. Falling back.", ex.getMessage());
                aiResponse = null;
            }
        } else {
            log.info("[AI] Gemini unavailable for Career Roadmap. Using domain-neutral fallback.");
        }

        String role = sanitizedRole;
        List<String> roadmap = new ArrayList<>();

        if (aiResponse == null || aiResponse.isBlank()) {
            return getFallbackRoadmap(sanitizedRole);
        }

        // Clean markdown code blocks if any
        String cleanedResponse = GeminiService.stripMarkdownFences(aiResponse);

        boolean roadmapSection = false;

        for (String line : cleanedResponse.split("\\R")) {

            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.toLowerCase().startsWith("target role")) {

                if (line.contains(":")) {

                    String value = line.substring(line.indexOf(":") + 1).trim();

                    if (!value.isEmpty()) {
                        role = value;
                    }
                }

                continue;
            }

            if (line.equalsIgnoreCase("Roadmap:")
                    || line.toLowerCase().startsWith("roadmap")) {

                roadmapSection = true;
                continue;
            }

            if (roadmapSection) {
                String stepText = line.replaceFirst("^[-*\\d.)\\s]+", "").trim();
                if (!stepText.isBlank() && !stepText.equalsIgnoreCase("roadmap:")) {
                    roadmap.add(stepText);
                }
            } else if (line.startsWith("-") || line.startsWith("*") || line.matches("^\\d+[.)].*")
                    || line.toLowerCase().startsWith("step") || line.toLowerCase().startsWith("month")
                    || line.toLowerCase().startsWith("phase") || line.toLowerCase().startsWith("week")) {
                String stepText = line.replaceFirst("^[-*\\d.)\\s]+", "").trim();
                if (!stepText.isBlank() && !stepText.toLowerCase().contains("target role")) {
                    roadmap.add(stepText);
                }
            }
        }

        if (roadmap.isEmpty()) {
            return getFallbackRoadmap(sanitizedRole);
        }

        CareerRoadmapResponse response = new CareerRoadmapResponse(role, roadmap);
        response.setSource("GEMINI");
        log.info("[AI] Response source: GEMINI");
        return response;
    }

    private CareerRoadmapResponse getFallbackRoadmap(String role) {
        String lowerRole = role.toLowerCase();
        List<String> steps = new ArrayList<>();

        if (lowerRole.contains("python") || lowerRole.contains("data") || lowerRole.contains("ml") || lowerRole.contains("ai")) {
            steps.add("Step 1: Master Python programming, data structures, and statistical computing.");
            steps.add("Step 2: Learn data manipulation and analysis with Pandas, NumPy, and SQL databases.");
            steps.add("Step 3: Study machine learning modeling and algorithms using Scikit-Learn.");
            steps.add("Step 4: Explore deep learning, PyTorch/TensorFlow, and model evaluation metrics.");
            steps.add("Step 5: Learn MLOps, Docker containerization, and production model deployment APIs.");
        } else if (lowerRole.contains("react") || lowerRole.contains("frontend") || lowerRole.contains("ui")) {
            steps.add("Step 1: Master modern JavaScript (ES6+), TypeScript, and CSS layouts/responsive design.");
            steps.add("Step 2: Deep-dive into React, component architecture, hooks, and state management.");
            steps.add("Step 3: Integrate REST/GraphQL APIs, handle caching, and optimize web performance.");
            steps.add("Step 4: Implement automated frontend testing (Jest, React Testing Library, Cypress).");
            steps.add("Step 5: Build scalable web applications and master Next.js and build tooling.");
        } else if (lowerRole.contains("devops") || lowerRole.contains("cloud") || lowerRole.contains("sre")) {
            steps.add("Step 1: Master Linux administration, shell scripting, and networking fundamentals.");
            steps.add("Step 2: Learn containerization with Docker and orchestration with Kubernetes.");
            steps.add("Step 3: Implement Infrastructure as Code (IaC) using Terraform / CloudFormation.");
            steps.add("Step 4: Build robust CI/CD automation pipelines using GitHub Actions or GitLab CI.");
            steps.add("Step 5: Configure cloud monitoring, logging, and incident response tooling.");
        } else if (lowerRole.contains("product") || lowerRole.contains("manager") || lowerRole.contains("pm")) {
            steps.add("Step 1: Learn product discovery, customer interview techniques, and market research.");
            steps.add("Step 2: Master user story mapping, backlog grooming, and Agile/Scrum methodologies.");
            steps.add("Step 3: Understand product analytics, A/B testing, and funnel optimization metrics.");
            steps.add("Step 4: Practice cross-functional leadership, stakeholder management, and roadmap planning.");
            steps.add("Step 5: Drive end-to-end product launches and measure post-release business impact.");
        } else if (lowerRole.contains("java") || lowerRole.contains("spring")) {
            steps.add("Step 1: Master Core Java (Java 17/21), OOP principles, and concurrency fundamentals.");
            steps.add("Step 2: Build scalable backend services using Spring Boot, Spring Data JPA, and Hibernate.");
            steps.add("Step 3: Design RESTful APIs, database indexing, and SQL transaction management.");
            steps.add("Step 4: Learn microservices patterns, Docker containerization, and Spring Security/JWT.");
            steps.add("Step 5: Implement automated testing with JUnit/Mockito and deploy to cloud environments.");
        } else {
            steps.add("Step 1: Master foundational principles, core methodologies, and essential tooling for " + role + ".");
            steps.add("Step 2: Develop practical domain competencies through structured real-world projects.");
            steps.add("Step 3: Learn industry-standard collaboration tools, workflows, and quality assurance.");
            steps.add("Step 4: Build an impressive portfolio highlighting measurable achievements and deliverables.");
            steps.add("Step 5: Prepare for technical/behavioral interviews and network within the " + role + " community.");
        }

        CareerRoadmapResponse response = new CareerRoadmapResponse(role, steps);
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}