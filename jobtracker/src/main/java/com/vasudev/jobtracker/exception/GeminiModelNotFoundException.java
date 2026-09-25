package com.vasudev.jobtracker.exception;

import org.springframework.http.HttpStatus;

public class GeminiModelNotFoundException extends GeminiException {

    public GeminiModelNotFoundException() {
        super(
                "Gemini model or endpoint not found. Please verify gemini.api.url configuration.",
                "GEMINI_MODEL_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public GeminiModelNotFoundException(String message) {
        super(
                message != null ? message : "Gemini model or endpoint not found. Please verify gemini.api.url configuration.",
                "GEMINI_MODEL_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}
