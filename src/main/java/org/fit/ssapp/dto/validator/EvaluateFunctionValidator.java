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
 * **EvaluateFunctionValidator** - Validator for evaluation function syntax.
 * This class validates whether the given evaluation functions are **mathematically valid**
 * and conform to the expected format with **allowed variables**.
 */
public class EvaluateFunctionValidator implements
        ConstraintValidator<ValidEvaluateFunction, String[]> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P\\d+|W\\d+|R\\d+)");

  /**
   * Validates an array of evaluation functions.
   * - Each function is checked for **correct syntax** and **valid variables**.
   * - Uses `exp4j` to **parse and evaluate** mathematical expressions.
   * - If a function is invalid, a constraint violation message is generated.
   *
   * @param values  The array of evaluation functions to validate.
   * @param context The validation context for adding constraint violations.
   * @return `true` if all functions are valid, otherwise `false`.
   */
  @Override
  public boolean isValid(String[] values, ConstraintValidatorContext context) {
    for (String func : values) {
      if (func.equalsIgnoreCase(StableMatchingConst.DEFAULT_EVALUATE_FUNC)) {
        continue;
      }
      String cleanFunc = func.replaceAll("\\s+", "");
      try {
        Set<String> variables = extractVariables(cleanFunc);
        ExpressionBuilder builder = new ExpressionBuilder(cleanFunc);
        for (String var : variables) {
          builder.variable(var);
        }
        Expression expression = builder.build();
        for (String var : variables) {
          expression.setVariable(var, 1.0);
        }
        expression.evaluate();
      } catch (Exception e) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "Invalid evaluate function syntax: '" + func + "'")
                .addConstraintViolation();
        return false;
      }
    }
    return true;
  }

  /**
   * Extracts valid variable names from a mathematical expression.
   * - Uses **regex matching** to identify variables in the form `P#`, `W#`, `R#`.
   * - Returns a **set of unique variable names** found in the function.
   *
   * @param func The mathematical function as a string.
   * @return A set of valid variable names found in the function.
   */
  private Set<String> extractVariables(String func) {
    Set<String> variables = new HashSet<>();
    Matcher matcher = VARIABLE_PATTERN.matcher(func);
    while (matcher.find()) {
      variables.add(matcher.group(1));
    }
    return variables;
  }
}