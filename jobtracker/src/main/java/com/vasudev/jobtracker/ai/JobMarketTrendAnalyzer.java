package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.JobMarketTrendRequest;
import com.vasudev.jobtracker.dto.JobMarketTrendResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobMarketTrendAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(JobMarketTrendAnalyzer.class);

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobMarketTrendAnalyzer(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public JobMarketTrendAnalyzer() {
        this.geminiService = null;
    }

    public JobMarketTrendResponse analyze(JobMarketTrendRequest request) {
        String rawRole = (request != null && request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().trim()
                : "Software Engineer";

        boolean geminiAvailable = geminiService != null && geminiService.isAvailable();
        log.info("[AI] Feature: Job Market Trends");
        log.info("[AI] Gemini available: {}", geminiAvailable);

        if (geminiAvailable) {
            try {
                log.info("[AI] Calling Gemini...");
                String prompt = """
                        You are an expert labor market and tech industry analyst.
                        Analyze current industry market trends for the following role:
                        <role>%s</role>
                        
                        Return a valid JSON object matching this exact schema:
                        {
                          "role": "%s",
                          "topSkills": ["skill 1", "skill 2", "skill 3", "skill 4", "skill 5"],
                          "trendingTechnologies": ["tech 1", "tech 2", "tech 3", "tech 4"],
                          "averageSalaryRange": "e.g. $80k - $130k or 8 - 18 LPA (AI Estimate)",
                          "marketDemand": "High" | "Very High" | "Moderate",
                          "hiringGrowth": "Excellent" | "Positive" | "Steady"
                        }
                        
                        Ensure the skills and technologies are strictly relevant to the requested role without assuming any specific programming language unless specified in the role.
                        """.formatted(rawRole, rawRole);

                String jsonStr = geminiService.askGeminiJson(prompt);
                log.info("[AI] Gemini response received");
                JsonNode root = objectMapper.readTree(jsonStr);
                log.info("[AI] Gemini JSON parsed successfully");

                String role = root.has("role") ? root.get("role").asText() : rawRole;
                List<String> topSkills = new ArrayList<>();
                if (root.has("topSkills") && root.get("topSkills").isArray()) {
                    for (JsonNode n : root.get("topSkills")) {
                        topSkills.add(n.asText());
                    }
                }
                List<String> trendingTechnologies = new ArrayList<>();
                if (root.has("trendingTechnologies") && root.get("trendingTechnologies").isArray()) {
                    for (JsonNode n : root.get("trendingTechnologies")) {
                        trendingTechnologies.add(n.asText());
                    }
                }
                String salary = root.has("averageSalaryRange") ? root.get("averageSalaryRange").asText() : "Competitive (AI Estimate)";
                String demand = root.has("marketDemand") ? root.get("marketDemand").asText() : "High";
                String growth = root.has("hiringGrowth") ? root.get("hiringGrowth").asText() : "Positive";

                if (!topSkills.isEmpty() && !trendingTechnologies.isEmpty()) {
                    JobMarketTrendResponse response = new JobMarketTrendResponse(
                            role,
                            topSkills,
                            trendingTechnologies,
                            salary,
                            demand,
                            growth
                    );
                    response.setSource("GEMINI");
                    log.info("[AI] Response source: GEMINI");
                    return response;
                }
            } catch (GeminiException e) {
                log.error("[AI] Gemini API error for Job Market Trends: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.warn("[AI] Gemini call failed for Job Market Trends: {}. Using domain-neutral fallback", e.getMessage());
            }
        }

        log.info("[AI] Using domain-neutral fallback");
        return generateFallbackTrends(rawRole);
    }

    private JobMarketTrendResponse generateFallbackTrends(String rawRole) {
        String roleLower = rawRole.toLowerCase();
        JobMarketTrendResponse response;

        if (roleLower.contains("java")) {
            response = new JobMarketTrendResponse(
                    "Java Developer",
                    List.of("Java", "Spring Boot", "Hibernate", "REST API", "Microservices", "Docker", "AWS", "Git"),
                    List.of("Java 21", "Spring Boot 3", "Docker", "Kubernetes", "AWS"),
                    "8 - 18 LPA (Market Estimate)",
                    "High",
                    "Excellent"
            );
        } else if (roleLower.contains("python") || roleLower.contains("django") || roleLower.contains("fastapi")) {
            response = new JobMarketTrendResponse(
                    rawRole,
                    List.of("Python", "FastAPI", "Django", "PostgreSQL", "Docker", "REST API", "Git"),
                    List.of("AsyncIO", "Docker", "Cloud APIs", "LangChain"),
                    "8 - 20 LPA (Market Estimate)",
                    "Very High",
                    "Excellent"
            );
        } else if (roleLower.contains("full") || roleLower.contains("stack") || roleLower.contains("mern") || roleLower.contains("mean") || roleLower.contains("frontend") || roleLower.contains("react")) {
            response = new JobMarketTrendResponse(
                    "Full Stack Developer",
                    List.of("React", "TypeScript", "JavaScript", "Node.js", "REST APIs", "CSS3", "Git"),
                    List.of("React 19", "Next.js", "Tailwind CSS", "Vite", "TypeScript"),
                    "7 - 20 LPA (Market Estimate)",
                    "Very High",
                    "Excellent"
            );
        } else if (roleLower.contains("data") || roleLower.contains("machine learning") || roleLower.contains("ai") || roleLower.contains("ml")) {
            response = new JobMarketTrendResponse(
                    "Data Scientist",
                    List.of("Python", "Machine Learning", "SQL", "Statistics", "Data Pipelines", "Pandas"),
                    List.of("Generative AI", "LLMs", "Vector DBs", "LangChain", "PyTorch"),
                    "10 - 25 LPA (Market Estimate)",
                    "Very High",
                    "Excellent"
            );
        } else if (roleLower.contains("devops") || roleLower.contains("cloud") || roleLower.contains("sre") || roleLower.contains("infrastructure")) {
            response = new JobMarketTrendResponse(
                    "DevOps Engineer",
                    List.of("Docker", "Kubernetes", "AWS", "Linux", "Terraform", "CI/CD"),
                    List.of("Kubernetes", "Terraform", "GitHub Actions", "ArgoCD"),
                    "10 - 22 LPA (Market Estimate)",
                    "High",
                    "Excellent"
            );
        } else {
            response = new JobMarketTrendResponse(
                    rawRole,
                    List.of("Problem Solving", "Domain Core Concepts", "Collaboration", "System Architecture", "Version Control"),
                    List.of("Modern Frameworks", "Cloud Platforms", "Automation Tools"),
                    "Competitive Market Rate (Market Estimate)",
                    "Moderate",
                    "Positive"
            );
        }

        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}