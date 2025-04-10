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
 */
public class FitnessValidateGT implements ConstraintValidator<ValidPayoffFunction, String> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(M\\d+|S\\d+|\\{[^}]+\\}|u\\d+)");

  /**
   * Validates the fitness function by checking its syntax and allowed variables.
   *
   * @param value   The fitness function string to validate.
   * @param context The validation context for constraint violations.
   * @return `true` if the function is valid, otherwise `false`.
   */
  @Override
  public void initialize(ValidPayoffFunction constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value.equalsIgnoreCase(StableMatchingConst.DEFAULT_EVALUATE_FUNC)) {
      return true;
    }
    String cleanFunc = value.replaceAll("\\s+", "");
    try {
      Set<String> variables = extractVariables(cleanFunc);

      if (variables.isEmpty()) {
        return false;
      }


      ExpressionBuilder builder = new ExpressionBuilder(cleanFunc);
      

      Expression expression = builder.build();

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
}