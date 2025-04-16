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
    if (value == null) return true;
    boolean isValid = true;
    context.disableDefaultConstraintViolation(); // Ngăn thông báo mặc định

    for (int i = 0; i < value.length; i++) {
      String[] row = value[i];
      for (int j = 0; j < row.length; j++) {
        String requirement = row[j];
        if (!VALID_PATTERN.matcher(requirement).matches()) {
          String errorMessage = String.format("%s tại hàng %d, cột %d: '%s'", message, i, j, requirement);

          // Gắn lỗi vào đúng trường
          context.buildConstraintViolationWithTemplate(errorMessage)
                  .addPropertyNode("individualRequirements")
                  .addBeanNode()
                  .inIterable().atIndex(i) // chỉ ra hàng
                  .addConstraintViolation();

          isValid = false;
        }
      }
    }

    return isValid;
  }

}