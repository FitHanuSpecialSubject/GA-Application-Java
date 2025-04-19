package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * **FitnessFunctionValidator** - Validator for fitness function syntax.
 * This class ensures that the provided fitness function follows **correct mathematical syntax**
 * and only includes **allowed variables**. It supports:
 * - **M{number}** → Represents matching-related variables.
 * - **S{number}** → Represents satisfaction-related variables.
 * - **SIGMA{expression}** → Represents a summation expression inside `{}`.
 */
public class FitnessFunctionValidator implements ConstraintValidator<ValidFitnessFunctionSMT, StableMatchingProblemDto> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(M\\d+|S\\d+|SIGMA\\{[^}]+\\})");
  private static final Pattern OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]{2,}");

  /**
   * Validates the fitness function by checking its syntax and allowed variables.
   *
   * @param dto   The fitness function string to validate.
   * @param context The validation context for constraint violations.
   * @return `true` if the function is valid, otherwise `false`.
   */
  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    String value = dto.getFitnessFunction();
    boolean isValid = true;
    if (value.equalsIgnoreCase(StableMatchingConst.DEFAULT_EVALUATE_FUNC)) {
      return true;
    }

    if (!checkSigmaExpressions(context, value)) {
      isValid = false;
    }

    String modifiedExpression = replaceSigma(value);

    if (!validateExp4j(modifiedExpression)) {
      collectAllErrors(context, modifiedExpression);
      isValid = false;
    }

    Set<String> variables = extractVariablesWithoutSigma(value);
    if (!validateVariableLimits(context, variables, dto)) {
      isValid = false;
    }

    return isValid;
  }

  private String replaceSigma(String expression) {
    return expression.replaceAll("SIGMA\\{[^}]*\\}", "1.0");
  }
  private boolean validateExp4j(String expression) {
    try {
      Set<String> variables = extractVariables(expression);
      ExpressionBuilder builder = new ExpressionBuilder(expression);

      for (String var : variables) {
        builder.variable(var);
        if (!var.startsWith("SIGMA{")) {
          builder.variable(var);
        }
      }

      Expression exp = builder.build();
      if (!exp.validate(false).isValid()) {
        return false;
      }

      for (String var : variables) {
        if (!var.startsWith("SIGMA{")) {
          exp.setVariable(var, 1.0);
        }
      }
      exp.evaluate();

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private void collectAllErrors(ConstraintValidatorContext context, String expression) {
    checkBrackets(context, expression);
    checkConsecutiveOperators(context, expression);
    checkDivisionByZero(context, expression);
  }

  private boolean checkSigmaExpressions(ConstraintValidatorContext context, String expression) {
    boolean isValid = true;
    try {
      Set<String> variables = extractVariables(expression);

      for (String var : variables) {
        if (var.startsWith("SIGMA{") && var.endsWith("}")) {
          String innerExpression = var.substring(6, var.length() - 1);

          if (!validateInnerSigmaExpression(innerExpression)) {
            addViolation(context, "expression",
                    "Invalid expression inside SIGMA: " + innerExpression);
            isValid = false;
          }
        }
      }
    } catch (Exception e) {
      addViolation(context, "expression",
              "Error in SIGMA expression: " + e.getMessage());
      isValid = false;
    }
    return isValid ;
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

  private boolean validateVariableLimits(ConstraintValidatorContext context, Set<String> variables, StableMatchingProblemDto dto) {
    boolean isValid = true;
    for (String var : variables) {
      if (var.startsWith("S") && !var.startsWith("SIGMA")) {
        try {
          int index = Integer.parseInt(var.substring(1));
          if (index > dto.getNumberOfSets() || index < 1) {
            addViolation(
                    context,
                    "fitnessFunction",
                    "Invalid S index: " + index + ". Must be between 1 and " + dto.getNumberOfSets()
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          addViolation(context, "fitnessFunction", "Invalid S variable format: " + var);
          isValid = false;
        }
      } else if (var.startsWith("M")) {
        try {
          int index = Integer.parseInt(var.substring(1));
          int maxM = dto.getIndividualSetIndices().length;
          if (index > maxM || index < 1) {
            addViolation(
                    context,
                    "fitnessFunction",
                    "Invalid M index: " + index + ". Must be between 1 and " + maxM
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          addViolation(context, "fitnessFunction", "Invalid M variable format: " + var);
          isValid = false;
        }
      }
    }
    return isValid;
  }

  private void checkDivisionByZero(ConstraintValidatorContext context, String expression) {
    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (c == '/') {
        String denominator = extractDenominator(expression, i + 1);
        if (denominator.matches("0(\\.0+)?")) {
          addViolation(context, "fitnessFunction", "Division by zero at position " + (i + 1));
        }
      }
    }
  }

  private String extractDenominator(String expression, int startPos) {
    StringBuilder denominator = new StringBuilder();
    for (int i = startPos; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (Character.isWhitespace(c)) {
        continue;
      }
      denominator.append(c);
    }
    return denominator.toString().trim();
  }

  private void checkBrackets(ConstraintValidatorContext context, String expression) {
    Stack<Integer> stack = new Stack<>();

    for (int i = 0; i < expression.length(); i++) {
      char current = expression.charAt(i);
      if (current == '(') {
        stack.push(i);
      } else if (current == ')') {
        if (stack.isEmpty()) {
          addViolation(context, "fitnessFunction", "Unmatched closing bracket at position " + i);
        } else {
          stack.pop();
        }
      }
    }

    while (!stack.isEmpty()) {
      int pos = stack.pop();
      addViolation(context, "fitnessFunction", "Unmatched opening bracket at position " + pos);
    }
  }

  private void checkConsecutiveOperators(ConstraintValidatorContext context, String expression) {
    Matcher matcher = OPERATOR_PATTERN.matcher(expression);
    while (matcher.find()) {
      int pos = matcher.start();
      addViolation(context, "fitnessFunction", "Consecutive operators at position " + pos);
    }
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

  private Set<String> extractVariablesWithoutSigma(String func) {
    Set<String> variables = new HashSet<>();
    Matcher matcher = VARIABLE_PATTERN.matcher(func);

    while (matcher.find()) {
      String var = matcher.group(0);
      if (var.startsWith("SIGMA")) {
        // Lấy nội dung bên trong {}
        String content = var.substring(6, var.length() - 1);
        variables.add(content);
      } else {
        variables.add(var);
      }
    }

    return variables;
  }

  private void addViolation(ConstraintValidatorContext context, String field, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message)
            .addPropertyNode(field)
            .addConstraintViolation();
  }

}