package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * RequirementSyntaxValidator.
 */
public class RequirementSyntaxValidator implements
        ConstraintValidator<ValidRequirementSyntax, String[][]> {

  private static final Pattern VALID_PATTERN = Pattern.compile(
          "^(\\d+(?:\\.\\d+)?)(?::(\\d+(?:\\.\\d+)?))?(?:\\+\\+|--)?$");
  private String message;

  @Override
  public void initialize(ValidRequirementSyntax annotation) {
    this.message = annotation.message();
  }

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