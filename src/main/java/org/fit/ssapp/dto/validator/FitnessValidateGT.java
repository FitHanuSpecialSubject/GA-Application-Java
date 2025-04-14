package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.constants.GameTheoryConst;

/**
 * **FitnessFunctionValidator** - Validator for fitness function syntax.
 * This class ensures that the provided fitness function follows **correct mathematical syntax**
 * and only includes **allowed variables**. It supports:
 * - **u{number}** → Represents utility variables for players in game theory.
 * - **M{number}** → Represents matching-related variables for stable matching.
 * - **S{number}** → Represents satisfaction-related variables for stable matching.
 * - **SIGMA{expression}** → Represents a summation expression inside `{}`.
 * - Various math functions like abs(), sqrt(), log(), ceil(), etc.
 */
public class FitnessValidateGT implements ConstraintValidator<ValidFitnessFunction, String> {

  // Updated pattern to match u1, u2, M1, M2, S1, S2, etc.
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(u\\d+|M\\d+|S\\d+)");

  // Pattern to match SIGMA expressions
  private static final Pattern SIGMA_PATTERN = Pattern.compile("SIGMA\\{([^}]+)\\}");

  // Pattern to match standard mathematical functions
  private static final Pattern FUNCTION_PATTERN = Pattern.compile("(abs|sqrt|log|exp|ceil|floor|sin|cos|tan|pow)\\(");

  private static final TreeSet<String> VALID_FUNCTIONS = new TreeSet<>(Set.of("SUM","AVERAGE","MIN","MAX","PRODUCT","MEDIAN","RANGE"));

  /**
   * Initializes the validator with annotation information.
   *
   * @param constraintAnnotation The annotation instance for this validator.
   */
  @Override
  public void initialize(ValidFitnessFunction constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return false;
    }

    if (value.equalsIgnoreCase(GameTheoryConst.DEFAULT_PAYOFF_FUNC)) {
      return true;
    }

    // Check if it's a default function
    if (VALID_FUNCTIONS.contains(value.toUpperCase().trim())) {
      return true;
    }

    String cleanFunc = value.replaceAll("\\s+", "");
    try {
      Set<String> variables = extractVariables(cleanFunc);
      Set<String> functions = extractFunctions(cleanFunc);
      Set<String> sigmaExpressions = extractSigmaExpressions(cleanFunc);

      if (variables.isEmpty() && functions.isEmpty() && sigmaExpressions.isEmpty()) {
        // For expressions like 4 + 2, no variables but still valid
        try {
          ExpressionBuilder builder = new ExpressionBuilder(cleanFunc);
          Expression expression = builder.build();
          expression.evaluate();
          return true;
        } catch (Exception ex) {
          context.disableDefaultConstraintViolation();
          context.buildConstraintViolationWithTemplate(
                  "Invalid fitness function syntax: '" + value + "'")
              .addConstraintViolation();
          return false;
        }
      }

      // Handle SIGMA expressions by replacing them with placeholder variables
      String evaluableExpression = cleanFunc;
      int placeholderCounter = 0;
      for (String sigma : sigmaExpressions) {
        // Replace each SIGMA expression with a unique placeholder
        String placeholder = "sigma" + placeholderCounter++;
        evaluableExpression = evaluableExpression.replace(sigma, placeholder);
        variables.add(placeholder);
      }

      ExpressionBuilder builder = new ExpressionBuilder(evaluableExpression);

      // Add all extracted variables to the builder
      for (String var : variables) {
        builder.variable(var);
      }

      Expression expression = builder.build();

      // Set dummy values for all variables
      for (String var : variables) {
        expression.setVariable(var, 1.0);
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
   * - Uses **regex matching** to identify valid variables (u1, u2, M1, M2, S1, S2, etc.).
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

  /**
   * Extracts SIGMA expressions from a mathematical function.
   *
   * @param func The mathematical function as a string.
   * @return A set of SIGMA expressions found in the function.
   */
  private Set<String> extractSigmaExpressions(String func) {
    Set<String> expressions = new HashSet<>();
    Matcher matcher = SIGMA_PATTERN.matcher(func);
    while (matcher.find()) {
      expressions.add(matcher.group(0));
    }
    return expressions;
  }

  /**
   * Extracts mathematical function names from an expression.
   *
   * @param func The mathematical function as a string.
   * @return A set of function names found in the expression.
   */
  private Set<String> extractFunctions(String func) {
    Set<String> functions = new HashSet<>();
    Matcher matcher = FUNCTION_PATTERN.matcher(func);
    while (matcher.find()) {
      // Remove the trailing parenthesis
      String function = matcher.group(0);
      function = function.substring(0, function.length() - 1);
      functions.add(function);
    }
    return functions;
  }
}