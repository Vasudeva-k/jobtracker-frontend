package com.vasudev.jobtracker.exception;

import org.springframework.http.HttpStatus;

public class GeminiQuotaExceededException extends GeminiException {

    public GeminiQuotaExceededException() {
        super(
                "Gemini AI has reached its usage limit. Please try again later.",
                "GEMINI_QUOTA_EXCEEDED",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public GeminiQuotaExceededException(String message) {
        super(
                message != null ? message : "Gemini AI has reached its usage limit. Please try again later.",
                "GEMINI_QUOTA_EXCEEDED",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
