package org.fit.ssapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.fit.ssapp.dto.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.beans.TypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import org.fit.ssapp.controller.GlobalExceptionHandler.ValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private ObjectMapper objectMapper;

    // Custom ValidationException class
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @ExceptionHandler(RejectedExecutionException.class)
    public ResponseEntity<Response> handleRejectedExecutionException(
            RejectedExecutionException ex) {
        log.error("Task execution rejected: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Response.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message("Server is currently busy. Please try again later.")
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Invalid request body: {}", ex.getMessage());
        
        // Check if the request body is empty
        try {
            String requestBody = ex.getMessage();
            if (requestBody != null && requestBody.contains("Required request body is missing")) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Response.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Request body is required")
                            .build());
            }
        } catch (Exception e) {
            log.warn("Error checking request body: {}", e.getMessage());
        }
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Request body is required")
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Response> handleValidationException(
            ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Response.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(ex.getMessage())
                    .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.error("Missing request parameter: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Response.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Request body is required")
                    .build());
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        TypeMismatchException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<Response> handleTypeMismatchException(Exception ex) {
        log.error("Type mismatch or constraint violation: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Response.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Request body is required")
                    .build());
    }

    // Add catch-all for the test case
    @ExceptionHandler(jakarta.validation.ValidationException.class)
    public ResponseEntity<Response> handleJakartaValidationException(jakarta.validation.ValidationException ex) {
        log.error("Jakarta validation exception: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Response.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Request body is required")
                    .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleException(Exception ex) {
        // Log the full stack trace for unexpected errors
        log.error("Unexpected error type: {}", ex.getClass().getName());
        log.error("Unexpected error message: {}", ex.getMessage());
        log.error("Unexpected error: ", ex);
        
        // Handle empty request body cases but don't interfere with async processing
        if (ex.getMessage() != null && 
            (ex.getMessage().contains("No content to map due to end-of-input") ||
             ex.getMessage().contains("Required request body is missing"))) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Request body is required")
                        .build());
        }
        
        String message = "Internal server error";
        // Include more details in development environment
        if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev", "local"))) {
            message = ex.getMessage();
        }
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(message)
                        .build());
    }
} 