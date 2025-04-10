package org.fit.ssapp.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.fit.ssapp.controller.GlobalExceptionHandler.ValidationException;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * Utility class for validation operations, including validating DTO objects, extracting error
 * details from binding results, and retrieving messages by key.
 */
public class ValidationUtils {

  static MessageSource messageSource;

  private ValidationUtils() {
  }

  /**
   * Validate DTO request Object.
   *
   * @param target DTO object
   * @return bindingResult
   * @throws ValidationException if validation fails
   */
  public static BindingResult validate(Object target) {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = factory.getValidator();
      BindingResult bindingResult = new BeanPropertyBindingResult(target, "");
      SpringValidatorAdapter springValidator = new SpringValidatorAdapter(validator);
      springValidator.validate(target, bindingResult);

      if (bindingResult.hasErrors()) {
        Map<String, List<String>> errors = getAllErrorDetails(bindingResult);
        throw new ValidationException("Validation failed: " + errors);
      }

      return bindingResult;
    }
  }


  /**
   * Extracts all errors from the given BindingResult and returns them as a Map. The map's keys are
   * the field names and the values are lists of error messages.
   *
   * @param bindingResult the BindingResult containing validation errors
   * @return a Map with field names as keys and lists of error messages as values
   */
  public static Map<String, List<String>> getAllErrorDetails(BindingResult bindingResult) {
    List<ObjectError> listObjectError = bindingResult.getAllErrors();
    if (CollectionUtils.isEmpty(listObjectError)) {
      return new HashMap<>();
    }
    HashMap<String, List<String>> errMap = new HashMap<>();
    for (ObjectError objectError : listObjectError) {
      String fieldErrKey = ((FieldError) objectError).getField();
      String defaultMsg = objectError.getDefaultMessage();
      errMap.computeIfAbsent(fieldErrKey, k -> new ArrayList<>()).add(defaultMsg);
    }
    return errMap;
  }


  /**
   * Retrieves a message by its key and optional parameters. Currently, this method uses a
   * MessageSource to fetch the message but may be updated later for additional functionality.
   *
   * @param defaultMessage the key of the message to retrieve
   * @param params         the parameters to be included in the message (optional)
   * @return the message corresponding to the key, or the default message if no such message is
   *     found
   */
  public static String getMessage(String defaultMessage, String... params) {
    try {
      return messageSource.getMessage(defaultMessage, params, Locale.ENGLISH);
    } catch (NoSuchMessageException e) {
      return defaultMessage;
    }
  }

}
