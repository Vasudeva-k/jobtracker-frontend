package com.vasudev.jobtracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.vasudev.jobtracker.exception.GeminiAuthException;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.exception.GeminiModelNotFoundException;
import com.vasudev.jobtracker.exception.GeminiQuotaExceededException;
import com.vasudev.jobtracker.exception.GeminiServiceUnavailableException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GeminiServiceTest {

    private GeminiService geminiService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        geminiService = new GeminiService(restTemplate);
    }

    @Test
    void testIsAvailable_WithPlaceholder() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "dummy_key_sample");
        assertFalse(geminiService.isAvailable());

        ReflectionTestUtils.setField(geminiService, "apiKey", "YOUR_GEMINI_API_KEY");
        assertFalse(geminiService.isAvailable());

        ReflectionTestUtils.setField(geminiService, "apiKey", "placeholder_key");
        assertFalse(geminiService.isAvailable());
    }

    @Test
    void testIsAvailable_WithValidKey() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");
        assertTrue(geminiService.isAvailable());

        ReflectionTestUtils.setField(geminiService, "apiKey", "AQ.SampleValidKey12345");
        assertTrue(geminiService.isAvailable());
    }

    @Test
    void testMaskedApiKey() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");
        assertEquals("****2345", geminiService.getMaskedApiKey());

        ReflectionTestUtils.setField(geminiService, "apiKey", "abcd");
        assertEquals("****", geminiService.getMaskedApiKey());
    }

    @Test
    void testAskGemini_ReturnsEmptyWhenUnavailable() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "YOUR_GEMINI_API_KEY");
        String result = geminiService.askGemini("Hello");
        assertEquals("", result);
    }

    @Test
    void testAskGemini_Success() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");

        Map<String, Object> mockBody = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(
                                        Map.of("text", "This is AI generated content")
                                )
                        ))
                )
        );

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(mockBody, HttpStatus.OK));

        String result = geminiService.askGemini("Generate cover letter");
        assertEquals("This is AI generated content", result);
    }

    @Test
    void testAskGemini_AuthenticationError_401() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        GeminiAuthException ex = assertThrows(GeminiAuthException.class, () -> geminiService.askGemini("Hello"));
        assertTrue(ex.getMessage().contains("authentication failed"));
        assertEquals("GEMINI_AUTH_ERROR", ex.getErrorCode());
    }

    @Test
    void testAskGemini_RateLimitError_429() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests"));

        GeminiQuotaExceededException ex = assertThrows(GeminiQuotaExceededException.class, () -> geminiService.askGemini("Hello"));
        assertEquals("Gemini AI has reached its usage limit. Please try again later.", ex.getMessage());
        assertEquals("GEMINI_QUOTA_EXCEEDED", ex.getErrorCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getHttpStatus());
    }

    @Test
    void testAskGemini_ModelNotFoundError_404() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        GeminiModelNotFoundException ex = assertThrows(GeminiModelNotFoundException.class, () -> geminiService.askGemini("Hello"));
        assertTrue(ex.getMessage().contains("Gemini model or endpoint not found"));
        assertEquals("GEMINI_MODEL_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void testAskGemini_ServerError_503() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable"));

        GeminiServiceUnavailableException ex = assertThrows(GeminiServiceUnavailableException.class, () -> geminiService.askGemini("Hello"));
        assertTrue(ex.getMessage().contains("temporarily unavailable"));
        assertEquals("GEMINI_SERVICE_UNAVAILABLE", ex.getErrorCode());
    }

    @Test
    void testAskGemini_NetworkTimeout() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "AIzaSyD-ValidKeySample12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> geminiService.askGemini("Hello"));
        assertTrue(ex.getMessage().contains("Network timeout or connection error"));
    }

    @Test
    void testExtractJson_FromMarkdownFences() {
        String wrapped = "```json\n{\n  \"key\": \"value\"\n}\n```";
        String extracted = GeminiService.extractJson(wrapped);
        assertEquals("{\n  \"key\": \"value\"\n}", extracted);
    }

    @Test
    void testExtractJson_WithSurroundingProse() {
        String prose = "Here is your JSON response:\n{\n  \"score\": 85\n}\nHope this helps!";
        String extracted = GeminiService.extractJson(prose);
        assertEquals("{\n  \"score\": 85\n}", extracted);
    }

    @Test
    void testExtractJson_ArrayResponse() {
        String arrayJson = "```\n[\"Java\", \"Python\", \"Docker\"]\n```";
        String extracted = GeminiService.extractJson(arrayJson);
        assertEquals("[\"Java\", \"Python\", \"Docker\"]", extracted);
    }

    @Test
    void testExtractModelName_Default() {
        assertEquals("gemini-3.5-flash", geminiService.extractModelName());
    }

    @Test
    void testExtractModelName_Custom() {
        ReflectionTestUtils.setField(geminiService, "apiUrl",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent");
        assertEquals("gemini-2.5-pro", geminiService.extractModelName());
    }
}
