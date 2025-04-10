package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;
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
 * - Simple function names: SUM, AVERAGE, MIN, MAX, PRODUCT, MEDIAN, RANGE
 */
public class FitnessFunctionValidator implements ConstraintValidator<ValidFitnessFunction, String> {

  // Split into multiple patterns for easier debugging
  private static final Pattern USER_VAR_PATTERN = Pattern.compile("u[1-9]\\d*");
  private static final Pattern M_VAR_PATTERN = Pattern.compile("M\\d+");
  private static final Pattern S_VAR_PATTERN = Pattern.compile("S\\d+");
  
  private static final Set<String> VALID_FUNCTIONS = Set.of("SUM", "AVERAGE", "MIN", "MAX", "PRODUCT", "MEDIAN", "RANGE");

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

    // Check if it's a simple function name
    if (VALID_FUNCTIONS.contains(value.toUpperCase())) {
      return true;
    }

    String cleanFunc = value.replaceAll("\\s+", "");
    try {
      Set<String> variables = extractVariables(cleanFunc);

      // Validate fitness variables
      for (String var : variables) {
        if (var.startsWith("u")) {
          int index = Integer.parseInt(var.substring(1));
          if (index <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid fitness variable index: " + var)
                    .addConstraintViolation();
            return false;
          }
        }
      }

      for (String var : variables) {
        if (var.startsWith("SIGMA{") && var.endsWith("}")) {
          cleanFunc = cleanFunc.replace(var, var.substring(6, var.length() - 1));
        }
      }

      ExpressionBuilder builder = new ExpressionBuilder(cleanFunc);
      for (String var : variables) {
        String cleanVar = var.startsWith("SIGMA{") && var.endsWith("}")
                ? var.substring(6, var.length() - 1)
                : var;
        builder.variable(cleanVar);
      }

      Expression expression = builder.build();

      for (String var : variables) {
        String cleanVar = var.startsWith("SIGMA{") && var.endsWith("}")
                ? var.substring(6, var.length() - 1)
                : var;
        expression.setVariable(cleanVar, 1.0);
      }

      expression.evaluate();
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
                      "Invalid fitness function syntax: '" + value + "'")
              .addConstraintViolation();
      return false;
    }
    return true;
  }

  /**
   * Extracts valid variable names from a mathematical function.
   * - Uses **multiple regex patterns** to identify valid variables (`M#`, `S#`, `SIGMA{}` expressions).
   * - Returns a **set of unique variable names** found in the function.
   *
   * @param func The mathematical function as a string.
   * @return A set of valid variable names found in the function.
   */
  private Set<String> extractVariables(String func) {
    Set<String> variables = new HashSet<>();
    
    // Check each pattern separately for better debugging
    addMatchesToSet(USER_VAR_PATTERN.matcher(func), variables);
    addMatchesToSet(M_VAR_PATTERN.matcher(func), variables);
    addMatchesToSet(S_VAR_PATTERN.matcher(func), variables);
    
    return variables;
  }
  
  /**
   * Helper method to add all matches from a matcher to a set.
   *
   * @param matcher The regex matcher
   * @param variables The set to add matches to
   */
  private void addMatchesToSet(Matcher matcher, Set<String> variables) {
    while (matcher.find()) {
      variables.add(matcher.group(0));
    }
  }
}