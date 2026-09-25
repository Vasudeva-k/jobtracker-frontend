package com.vasudev.jobtracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.MockInterviewRequest;
import com.vasudev.jobtracker.dto.MockInterviewResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MockInterviewSimulator {

    private static final Logger log = LoggerFactory.getLogger(MockInterviewSimulator.class);

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MockInterviewSimulator(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public MockInterviewSimulator() {
        this.geminiService = null;
    }

    public MockInterviewResponse evaluate(MockInterviewRequest request) {

        String role = (request != null && request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().trim()
                : "Software Engineer";

        List<String> rawAnswers = (request != null && request.getAnswers() != null)
                ? request.getAnswers()
                : List.of();

        List<String> validAnswers = new ArrayList<>();
        for (String ans : rawAnswers) {
            if (ans != null && !ans.trim().isBlank()) {
                validAnswers.add(ans.trim());
            }
        }

        if (validAnswers.isEmpty()) {
            return new MockInterviewResponse(
                    0,
                    List.of("No answers provided for evaluation. Please provide substantive technical and behavioral answers."),
                    List.of(),
                    List.of("Provide detailed answers showcasing your technical depth and problem-solving process."),
                    "Needs significant preparation before interviews."
            );
        }

        boolean geminiAvailable = geminiService != null && geminiService.isAvailable();
        log.info("[AI] Feature: Mock Interview Simulator");
        log.info("[AI] Gemini available: {}", geminiAvailable);

        if (geminiAvailable) {
            StringBuilder answersText = new StringBuilder();
            for (int i = 0; i < validAnswers.size(); i++) {
                answersText.append("Answer ").append(i + 1).append(":\n").append(validAnswers.get(i)).append("\n\n");
            }

            String prompt = """
                    You are an expert Technical Interview Evaluator. Evaluate the candidate's interview responses for the target role: "%s".

                    Evaluate technical accuracy, problem-solving depth, communication clarity, and completeness.

                    Return ONLY a valid JSON object. Do not include markdown code block formatting or explanations:
                    {
                      "score": <integer 0-100>,
                      "feedback": ["feedback point 1", "feedback point 2"],
                      "strengths": ["strength 1", "strength 2"],
                      "improvements": ["improvement suggestion 1", "improvement suggestion 2"],
                      "recommendation": "<Concise final readiness verdict>"
                    }

                    Guidelines:
                    - Evaluate specifically against the domain requirements of "%s".
                    - Do not require or assume Java/Spring Boot knowledge unless "%s" specifically requires it.

                    Target Role: %s
                    Candidate Responses:
                    %s
                    """.formatted(role, role, role, role, answersText.toString());

            try {
                log.info("[AI] Calling Gemini...");
                String jsonResponse = geminiService.askGeminiJson(prompt);
                log.info("[AI] Gemini response received");
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(jsonResponse);
                    log.info("[AI] Gemini JSON parsed successfully");

                    int score = root.has("score") ? Math.min(100, Math.max(0, root.get("score").asInt())) : 70;
                    List<String> feedback = extractStringList(root, "feedback");
                    List<String> strengths = extractStringList(root, "strengths");
                    List<String> improvements = extractStringList(root, "improvements");
                    String recommendation = root.has("recommendation") ? root.get("recommendation").asText() : "Good performance.";

                    if (!feedback.isEmpty() || !strengths.isEmpty() || !improvements.isEmpty()) {
                        MockInterviewResponse response = new MockInterviewResponse(score, feedback, strengths, improvements, recommendation);
                        response.setSource("GEMINI");
                        log.info("[AI] Response source: GEMINI");
                        return response;
                    }
                }
            } catch (GeminiException e) {
                log.error("[AI] Gemini API error for Mock Interview: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.warn("[AI] Gemini call failed for Mock Interview: {}. Using domain-neutral fallback", e.getMessage());
            }
        }

        log.info("[AI] Using domain-neutral fallback");
        return generateDomainNeutralEvaluation(validAnswers, role);
    }

    private List<String> extractStringList(JsonNode root, String fieldName) {
        List<String> list = new ArrayList<>();
        if (root.has(fieldName) && root.get(fieldName).isArray()) {
            for (JsonNode item : root.get(fieldName)) {
                String text = item.asText().trim();
                if (!text.isBlank()) {
                    list.add(text);
                }
            }
        }
        return list;
    }

    private MockInterviewResponse generateDomainNeutralEvaluation(List<String> answers, String role) {
        List<String> feedback = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        int totalWords = 0;
        int substantiveAnswers = 0;

        for (String ans : answers) {
            String[] words = ans.split("\\s+");
            totalWords += words.length;
            if (words.length >= 15) {
                substantiveAnswers++;
            }
        }

        int avgWords = answers.isEmpty() ? 0 : totalWords / answers.size();

        if (avgWords >= 25) {
            strengths.add("Comprehensive, articulate explanations");
            feedback.add("Provided thorough and detailed responses demonstrating communication clarity.");
        } else if (avgWords >= 10) {
            strengths.add("Direct and concise communication");
            feedback.add("Answers are clear but could benefit from deeper technical examples.");
        } else {
            improvements.add("Elaborate with deeper architectural details and concrete project examples");
            feedback.add("Responses are relatively brief. Elaborating on real-world trade-offs will strengthen your interview.");
        }

        if (substantiveAnswers >= 2) {
            strengths.add("Demonstrated domain competence in " + role);
        } else {
            improvements.add("Structure answers using the STAR method (Situation, Task, Action, Result)");
        }

        int score = Math.min(95, Math.max(30, 40 + (substantiveAnswers * 15) + Math.min(25, avgWords)));

        String recommendation;
        if (score >= 85) {
            recommendation = "Excellent! You demonstrate strong readiness for " + role + " interviews.";
        } else if (score >= 70) {
            recommendation = "Good performance. Review and practice articulating complex technical trade-offs.";
        } else if (score >= 50) {
            recommendation = "Average performance. Practice elaborating on technical details and past achievements.";
        } else {
            recommendation = "Needs significant preparation before interviews.";
        }

        if (improvements.isEmpty()) {
            improvements.add("Continue practicing scenario-based system and behavioral questions.");
        }

        MockInterviewResponse response = new MockInterviewResponse(
                score,
                feedback,
                strengths,
                improvements,
                recommendation
        );
        response.setSource("FALLBACK");
        log.info("[AI] Response source: FALLBACK");
        return response;
    }
}

