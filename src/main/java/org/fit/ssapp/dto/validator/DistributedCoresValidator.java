package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * DistributedCoresValidator.
 */
public class DistributedCoresValidator implements
        ConstraintValidator<ValidDistributedCores, String> {

  /**
   * ValidDistributedCores initialize.
   */
  @Override
  public void initialize(ValidDistributedCores annotation) {
  }

  /**
   * isValid .
   *
   * @return boolean.
   *
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
