package org.fit.ssapp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.fit.ssapp.util.ValidationUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controller layer exception handler.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  final Logger logger = Logger.getLogger("GlobalExceptionHandler");

  /**
   * Server busy exception handler.
   *
   * @param ex RejectedExecutionException
   * @return ResponseEntity
   */
  @ExceptionHandler(RejectedExecutionException.class)
  public ResponseEntity<String> handleRejectedExecutionException(RejectedExecutionException ex) {
    logger.warning("Queue full!");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Server is busy. Please try again later.");
  }

  /**
   * MethodArgumentNotValidException handler.
   *
   * @param ex MethodArgumentNotValidException
   * @return ResponseEntity
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
          MethodArgumentNotValidException ex) {
    logger.warning("Invalid request body!");
    Map<String, List<String>> errorsMap =  ValidationUtils
        .getAllErrorDetails(ex.getBindingResult());
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("errors", errorsMap);

    return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
  }

}
