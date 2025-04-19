package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.constants.StableMatchingConst;

/**
 * **FitnessFunctionValidator** - Validator for fitness function syntax.
 * This class ensures that the provided fitness function follows **correct mathematical syntax**
 * and only includes **allowed variables**. It supports:
 * - **M{number}** → Represents matching-related variables.
 * - **S{number}** → Represents satisfaction-related variables.
 * - **SIGMA{expression}** → Represents a summation expression inside `{}`.
 */
public class FitnessFunctionValidator implements ConstraintValidator<ValidFitnessFunction, String> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(M\\d+|S\\d+|SIGMA\\{[^}]+\\})");
  private static final Pattern OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]{2,}");
  private static class ValidationError {
    private final String message;
    private final int position;

    public ValidationError(String message, int position) {
      this.message = message;
      this.position = position;
    }
  }

  /**
   * Validates the fitness function by checking its syntax and allowed variables.
   *
   * @param value   The fitness function string to validate.
   * @param context The validation context for constraint violations.
   * @return `true` if the function is valid, otherwise `false`.
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value.equalsIgnoreCase(StableMatchingConst.DEFAULT_EVALUATE_FUNC)) {
      return true;
    }

    if (validateExp4j(value)) {
      return true;
    }

    List<ValidationError> errors = collectAllErrors(value);

    if (!errors.isEmpty()) {
      context.disableDefaultConstraintViolation();

      for (ValidationError error : errors) {
        String message = formatErrorMessage(error, value);
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
      }
      return false;
    }

    return true;
  }

  private boolean validateExp4j(String expression) {
    try {
      Set<String> variables = extractVariables(expression);
      ExpressionBuilder builder = new ExpressionBuilder(expression);

      for (String var : variables) {
        builder.variable(var);
      }

      Expression exp = builder.build();
      if (!exp.validate(false).isValid()) {
        return false;
      }

      for (String var : variables) {
        exp.setVariable(var, 1.0);
      }
      exp.evaluate();

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private List<ValidationError> collectAllErrors(String expression) {
    List<ValidationError> errors = new ArrayList<>();

    ValidationError bracketError = checkBrackets(expression);
    if (bracketError != null) {
      errors.add(bracketError);
    }

    ValidationError operatorError = checkConsecutiveOperators(expression);
    if (operatorError != null) {
      errors.add(operatorError);
    }

    ValidationError sisgmaError = checkSigmaExpressions(expression);
    if (operatorError != null) {
      errors.add(sisgmaError);
    }

    return errors;
  }

  private ValidationError checkSigmaExpressions(String expression) {
    try {
      Set<String> variables = extractVariables(expression);

      // Kiểm tra từng biểu thức SIGMA
      for (String var : variables) {
        if (var.startsWith("SIGMA{") && var.endsWith("}")) {
          String innerExpression = var.substring(6, var.length() - 1);

          // Kiểm tra biểu thức bên trong SIGMA có hợp lệ không
          if (!validateInnerSigmaExpression(innerExpression)) {
            return new ValidationError("Invalid expression inside SIGMA: " + innerExpression,
                    expression.indexOf(var) + 6);
          }
        }
      }

      return null;
    } catch (Exception e) {
      return new ValidationError("Error in SIGMA expression: " + e.getMessage(), 0);
    }
  }

  private boolean validateInnerSigmaExpression(String innerExpression) {
    try {
      Set<String> innerVars = extractVariables(innerExpression);

      ExpressionBuilder builder = new ExpressionBuilder(innerExpression);
      for (String var : innerVars) {
        builder.variable(var);
      }

      Expression exp = builder.build();

      for (String var : innerVars) {
        exp.setVariable(var, 1.0);
      }

      exp.evaluate();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private ValidationError checkBrackets(String expression) {
    Stack<Integer> stack = new Stack<>();

    for (int i = 0; i < expression.length(); i++) {
      char current = expression.charAt(i);
      if (current == '(') {
        stack.push(i);
      } else if (current == ')') {
        if (stack.isEmpty()) {
          return new ValidationError("Unmatched closing bracket", i);
        }
        stack.pop();
      }
    }

    if (!stack.isEmpty()) {
      return new ValidationError("Unmatched opening bracket", stack.peek());
    }

    return null;
  }

  private ValidationError checkConsecutiveOperators(String expression) {
    Matcher matcher = OPERATOR_PATTERN.matcher(expression);
    if (matcher.find()) {
      return new ValidationError(
              "Invalid consecutive operators: " + matcher.group(),
              matcher.start()
      );
    }
    return null;
  }

  /**
   * Extracts valid variable names from a mathematical function.
   * - Uses **regex matching** to identify valid variables (`M#`, `S#`, `SIGMA{}` expressions).
   * - Returns a **set of unique variable names** found in the function.
   *
   * @param func The mathematical function as a string.
   * @return A set of valid variable names found in the function.
   */
  private Set<String> extractVariables(String func) {
    Set<String> variables = new HashSet<>();
    Matcher matcher = VARIABLE_PATTERN.matcher(func);
    while (matcher.find()) {
      variables.add(matcher.group(0));
    }
    return variables;
  }

  private String formatErrorMessage(ValidationError error, String expression) {
    return String.format("%s at position %d in function: %s",
            error.message,
            error.position,
            expression);
  }
}