package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * **RequirementSyntaxValidator** - Validator for checking requirement syntax.
 * This class ensures that all **requirement expressions** follow the correct format.
 */
public class RequirementSyntaxValidator implements
        ConstraintValidator<ValidRequirementSyntax, String[][]> {

  private static final Pattern VALID_PATTERN = Pattern.compile(
          "^(\\d+(?:\\.\\d+)?)(?::(\\d+(?:\\.\\d+)?))?(?:\\+\\+|--)?$");
  private String message;

  /**
   * Initializes the validator.
   *
   * @param annotation The annotation instance for additional configurations (e.g., error message).
   */
  @Override
  public void initialize(ValidRequirementSyntax annotation) {
    this.message = annotation.message();
  }

  /**
   * Validates a **2D array of requirement expressions**.
   * - Iterates through each row and requirement expression.
   * - Applies **regex matching** to check syntax validity.
   * - If an invalid expression is found, a **constraint violation message** is generated.
   *
   * @param value   The `String[][]` array containing requirement expressions.
   * @param context The validation context for reporting violations.
   * @return `true` if all expressions match the expected format, otherwise `false`.
   */
  @Override
  public boolean isValid(String[][] value, ConstraintValidatorContext context) {
    for (String[] row : value) {
      for (String requirement : row) {
        if (!VALID_PATTERN.matcher(requirement).matches()) {
          context.disableDefaultConstraintViolation();
          context.buildConstraintViolationWithTemplate(message + ": '" + requirement + "'")
                  .addConstraintViolation();
          return false;
        }
      }
    }
    return true;
  }
}