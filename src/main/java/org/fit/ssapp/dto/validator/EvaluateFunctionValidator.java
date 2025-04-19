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
 * Validator class for checking the validity of evaluate functions provided as an array of strings.
 * Ensures that each function is either the default evaluate function or a valid mathematical
 * expression with supported variables.
 */
public class EvaluateFunctionValidator implements
        ConstraintValidator<ValidEvaluateFunction, String[]> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P\\d+|W\\d+|R\\d+)");
  private static final Pattern OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]{2,}");
  private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
  private static final Pattern SINGLE_OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]");
  private static final Pattern BRACKET_PATTERN = Pattern.compile("[()]");



  private static class ValidationError {
    private final String message;
    private final int position;

    public ValidationError(String message, int position) {
      this.message = message;
      this.position = position;
    }
  }
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

      if (validateExp4j(func)) {
        return true;
      }

      List<ValidationError> errors = collectAllErrors(func);

      if (!errors.isEmpty()) {
        context.disableDefaultConstraintViolation();

        for (ValidationError error : errors) {
          String message = formatErrorMessage(error, func);
          context.buildConstraintViolationWithTemplate(message)
                  .addConstraintViolation();
        }
        return false;
      }
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

    ValidationError charError = checkInvalidCharacters(expression);
    if (charError != null) {
      errors.add(charError);
    }

    ValidationError divisionByZeroError = checkDivisionByZero(expression);
    if (divisionByZeroError != null) {
      errors.add(divisionByZeroError);
    }

    return errors;
  }

  private ValidationError checkInvalidCharacters(String expression) {
    StringBuilder currentToken = new StringBuilder();
    int position = 0;

    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);

      if (Character.isWhitespace(c)) {
        if (currentToken.length() > 0) {
          ValidationError error = validateToken(currentToken.toString(), position);
          if (error != null) return error;
          currentToken = new StringBuilder();
        }
        continue;
      }

      if (isSingleCharacterToken(c)) {
        if (currentToken.length() > 0) {
          ValidationError error = validateToken(currentToken.toString(), position);
          if (error != null) return error;
          currentToken = new StringBuilder();
        }

        if (!isValidSingleCharacter(c)) {
          return new ValidationError("Invalid character '" + c + "'", i);
        }
        continue;
      }

      if (currentToken.length() == 0) {
        position = i;
      }
      currentToken.append(c);
    }

    if (currentToken.length() > 0) {
      return validateToken(currentToken.toString(), position);
    }

    return null;
  }

  private boolean isSingleCharacterToken(char c) {
    return SINGLE_OPERATOR_PATTERN.matcher(String.valueOf(c)).matches()
            || BRACKET_PATTERN.matcher(String.valueOf(c)).matches();
  }

  private boolean isValidSingleCharacter(char c) {
    return SINGLE_OPERATOR_PATTERN.matcher(String.valueOf(c)).matches()
            || BRACKET_PATTERN.matcher(String.valueOf(c)).matches();
  }

  private ValidationError validateToken(String token, int position) {
    if (VARIABLE_PATTERN.matcher(token).matches()) {
      return null;
    }

    if (NUMBER_PATTERN.matcher(token).matches()) {
      return null;
    }

    return new ValidationError("Invalid token '" + token + "'", position);
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

  private ValidationError checkDivisionByZero(String expression) {
    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (c == '/') {
        String denominator = extractDenominator(expression, i + 1);
        if (denominator.equals("0") || denominator.equals("0.0")) {
          return new ValidationError("Division by zero", i);
        }
      }
    }
    return null;
  }

  private String extractDenominator(String expression, int startPos) {
    StringBuilder denominator = new StringBuilder();
    for (int i = startPos; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (Character.isWhitespace(c)) {
        continue;
      }
      if (isSingleCharacterToken(c) || c == ')') {
        break;
      }
      denominator.append(c);
    }
    return denominator.toString().trim();
  }

  private String formatErrorMessage(ValidationError error, String expression) {
    return String.format("%s at position %d in function: %s",
            error.message,
            error.position,
            expression);
  }

  // .-..-.
}