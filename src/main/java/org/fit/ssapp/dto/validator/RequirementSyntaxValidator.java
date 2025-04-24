package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * RequirementSyntaxValidator - Validator for checking requirement syntax.
 * This class ensures that all requirement expressions follow the correct format.
 */
public class RequirementSyntaxValidator implements
        ConstraintValidator<ValidRequirementSyntax, String[][]> {

  private String message;

  private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
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
   * Validates a 2D array of requirement expressions.
   * - Iterates through each row and requirement expression.
   * - Applies regex matching to check syntax validity.
   * - If an invalid expression is found, a constraint violation message is generated.
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
      if (row == null) {
        buildViolation(context, "Requirement row cannot be null", i, -1, "null");
        isValid = false;
        continue;
      }
      for (int j = 0; j < row.length; j++) {
        String requirement = row[j];
        boolean hasSpecificError = false;

        if (requirement == null || requirement.trim().isEmpty()) {
          buildViolation(context, "Requirement cannot be empty", i, j, requirement);
          isValid = false;
          hasSpecificError = true;
          continue;
        }

        // Check if the requirement matches the full valid pattern
        if (!FULL_PATTERN.matcher(requirement).matches()) {
          // Check for non-numeric start
          String[] parts = requirement.split("[:\\+\\-]");
          if (parts.length == 0 || parts[0].isEmpty() || !NUMBER_PATTERN.matcher(parts[0]).matches()) {
            buildViolation(context, "Requirement must start with a valid number", i, j, requirement);
            isValid = false;
            hasSpecificError = true;
          }

          // Check for invalid suffix
          if (SUFFIX_PATTERN.matcher(requirement).matches() && !requirement.endsWith("++") && !requirement.endsWith("--")) {
            buildViolation(context, "Invalid suffix format. Only '++' or '--' allowed", i, j, requirement);
            isValid = false;
            hasSpecificError = true;
          }

          // Fallback error if no specific error was found
          if (!hasSpecificError) {
            buildViolation(context, message + " - Invalid syntax", i, j, requirement);
            isValid = false;
          }
          continue;
        }

        // Additional range validation for left > right
        if (requirement.contains(":")) {
          String[] parts = requirement.split(":");
          try {
            double left = Double.parseDouble(parts[0].replaceAll("(\\+\\+|--)$", ""));
            double right = Double.parseDouble(parts[1].replaceAll("(\\+\\+|--)$", ""));
            if (left > right) {
              buildViolation(context, "Invalid range logic: left bound is greater than right bound", i, j, requirement);
              isValid = false;
              hasSpecificError = true;
            }
          } catch (NumberFormatException e) {
            buildViolation(context, "Range values must be numeric", i, j, requirement);
            isValid = false;
            hasSpecificError = true;
          }
        }
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