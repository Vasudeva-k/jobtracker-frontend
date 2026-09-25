package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.CompanyInterviewRequest;
import com.vasudev.jobtracker.dto.CompanyInterviewResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyInterviewExperienceGenerator {

    private static final Logger log = LoggerFactory.getLogger(CompanyInterviewExperienceGenerator.class);
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompanyInterviewExperienceGenerator(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public CompanyInterviewExperienceGenerator() {
        this.geminiService = null;
    }

    public CompanyInterviewResponse generate(CompanyInterviewRequest request) {
        String rawCompany = (request != null && request.getCompany() != null && !request.getCompany().isBlank())
                ? request.getCompany().trim()
                : "General";

        log.info("[AI] Feature: Company Interview Experience - Generating interview prep for company: {}", rawCompany);

        if (geminiService != null && geminiService.isAvailable()) {
            try {
                String prompt = """
                        You are an expert interview coach and hiring manager.
                        Generate interview preparation questions, key topics, and tips for candidates interviewing at:
                        <company>%s</company>
                        
                        Return a valid JSON object matching this exact schema:
                        {
                          "company": "%s",
                          "technicalQuestions": ["technical question 1", "technical question 2", "technical question 3", "technical question 4"],
                          "hrQuestions": ["behavioral/hr question 1", "behavioral/hr question 2", "behavioral/hr question 3"],
                          "codingTopics": ["topic 1", "topic 2", "topic 3", "topic 4"],
                          "tips": ["preparation tip 1", "preparation tip 2", "preparation tip 3"]
                        }
                        
                        Ground the questions in the company's real engineering culture, interview format, and values without biasing toward any single programming language unless company-specific.
                        """.formatted(rawCompany, rawCompany);

                log.info("[AI] Calling Gemini for Company Interview Experience...");
                String jsonStr = geminiService.askGeminiJson(prompt);
                JsonNode root = objectMapper.readTree(jsonStr);

                String company = root.has("company") ? root.get("company").asText() : rawCompany;
                List<String> technicalQuestions = extractList(root, "technicalQuestions");
                if (technicalQuestions.isEmpty()) {
                    technicalQuestions = extractList(root, "systemDesignTopics");
                }
                List<String> hrQuestions = extractList(root, "hrQuestions");
                if (hrQuestions.isEmpty()) {
                    hrQuestions = extractList(root, "behavioralQuestions");
                }
                List<String> codingTopics = extractList(root, "codingTopics");
                List<String> tips = extractList(root, "tips");

                if (!codingTopics.isEmpty() || !technicalQuestions.isEmpty() || !hrQuestions.isEmpty()) {
                    log.info("[AI] Gemini JSON parsed successfully for Company Interview Experience");
                    CompanyInterviewResponse response = new CompanyInterviewResponse(
                            company,
                            technicalQuestions,
                            hrQuestions,
                            codingTopics,
                            tips
                    );
                    response.setSource("GEMINI");
                    log.info("[AI] Response source: GEMINI");
                    return response;
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Company Interview Experience: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Company Interview Experience: {}. Falling back.", ex.getMessage());
            }
        } else {
            log.info("[AI] Gemini unavailable for Company Interview Experience. Using domain-neutral fallback.");
        }

        return generateFallbackCompanyInterview(rawCompany);
    }

    private List<String> extractList(JsonNode root, String field) {
        List<String> list = new ArrayList<>();
        if (root.has(field) && root.get(field).isArray()) {
            for (JsonNode n : root.get(field)) {
                list.add(n.asText());
            }
        }
        return list;
    }

    private CompanyInterviewResponse generateFallbackCompanyInterview(String rawCompany) {
        String companyLower = rawCompany.toLowerCase();

        List<String> technicalQuestions = new ArrayList<>();
        List<String> hrQuestions = new ArrayList<>();
        List<String> codingTopics = new ArrayList<>();
        List<String> tips = new ArrayList<>();

        if (companyLower.contains("amazon")) {
            technicalQuestions.add("Explain system design principles for high availability and low latency.");
            technicalQuestions.add("Describe the trade-offs between SQL and NoSQL for distributed services.");
            technicalQuestions.add("How do you handle API throttling and eventual consistency?");
            technicalQuestions.add("Explain how concurrency and race conditions are mitigated in backend services.");

            hrQuestions.add("Tell me about a time you had to make a decision with incomplete information (Bias for Action).");
            hrQuestions.add("Describe a situation where you had a disagreement with a team member and how you resolved it (Have Backbone; Disagree and Commit).");
            hrQuestions.add("Give an example of when you went above and beyond for a customer (Customer Obsession).");
            hrQuestions.add("Explain Amazon Leadership Principles with concrete examples.");

            codingTopics.add("Arrays and Strings");
            codingTopics.add("Trees and Graphs");
            codingTopics.add("Dynamic Programming");
            codingTopics.add("System Design");

            tips.add("Format your behavioral answers strictly around the STAR method.");
            tips.add("Study Amazon Leadership Principles in depth with 2 stories per principle.");
            tips.add("Focus on scalability and edge cases during coding rounds.");

        } else if (companyLower.contains("google")) {
            technicalQuestions.add("How would you optimize algorithm complexity from O(n^2) to O(n log n)?");
            technicalQuestions.add("Explain memory management, caching strategies, and cache invalidation.");
            technicalQuestions.add("Design a globally distributed rate limiter.");
            technicalQuestions.add("Explain asynchronous event handling and concurrency models.");

            hrQuestions.add("Tell me about your most technically challenging project.");
            hrQuestions.add("How do you handle open-ended ambiguity in technical requirements?");
            hrQuestions.add("Describe a situation where you received critical constructive feedback.");

            codingTopics.add("Graph Algorithms (BFS/DFS, Dijkstra)");
            codingTopics.add("Dynamic Programming & Recursion");
            codingTopics.add("Tree Traversals & Binary Search");
            codingTopics.add("System Design & Scale");

            tips.add("Focus on algorithms and practice LeetCode problems.");
            tips.add("Communicate your thought process out loud before writing any code.");
            tips.add("Always analyze time and space complexity upfront.");

        } else if (companyLower.contains("microsoft")) {
            technicalQuestions.add("Explain OOP concepts, design patterns, and SOLID principles.");
            technicalQuestions.add("How do you design scalable cloud architectures with microservices?");
            technicalQuestions.add("Compare relational databases with document stores for transactional workloads.");
            technicalQuestions.add("How do you secure web APIs and handle authentication tokens?");

            hrQuestions.add("Tell me about a time you collaborated across cross-functional teams.");
            hrQuestions.add("Why Microsoft and how do you align with a growth mindset culture?");
            hrQuestions.add("Describe a time you failed and what you learned from it.");

            codingTopics.add("Arrays, Strings, and Hash Tables");
            codingTopics.add("Linked Lists and Two Pointers");
            codingTopics.add("Binary Trees and Search");
            codingTopics.add("API and Object Design");

            tips.add("Emphasize collaboration, adaptability, and continuous learning.");
            tips.add("Write clean, modular, and maintainable code with descriptive naming.");
            tips.add("Be prepared for design questions focused on cloud architecture.");

        } else {
            technicalQuestions.add("Explain core architectural patterns and principles you use in production.");
            technicalQuestions.add("How do you design and document secure RESTful or GraphQL APIs?");
            technicalQuestions.add("Describe your testing strategy (unit, integration, end-to-end).");
            technicalQuestions.add("How do you identify and debug performance bottlenecks in your applications?");

            hrQuestions.add("Tell me about yourself and your proudest technical achievement.");
            hrQuestions.add("Why are you interested in joining " + rawCompany + "?");
            hrQuestions.add("How do you prioritize competing deadlines in an agile sprint?");
            hrQuestions.add("Describe a challenging technical disagreement and how you resolved it.");

            codingTopics.add("Data Structures & Algorithms");
            codingTopics.add("String & Array Manipulation");
            codingTopics.add("Database Query Optimization");
            codingTopics.add("Clean Code & System Design");

            tips.add("Research " + rawCompany + "'s business model, tech stack, and recent product launches.");
            tips.add("Prepare 3-4 structured STAR stories highlighting impact and metrics.");
            tips.add("Ask thoughtful questions to the interviewer about engineering challenges and roadmap.");
        }

        CompanyInterviewResponse response = new CompanyInterviewResponse(
                rawCompany,
                technicalQuestions,
                hrQuestions,
                codingTopics,
                tips
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}
