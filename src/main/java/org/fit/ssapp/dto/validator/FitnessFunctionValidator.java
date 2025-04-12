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
  
  private static final TreeSet<String> VALID_FUNCTIONS = new TreeSet<>(Set.of("SUM","AVERAGE","MIN","MAX","PRODUCT","MEDIAN","RANGE"));

  /**
   * Validates the fitness function by checking its syntax and allowed variables.
   *
   * @param value   The fitness function string to validate.
   * @param context The validation context for constraint violations.
   * @return `true` if the function is valid, otherwise `false`.
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return false;
    }
    
    // Check for default function names
    if (value.equalsIgnoreCase(StableMatchingConst.DEFAULT_EVALUATE_FUNC) || 
        value.equalsIgnoreCase(GameTheoryConst.DEFAULT_PAYOFF_FUNC)) {
      return true;
    }
    
    // Check if it's one of the default functions
    if (VALID_FUNCTIONS.contains(value.toUpperCase().trim())) {
      return true;
    }
    
    String cleanFunc = value.replaceAll("\\s+", "");
    try {
      Set<String> variables = extractVariables(cleanFunc);

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