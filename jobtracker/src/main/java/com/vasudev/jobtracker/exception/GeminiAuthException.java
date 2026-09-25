package com.vasudev.jobtracker.exception;

import org.springframework.http.HttpStatus;

public class GeminiAuthException extends GeminiException {

    public GeminiAuthException() {
        super(
                "Gemini API authentication failed. Please check your API configuration.",
                "GEMINI_AUTH_ERROR",
                HttpStatus.UNAUTHORIZED
        );
    }

    public GeminiAuthException(String message) {
        super(
                message != null ? message : "Gemini API authentication failed. Please check your API configuration.",
                "GEMINI_AUTH_ERROR",
                HttpStatus.UNAUTHORIZED
        );
    }
}
