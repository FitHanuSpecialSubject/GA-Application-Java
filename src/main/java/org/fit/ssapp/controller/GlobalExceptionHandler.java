package org.fit.ssapp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  Logger logger = Logger.getLogger("GlobalExceptionHandler");

  @ExceptionHandler(RejectedExecutionException.class)
  public ResponseEntity<String> handleRejectedExecutionException(RejectedExecutionException ex) {
    logger.warning("Queue full!");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body("Server is busy. Please try again later.");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    logger.warning("Invalid request body!");
    Map<String, Object> errorMap = new HashMap<>();
    errorMap.put("errors", ex.getBindingResult().getAllErrors()
        .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.toList()));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
  }

}
