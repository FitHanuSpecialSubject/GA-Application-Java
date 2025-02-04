package org.fit.ssapp.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class ValidationUtils {

  static MessageSource messageSource;

  private ValidationUtils() {
  }

  /**
   * Validate DTO request Object.
   *
   * @param target DTO object
   * @return bindingResult
   */

  public static BindingResult validate(Object target) {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = factory.getValidator();
      BindingResult bindingResult = new BeanPropertyBindingResult(target, "");
      SpringValidatorAdapter springValidator = new SpringValidatorAdapter(validator);
      springValidator.validate(target, bindingResult);
      return bindingResult;
    }
  }


  /**
   * Get all errors out of bindingResult as Map.
   *
   * @param bindingResult Validate result.
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
   * Get message by key and params, currently not using, update later.
   *
   * @param defaultMessage String
   * @param params         Object...
   * @return String
   */
  @SuppressWarnings("unused")
  public static String getMessage(String defaultMessage, String... params) {
    try {
      return messageSource.getMessage(defaultMessage, params, Locale.ENGLISH);
    } catch (NoSuchMessageException e) {
      return defaultMessage;
    }
  }

}
