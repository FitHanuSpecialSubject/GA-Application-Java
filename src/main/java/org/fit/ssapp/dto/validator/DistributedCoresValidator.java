package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator class for checking if the provided value for distributed cores is valid. The value must
 * either be "all" or a positive integer that does not exceed the available CPU cores.
 */
public class DistributedCoresValidator implements
    ConstraintValidator<ValidDistributedCores, String> {

  /**
   * @param annotation annotation instance for a given constraint declaration
   */
  @Override
  public void initialize(ValidDistributedCores annotation) {
  }

  /**
   * Validates whether the provided value is a valid representation of distributed cores. The value
   * is valid if it is "all" or a positive integer that does not exceed the available CPU cores.
   *
   * @param value   the value to validate
   * @param context the context in which the constraint is evaluated
   * @return true if the value is valid, false otherwise
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    int availableCores = Runtime.getRuntime().availableProcessors();

    if (value.equalsIgnoreCase("all")) {
      return true;
    }

    try {
      int cores = Integer.parseInt(value);
      return cores > 0 && cores <= availableCores;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
