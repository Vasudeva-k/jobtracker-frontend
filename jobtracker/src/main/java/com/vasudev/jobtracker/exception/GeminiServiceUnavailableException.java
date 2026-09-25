package com.vasudev.jobtracker.exception;

import org.springframework.http.HttpStatus;

public class GeminiServiceUnavailableException extends GeminiException {

    public GeminiServiceUnavailableException() {
        super(
                "Gemini AI service is temporarily unavailable. Please try again later.",
                "GEMINI_SERVICE_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public GeminiServiceUnavailableException(String message) {
        super(
                message != null ? message : "Gemini AI service is temporarily unavailable. Please try again later.",
                "GEMINI_SERVICE_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
