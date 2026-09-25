package com.vasudev.jobtracker.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleTypeMismatch() {
        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc",
                Long.class,
                "id",
                parameter,
                new NumberFormatException("For input string: \"abc\"")
        );

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleTypeMismatch(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid Parameter", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").contains("id"));
        assertNull(response.getBody().get("stackTrace"));
    }

    @Test
    void testHandleNoResourceFound() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNoResourceFound(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Requested resource does not exist", response.getBody().get("message"));
    }

    @Test
    void testHandleValidationExceptions() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("jobRequest", "companyName", "Company name is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Error", response.getBody().get("error"));
        assertEquals("Company name is required", response.getBody().get("message"));
        assertNotNull(response.getBody().get("fieldErrors"));
    }

    @Test
    void testHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadCredentials(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication Failed", response.getBody().get("error"));
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @Test
    void testHandleDisabledAccount() {
        DisabledException ex = new DisabledException("Account is disabled");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDisabledAccount(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Account Disabled", response.getBody().get("error"));
    }

    @Test
    void testHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccessDenied(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input value");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Invalid input value", response.getBody().get("message"));
    }

    @Test
    void testHandleMaxUploadSize() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(10485760);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleMaxUploadSize(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File Size Limit Exceeded", response.getBody().get("error"));
    }

    @Test
    void testHandleRuntimeException_GeminiError() {
        RuntimeException ex = new RuntimeException("Gemini rate limit or quota exceeded.");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("AI Service Unavailable", response.getBody().get("error"));
    }

    @Test
    void testHandleRuntimeException_NotFound() {
        RuntimeException ex = new RuntimeException("Job not found");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request Error", response.getBody().get("error"));
        assertEquals("Job not found", response.getBody().get("message"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected internal error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertNull(response.getBody().get("stackTrace"));
    }
}
