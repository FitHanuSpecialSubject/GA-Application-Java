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
 * Validator class for checking the validity of evaluate functions provided as an array of strings.
 * Ensures that each function is either the default evaluate function or a valid mathematical
 * expression with supported variables.
 */
public class EvaluateFunctionValidator implements
        ConstraintValidator<ValidEvaluateFunction, StableMatchingProblemDto> {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P\\d+|W\\d+|R\\d+)");
  private static final Pattern OPERATOR_PATTERN = Pattern.compile("([+\\-*/^]\\s*[+\\-*/^])");
  private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
  private static final Pattern SINGLE_OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]");
  private static final Pattern BRACKET_PATTERN = Pattern.compile("[()]");


  /**
   * Validates the array of evaluate functions. Each function is checked to ensure it is either the
   * default evaluate function or a valid mathematical expression with supported variables.
   *
   * @param values  the array of evaluate functions to validate
   * @param context the context in which the constraint is evaluated
   * @return true if all evaluate functions are valid, false otherwise
   */
  @Override
  public boolean isValid(StableMatchingProblemDto dto, ConstraintValidatorContext context) {
    String[] values = dto.getEvaluateFunctions();
    boolean isValid = true;
    for (String func : values) {
      if (func.equalsIgnoreCase(StableMatchingConst.DEFAULT_EVALUATE_FUNC)) {
        continue;
      }

      if (!validateExp4j(func)) {
        collectAllErrors(context, func);
        isValid = false;
      }

//      Set<String> variables = extractVariables(func);
      if (!validateVariableLimits(context, func, dto)) {
        isValid = false;
      }
    }
    return isValid;
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
  private void collectAllErrors(ConstraintValidatorContext context, String expression) {
    checkBrackets(context, expression);
    checkConsecutiveOperators(context, expression);
    checkDivisionByZero(context, expression);
    checkInvalidCharacters(context, expression);
  }

  private boolean validateVariableLimits(ConstraintValidatorContext context, String function, StableMatchingProblemDto dto) {
    boolean isValid = true;

    Set<String> variables = extractVariables(function);
    for (String var : variables) {
      if (var.startsWith("P") || var.startsWith("p")) {
        try {
          int index = Integer.parseInt(var.substring(1));
          int wrongIndex = function.indexOf(String.valueOf(index));
          if (index > dto.getNumberOfProperty() || index < 1) {
            addViolation(
                    context,
                    "evaluateFunctions",
                    "Invalid P index: " + index + ", at position " + wrongIndex + ". Must be between 1 and " + dto.getNumberOfProperty()
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          addViolation(context, "evaluateFunctions", "Invalid P variable format: " + var);
          isValid = false;
        }
      } else if (var.startsWith("W") || var.startsWith("w")) {
        try {
          int index = Integer.parseInt(var.substring(1));
          int wrongIndex = function.indexOf(String.valueOf(index));
          if (index > dto.getNumberOfProperty() || index < 1) {
            addViolation(
                    context,
                    "evaluateFunctions",
                    "Invalid W index: " + index + ", at position " + wrongIndex + ". Must be between 1 and " + dto.getNumberOfProperty()
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          addViolation(context, "evaluateFunctions", "Invalid W variable format: " + var);
          isValid = false;
        }
      }  else if (var.startsWith("R") || var.startsWith("r")) {
        try {
          int index = Integer.parseInt(var.substring(1));
          int wrongIndex = function.indexOf(String.valueOf(index));
          if (index > dto.getNumberOfProperty() || index < 1) {
            addViolation(
                    context,
                    "evaluateFunctions",
                    "Invalid R index: " + index + ", at position " + wrongIndex + ". Must be between 1 and " + dto.getNumberOfProperty()
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          addViolation(context, "evaluateFunctions", "Invalid R variable format: " + var);
          isValid = false;
        }
      }
    }
    return isValid;
  }

  private void checkInvalidCharacters(ConstraintValidatorContext context, String expression) {
    StringBuilder currentToken = new StringBuilder();
    int position = 0;

    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);

      if (Character.isWhitespace(c)) {
        if (currentToken.length() > 0) {
          validateToken(context, currentToken.toString(), position);
          currentToken = new StringBuilder();
        }
        continue;
      }

      if (isSingleCharacterToken(c)) {
        if (currentToken.length() > 0) {
          validateToken(context, currentToken.toString(), position);
          currentToken = new StringBuilder();
        }

        if (!isValidSingleCharacter(c)) {
          addViolation(context, "evaluateFunctions", "Invalid character '" + c + "' at position " + i);
          return;
        }
        continue;
      }

      if (currentToken.length() == 0) {
        position = i;
      }
      currentToken.append(c);
    }

    if (currentToken.length() > 0) {
      validateToken(context, currentToken.toString(), position);
    }
  }

  private boolean isSingleCharacterToken(char c) {
    return SINGLE_OPERATOR_PATTERN.matcher(String.valueOf(c)).matches()
            || BRACKET_PATTERN.matcher(String.valueOf(c)).matches();
  }

  private boolean isValidSingleCharacter(char c) {
    return SINGLE_OPERATOR_PATTERN.matcher(String.valueOf(c)).matches()
            || BRACKET_PATTERN.matcher(String.valueOf(c)).matches();
  }

  private void validateToken(ConstraintValidatorContext context, String token, int position) {
    if (VARIABLE_PATTERN.matcher(token).matches()) {
      return;
    }

    if (NUMBER_PATTERN.matcher(token).matches()) {
      return;
    }

    if(validateExp4j(token)){
      return;
    }

    addViolation(context, "evaluateFunctions", "Invalid token '" + token + "' at position " + position);
  }

  private void checkBrackets(ConstraintValidatorContext context, String expression) {
    Stack<Integer> stack = new Stack<>();

    for (int i = 0; i < expression.length(); i++) {
      char current = expression.charAt(i);
      if (current == '(') {
        stack.push(i);
      } else if (current == ')') {
        if (stack.isEmpty()) {
          addViolation(context, "evaluateFunctions", "Unmatched closing bracket at position " + i);
        } else {
          stack.pop();
        }
      }
    }

    while (!stack.isEmpty()) {
      int pos = stack.pop();
      addViolation(context, "evaluateFunctions", "Unmatched opening bracket at position " + pos);
    }
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

  private void checkConsecutiveOperators(ConstraintValidatorContext context, String expression) {
    Matcher matcher = OPERATOR_PATTERN.matcher(expression);
    while (matcher.find()) {
      int pos = matcher.start();
      addViolation(context, "evaluateFunctions", "Consecutive operators at position " + pos);
    }
  }

  private void checkDivisionByZero(ConstraintValidatorContext context, String expression) {
    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (c == '/') {
        String denominator = extractDenominator(expression, i + 1);
        if (denominator.matches("0(\\.0+)?")) {
          addViolation(context, "evaluateFunctions", "Division by zero at position " + (i + 1));
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
      if (isSingleCharacterToken(c) || c == ')') {
        break;
      }
      denominator.append(c);
    }
    return denominator.toString().trim();
  }

  private void addViolation(ConstraintValidatorContext context, String field, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message)
            .addPropertyNode(field)
            .addConstraintViolation();
  }

  // .-..-.
}