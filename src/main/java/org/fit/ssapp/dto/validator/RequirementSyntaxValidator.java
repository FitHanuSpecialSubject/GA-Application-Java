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

  private String message;

  private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
  private static final Pattern RANGE_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?:\\d+(\\.\\d+)?$");
  private static final Pattern SUFFIX_PATTERN = Pattern.compile(".*(\\+\\+|--)$");
  private static final Pattern FULL_PATTERN = Pattern.compile("^(\\d+(\\.\\d+)?)(?::(\\d+(\\.\\d+)?))?(\\+\\+|--)?$");

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
    context.disableDefaultConstraintViolation();

    for (int i = 0; i < value.length; i++) {
      String[] row = value[i];
      for (int j = 0; j < row.length; j++) {
        String requirement = row[j];

        if (requirement == null || requirement.trim().isEmpty()) {
          buildViolation(context, "Requirement cannot be empty", i, j, requirement);
          isValid = false;
          continue;
        }

        // Full pattern check (valid case)
        if (FULL_PATTERN.matcher(requirement).matches()) {
          continue;
        }

        // Invalid: Check components and give detailed feedback
        if (requirement.contains(":")) {
          String withoutSuffix = requirement.replaceAll("(\\+\\+|--)$", "");
          if (RANGE_PATTERN.matcher(withoutSuffix).matches()) {
            String[] parts = withoutSuffix.split(":");
            try {
              double left = Double.parseDouble(parts[0]);
              double right = Double.parseDouble(parts[1]);

              if (left > right) {
                buildViolation(context, "Invalid range logic: left bound is greater than right bound", i, j, requirement);
                isValid = false;
                continue;
              }

            } catch (NumberFormatException e) {
              buildViolation(context, "Range values must be numeric", i, j, requirement);
              isValid = false;
              continue;
            }
          } else {
            buildViolation(context, "Invalid range format", i, j, requirement);
            isValid = false;
            continue;
          }
        }


        if (!NUMBER_PATTERN.matcher(requirement.split("[:\\+\\-]")[0]).matches()) {
          buildViolation(context, "Requirement must start with a valid number", i, j, requirement);
          isValid = false;
          continue;
        }

        if (SUFFIX_PATTERN.matcher(requirement).find() && !requirement.endsWith("++") && !requirement.endsWith("--")) {
          buildViolation(context, "Invalid suffix format. Only '++' or '--' allowed", i, j, requirement);
          isValid = false;
          continue;
        }

        // Fallback error
        buildViolation(context, message + " - Invalid syntax", i, j, requirement);
        isValid = false;
      }
    }

    return isValid;
  }

  private void buildViolation(ConstraintValidatorContext context, String reason, int row, int col, String value) {
    String formatted = String.format("%s at row %d, column %d: '%s'", reason, row, col, value);
    context.buildConstraintViolationWithTemplate(formatted)
            .addPropertyNode("individualRequirements")
            .addBeanNode()
            .inIterable().atIndex(row)
            .addConstraintViolation();
  }
}
