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
 * Validator class for checking the validity of evaluate functions provided as an array of strings.
 * Ensures that each function is either the default evaluate function or a valid mathematical
 * expression with supported variables.
 */
public class EvaluateFunctionValidator implements
    ConstraintValidator<ValidEvaluateFunction, String[]> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P\\d+|W\\d+|R\\d+)");

  /**
   * Validates the array of evaluate functions. Each function is checked to ensure it is either the
   * default evaluate function or a valid mathematical expression with supported variables.
   *
   * @param values  the array of evaluate functions to validate
   * @param context the context in which the constraint is evaluated
   * @return true if all evaluate functions are valid, false otherwise
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
   * Extracts variables from the evaluate function using a predefined pattern. Variables must match
   * the pattern (P\\d+|W\\d+|R\\d+).
   *
   * @param func the evaluate function from which to extract variables
   * @return a set of variables found in the function
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