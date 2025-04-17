package org.fit.ssapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.fit.ssapp.exception.ZeroWeightSum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = Logger.getLogger("GlobalExceptionHandler");

  /**
   * TODO: Chuyển sang package org/fit/ssapp/exception
   */
  public static class ValidationException extends RuntimeException {
    public ValidationException(String message) {
      super(message);
    }
  }

  /**
   * Server busy exception handler.
   *
   * @param ex RejectedExecutionException
   * @return ResponseEntity with error message
   */
  @ExceptionHandler(RejectedExecutionException.class)
  public ResponseEntity<Map<String, String>> handleRejectedExecutionException(RejectedExecutionException ex) {
    logger.warning("Queue full!");
    Map<String, String> error = new HashMap<>();
    error.put("error", "Server is busy. Please try again later.");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
  }

  /**
   * Validation exception handler.
   *
   * @param ex MethodArgumentNotValidException
   * @return ResponseEntity with validation errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    logger.warning("Invalid request body!");
    Map<String, Object> errors = new HashMap<>();
    errors.put("errors", ex.getBindingResult().getAllErrors()
        .stream()
        .map(error -> {
          if (error instanceof FieldError) {
            return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
          }
          return error.getDefaultMessage();
        })
        .collect(Collectors.toList()));
    return ResponseEntity.badRequest().body(errors);
  }

  /**
   * HTTP message not readable exception handler.
   *
   * @param ex HttpMessageNotReadableException
   * @return ResponseEntity with error message
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    logger.warning("Invalid request body format!");
    Map<String, String> error = new HashMap<>();
    error.put("error", "Invalid request body format");
    return ResponseEntity.badRequest().body(error);
  }

  /**
   * General exception handler.
   *
   * @param ex Exception
   * @return ResponseEntity with error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
    logger.severe("Unexpected error: " + ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  /**
   * Zero-sum exception handler.
   * - Throw when normalizing weights of MatchingData (module SMT)
   *
   * @param ex Exception
   * @return ResponseEntity with error message
   */
  @ExceptionHandler(ZeroWeightSum.class)
  public ResponseEntity<Map<String, String>> handleZeroWeightSum(Exception ex) {
    logger.warning("Message: " + ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}