package com.vasudev.jobtracker.exception;

import org.springframework.http.HttpStatus;

public class GeminiException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public GeminiException(String message) {
        super(message);
        this.errorCode = "GEMINI_ERROR";
        this.httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
    }

    public GeminiException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public GeminiException(String message, Throwable cause, String errorCode, HttpStatus httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
