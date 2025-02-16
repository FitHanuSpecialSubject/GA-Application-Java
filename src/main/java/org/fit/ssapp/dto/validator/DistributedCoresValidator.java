package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * **DistributedCoresValidator** - Validator for distributed core allocation.
 * This class validates whether the specified number of CPU cores is valid for
 * distributed computing. It ensures that:
 * - The value is `"all"`, meaning all available cores can be used.
 * - The value is a **positive integer** that does not exceed the system's available cores.
 */
public class DistributedCoresValidator implements
        ConstraintValidator<ValidDistributedCores, String> {

  /**
   * Initializes the validator.
   *
   * @param annotation The annotation instance for additional configurations (if needed).
   */
  @Override
  public void initialize(ValidDistributedCores annotation) {
  }

  /**
   * Validates the given core allocation value.
   *
   * @param value   The number of cores requested (as a String).
   * @param context The validation context.
   * @return `true` if the value is `"all"` or a valid number within the system's available cores.
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
