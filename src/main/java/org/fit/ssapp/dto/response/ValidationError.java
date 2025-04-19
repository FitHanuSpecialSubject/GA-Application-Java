package org.fit.ssapp.dto.response;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class ValidationError {
  public final Date timestamp = new Date();
  public final int status = HttpStatus.BAD_REQUEST.value();
  public final String message = "Validation failed";
  public final List<CustomFieldError> details;

  public ValidationError(BindingResult bindingResult) {
    this.details = bindingResult.getFieldErrors()
        .stream()
        .map(CustomFieldError::new)
        .toList();
  }
}

class CustomFieldError {
  public final String field;
  public final String message;
  public final Object value;

  public CustomFieldError(FieldError error) {
    this.field = error.getField();
    this.value = error.getRejectedValue();
    this.message = error.getDefaultMessage();
  }
}
