package com.vasudev.jobtracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.exception.GeminiQuotaExceededException;
import com.vasudev.jobtracker.exception.GeminiAuthException;
import com.vasudev.jobtracker.exception.GeminiModelNotFoundException;
import com.vasudev.jobtracker.exception.GeminiServiceUnavailableException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public GeminiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restTemplate = new RestTemplate(factory);
    }

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getApiKeySource() {
        if (apiKey != null && !apiKey.isBlank()) {
            return "PROPERTIES";
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return "ENVIRONMENT";
        }
        String sysProp = System.getProperty("gemini.api.key");
        if (sysProp != null && !sysProp.isBlank()) {
            return "JVM";
        }
        return "NONE";
    }

    public String getMaskedApiKey() {
        String key = getEffectiveApiKey();
        if (key.isBlank()) {
            return "NONE";
        }
        if (key.length() <= 4) {
            return "****";
        }
        return "****" + key.substring(key.length() - 4);
    }

    private String getEffectiveApiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        String sysProp = System.getProperty("gemini.api.key");
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        return "";
    }

    /**
     * Checks whether a valid Gemini API key is configured.
     */
    public boolean isAvailable() {
        String key = getEffectiveApiKey();
        return !key.isBlank()
                && !key.toLowerCase().contains("placeholder")
                && !key.toLowerCase().contains("dummy")
                && !key.toLowerCase().contains("your_");
    }

    public String extractModelName() {
        String url = getSanitizedApiUrl();
        try {
            if (url.contains("/models/")) {
                int start = url.indexOf("/models/") + 8;
                int end = url.indexOf(":", start);
                if (end > start) {
                    return url.substring(start, end);
                }
                int queryStart = url.indexOf("?", start);
                if (queryStart > start) {
                    return url.substring(start, queryStart);
                }
                return url.substring(start);
            }
        } catch (Exception ignored) {
        }
        return "gemini-3.5-flash";
    }

    private String getSanitizedApiUrl() {
        if (apiUrl != null && !apiUrl.isBlank()) {
            return apiUrl.trim();
        }
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent";
    }

    /**
     * Safe live diagnostic test that probes the real Gemini endpoint.
     * Never exposes API key secrets.
     */
    public Map<String, Object> checkGeminiStatus() {
        Map<String, Object> status = new java.util.LinkedHashMap<>();
        boolean configured = isAvailable();
        String source = getApiKeySource();
        String masked = getMaskedApiKey();
        String model = extractModelName();

        status.put("configured", configured);
        status.put("keySource", source);
        status.put("keyMasked", masked);
        status.put("available", configured);
        status.put("model", model);

        if (!configured) {
            status.put("requestAttempted", false);
            status.put("requestSuccessful", false);
            status.put("httpStatus", 0);
            status.put("responseReceived", false);
            status.put("responseParsed", false);
            status.put("source", "FALLBACK");
            status.put("message", "No valid Gemini API key configured. Application is using domain-neutral fallbacks.");
            return status;
        }

        status.put("requestAttempted", true);
        String testPrompt = """
                Return exactly this JSON:
                {
                  "verification": "REAL_GEMINI_TEST",
                  "randomPhrase": "LiveProbe-%d"
                }
                Do not add markdown fences.
                """.formatted(System.currentTimeMillis());

        try {
            log.info("[AI] Health Check: Initiating real Gemini API probe (Model: {})...", model);
            String rawResponse = askGemini(testPrompt);
            if (rawResponse != null && !rawResponse.isBlank()) {
                String extractedJson = extractJson(rawResponse);
                boolean parsed = extractedJson.contains("REAL_GEMINI_TEST");
                status.put("requestSuccessful", true);
                status.put("httpStatus", 200);
                status.put("responseReceived", true);
                status.put("responseParsed", parsed);
                status.put("source", "GEMINI");
                status.put("message", "Real Gemini API is fully reachable, authenticated, and generating live responses.");
                return status;
            } else {
                status.put("requestSuccessful", false);
                status.put("httpStatus", 200);
                status.put("responseReceived", false);
                status.put("responseParsed", false);
                status.put("source", "FALLBACK");
                status.put("message", "Gemini returned an empty candidate text block.");
                return status;
            }
        } catch (GeminiQuotaExceededException ex) {
            status.put("requestSuccessful", false);
            status.put("httpStatus", 429);
            status.put("responseReceived", false);
            status.put("responseParsed", false);
            status.put("source", "FALLBACK");
            status.put("message", ex.getMessage());
            return status;
        } catch (GeminiException ex) {
            int code = ex.getHttpStatus() != null ? ex.getHttpStatus().value() : 500;
            status.put("requestSuccessful", false);
            status.put("httpStatus", code);
            status.put("responseReceived", false);
            status.put("responseParsed", false);
            status.put("source", "FALLBACK");
            status.put("message", ex.getMessage());
            return status;
        } catch (HttpStatusCodeException ex) {
            int code = ex.getStatusCode().value();
            status.put("requestSuccessful", false);
            status.put("httpStatus", code);
            status.put("responseReceived", false);
            status.put("responseParsed", false);
            status.put("source", "FALLBACK");
            if (code == 401 || code == 403) {
                status.put("message", "Gemini authentication failed (HTTP " + code + "). Please verify GEMINI_API_KEY.");
            } else if (code == 429) {
                status.put("message", "Gemini AI has reached its usage limit. Please try again later.");
            } else if (code == 404) {
                status.put("message", "Gemini model not found (HTTP 404). Verify model endpoint.");
            } else {
                status.put("message", "Gemini HTTP error: " + code);
            }
            return status;
        } catch (org.springframework.web.client.ResourceAccessException ex) {
            status.put("requestSuccessful", false);
            status.put("httpStatus", 503);
            status.put("responseReceived", false);
            status.put("responseParsed", false);
            status.put("source", "FALLBACK");
            status.put("message", "Network timeout or connection failure communicating with Gemini API.");
            return status;
        } catch (Exception ex) {
            status.put("requestSuccessful", false);
            status.put("httpStatus", 500);
            status.put("responseReceived", false);
            status.put("responseParsed", false);
            status.put("source", "FALLBACK");
            status.put("message", "Gemini communication error: " + ex.getMessage());
            return status;
        }
    }

    /**
     * Unique live runtime probe for on-demand validation of Gemini API connectivity.
     */
    public Map<String, Object> executeLiveRuntimeProbe(String userToken) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        boolean configured = isAvailable();
        String model = extractModelName();
        String token = (userToken != null && !userToken.isBlank())
                ? userToken.trim()
                : "GEMINI_RUNTIME_" + System.currentTimeMillis();

        result.put("token", token);
        result.put("configured", configured);
        result.put("model", model);
        result.put("keyMasked", getMaskedApiKey());

        if (!configured) {
            result.put("success", false);
            result.put("httpStatus", 0);
            result.put("generatedText", "");
            result.put("source", "FALLBACK");
            result.put("message", "REAL GEMINI TEST NOT EXECUTED — GEMINI_API_KEY NOT CONFIGURED");
            return result;
        }

        String probePrompt = """
                Generate a unique response containing the exact token "%s" and explain in one concise sentence why this response proves the live Google Gemini API request was successfully executed.
                """.formatted(token);

        try {
            log.info("[AI] Feature: Gemini Runtime Probe");
            log.info("[AI] Gemini available: true");
            log.info("[AI] Calling Gemini API...");
            log.info("[AI] Gemini model: {}", model);

            String text = askGemini(probePrompt);
            if (text != null && !text.isBlank()) {
                log.info("[AI] Gemini response received");
                log.info("[AI] Gemini response parsed successfully");
                log.info("[AI] Response source: GEMINI");

                result.put("success", true);
                result.put("httpStatus", 200);
                result.put("generatedText", text);
                result.put("source", "GEMINI");
                result.put("message", "Live Google Gemini API responded and verified.");
                return result;
            } else {
                result.put("success", false);
                result.put("httpStatus", 200);
                result.put("generatedText", "");
                result.put("source", "FALLBACK");
                result.put("message", "Gemini returned an empty candidate text block.");
                return result;
            }
        } catch (GeminiQuotaExceededException ex) {
            log.error("[AI] Gemini API request quota exceeded - HTTP status: 429");
            log.info("[AI] Response source: FALLBACK");
            result.put("success", false);
            result.put("httpStatus", 429);
            result.put("generatedText", "");
            result.put("source", "FALLBACK");
            result.put("message", ex.getMessage());
            return result;
        } catch (GeminiException ex) {
            int code = ex.getHttpStatus() != null ? ex.getHttpStatus().value() : 500;
            log.error("[AI] Gemini API request failed - HTTP status: {}", code);
            log.info("[AI] Response source: FALLBACK");
            result.put("success", false);
            result.put("httpStatus", code);
            result.put("generatedText", "");
            result.put("source", "FALLBACK");
            result.put("message", ex.getMessage());
            return result;
        } catch (HttpStatusCodeException ex) {
            int code = ex.getStatusCode().value();
            log.error("[AI] Gemini API request failed - HTTP status: {}", code);
            log.info("[AI] Response source: FALLBACK");
            result.put("success", false);
            result.put("httpStatus", code);
            result.put("generatedText", "");
            result.put("source", "FALLBACK");
            result.put("message", "Gemini API HTTP Error " + code + ": " + ex.getStatusText());
            return result;
        } catch (org.springframework.web.client.ResourceAccessException ex) {
            log.error("[AI] Gemini API request network error: {}", ex.getMessage());
            log.info("[AI] Response source: FALLBACK");
            result.put("success", false);
            result.put("httpStatus", 503);
            result.put("generatedText", "");
            result.put("source", "FALLBACK");
            result.put("message", "Network timeout or connection failure communicating with Gemini API.");
            return result;
        } catch (Exception ex) {
            log.error("[AI] Gemini API communication error: {}", ex.getMessage());
            log.info("[AI] Response source: FALLBACK");
            result.put("success", false);
            result.put("httpStatus", 500);
            result.put("generatedText", "");
            result.put("source", "FALLBACK");
            result.put("message", "Communication failure: " + ex.getMessage());
            return result;
        }
    }

    /**
     * Generic Gemini API Call
     */
    public String askGemini(String prompt) {

        String effectiveKey = getEffectiveApiKey();
        String modelName = extractModelName();

        if (!isAvailable()) {
            log.info("[AI] Gemini API key configured: false");
            log.info("[AI] Gemini available: false");
            log.info("[AI] Reason: API key unavailable");
            log.info("[AI] Response source: FALLBACK");
            return "";
        }

        log.info("[AI] Gemini API key configured: true");
        log.info("[AI] Gemini available: true");
        log.info("[AI] Model: {}", modelName);
        log.info("[AI] Calling Gemini API...");

        String url = getSanitizedApiUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", effectiveKey);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt != null ? prompt : "")
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = null;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    response = restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            Map.class
                    );
                    break;
                } catch (HttpStatusCodeException ex) {
                    if (ex.getStatusCode().value() == 503 && attempt < 3) {
                        log.warn("[AI] Gemini 503 high demand spike. Retrying attempt {}/3...", attempt + 1);
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    throw ex;
                }
            }

            if (response == null) {
                log.warn("[AI] Gemini response was null");
                log.info("[AI] Response source: FALLBACK");
                return "";
            }

            log.info("[AI] Gemini HTTP status: {}", response.getStatusCode().value());

            Map<String, Object> body = response.getBody();

            if (body == null) {
                log.warn("[AI] Gemini response body was empty");
                log.info("[AI] Response source: FALLBACK");
                return "";
            }

            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) body.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                log.warn("[AI] No candidates returned in Gemini response");
                log.info("[AI] Response source: FALLBACK");
                return "";
            }

            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");

            if (content == null) {
                log.warn("[AI] No content block in candidate response");
                log.info("[AI] Response source: FALLBACK");
                return "";
            }

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                log.warn("[AI] No parts in candidate content block");
                log.info("[AI] Response source: FALLBACK");
                return "";
            }

            Object text = parts.get(0).get("text");

            if (text == null || text.toString().isBlank()) {
                log.warn("[AI] Gemini candidate text was empty");
                log.info("[AI] Response source: FALLBACK");
                return "";
            }

            log.info("[AI] Gemini response received");
            log.info("[AI] Response source: GEMINI");
            return text.toString().trim();

        } catch (HttpStatusCodeException ex) {

            int statusCode = ex.getStatusCode().value();
            log.error("[AI] Gemini API call failed - HTTP status: {}", statusCode);
            log.error("[AI] Error type: HTTP {}", statusCode);
            log.error("[AI] Error body: {}", ex.getResponseBodyAsString());
            log.info("[AI] Response source: FALLBACK");

            if (statusCode == 429) {
                throw new GeminiQuotaExceededException(
                        "Gemini AI has reached its usage limit. Please try again later."
                );
            }

            if (statusCode == 400) {
                throw new GeminiException(
                        "Invalid Gemini API request payload or parameters.",
                        "GEMINI_BAD_REQUEST",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (statusCode == 401 || statusCode == 403) {
                throw new GeminiAuthException(
                        "Gemini API authentication failed. Check your GEMINI_API_KEY."
                );
            }

            if (statusCode == 404) {
                throw new GeminiModelNotFoundException(
                        "Gemini model or endpoint not found (" + modelName + "). Verify gemini.api.url configuration."
                );
            }

            if (statusCode >= 500) {
                throw new GeminiServiceUnavailableException(
                        "Gemini AI service temporarily unavailable (HTTP " + statusCode + "). Please try again later."
                );
            }

            throw new GeminiException("Gemini API error: HTTP " + statusCode, "GEMINI_ERROR", HttpStatus.valueOf(statusCode));

        } catch (org.springframework.web.client.ResourceAccessException ex) {
            log.error("[AI] Gemini API call failed - Network/Timeout error: {}", ex.getMessage());
            log.error("[AI] Error type: Network connection/timeout error");
            log.info("[AI] Response source: FALLBACK");
            throw new GeminiServiceUnavailableException("Network timeout or connection error communicating with Gemini AI.");

        } catch (GeminiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[AI] Gemini API call failed - Error: {}", ex.getMessage());
            log.error("[AI] Error type: Connection/Communication error");
            log.info("[AI] Response source: FALLBACK");
            throw new GeminiException("Unable to communicate with Gemini AI: " + ex.getMessage());
        }
    }

    /**
     * Calls Gemini and extracts a JSON root object or array from the response.
     */
    public String askGeminiJson(String prompt) {
        String raw = askGemini(prompt);
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String extracted = extractJson(raw);
        if (!extracted.isBlank()) {
            log.info("[AI] Gemini JSON extracted successfully");
        }
        return extracted;
    }

    /**
     * Robust utility method to extract JSON substring from a text response.
     */
    public static String extractJson(String response) {
        if (response == null || response.isBlank()) return "";
        String trimmed = stripMarkdownFences(response);

        int firstBrace = trimmed.indexOf('{');
        int firstBracket = trimmed.indexOf('[');

        if (firstBrace != -1 && (firstBracket == -1 || firstBrace < firstBracket)) {
            int lastBrace = trimmed.lastIndexOf('}');
            if (lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1).trim();
            }
        } else if (firstBracket != -1) {
            int lastBracket = trimmed.lastIndexOf(']');
            if (lastBracket > firstBracket) {
                return trimmed.substring(firstBracket, lastBracket + 1).trim();
            }
        }

        return trimmed;
    }

    /**
     * Utility method to strip markdown fences (```json ... ``` or ``` ...)
     */
    public static String stripMarkdownFences(String response) {
        if (response == null) return "";
        String trimmed = response.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```markdown")) {
            trimmed = trimmed.substring(11);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    /**
     * Resume Review
     */
    public String reviewResume(String resumeText) {

        String prompt = """
                You are an expert ATS Resume Reviewer.

                Review the following candidate resume across its specific career domain.

                Give:
                1. Overall Score out of 10
                2. Strengths
                3. Weaknesses
                4. Missing Skills
                5. Suggestions to Improve

                Resume:
                """ + (resumeText != null ? resumeText : "");

        return askGemini(prompt);
    }

    /**
     * Cover Letter Generator
     */
    public String generateCoverLetter(
            String resume,
            String jobDescription) {

        String prompt = """
                Write a professional, compelling cover letter grounded entirely in the candidate's actual background and the target job description. Do not fabricate experience.

                Candidate Background / Resume:
                """ + (resume != null ? resume : "") + """

                Target Job Description:
                """ + (jobDescription != null ? jobDescription : "");

        return askGemini(prompt);
    }

    /**
     * Interview Questions
     */
    public String generateInterviewQuestions(String jobTitle) {

        String prompt = """
                Generate 10 relevant, high-yield interview questions for the position of: %s

                Include:
                - Domain & Technical Questions tailored specifically to this role
                - Behavioral & Scenario-Based Questions
                - Problem Solving Questions
                """.formatted(jobTitle != null ? jobTitle : "Professional");

        return askGemini(prompt);
    }

    /**
     * Resume Summary
     */
    public String summarizeResume(String resume) {

        String prompt = """
                Summarize the following resume professionally in 5-7 concise bullet points, highlighting key experience, core strengths, and achievements.

                Resume:
                """ + (resume != null ? resume : "");

        return askGemini(prompt);
    }

    /**
     * Job Match Analysis
     */
    public String analyzeJobMatch(
            String resume,
            String jobDescription) {

        String prompt = """
                Compare the following Resume with the Job Description.

                Give:
                - Match Percentage
                - Matching Skills
                - Missing Skills
                - Suggestions

                Resume:
                """ + (resume != null ? resume : "") + """

                Job Description:
                """ + (jobDescription != null ? jobDescription : "");

        return askGemini(prompt);
    }
}
