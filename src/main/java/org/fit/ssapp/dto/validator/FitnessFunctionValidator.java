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
  private static final Pattern OPERATOR_PATTERN = Pattern.compile("([+\\-*/^]\\s*[+\\-*/^])");
  private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
  private static final Pattern SINGLE_OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]");
  private static final Pattern BRACKET_PATTERN = Pattern.compile("[(){}]");

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

    if (!checkExpressions(context, value, dto )) { // sigma and out of index for Si and Mi
      isValid = false;
    }

    String modifiedExpression = replaceExpression(value);

    if (!validateExp4j(modifiedExpression)) {
      collectAllErrors(context, value);
      isValid = false;
    }

    return isValid;
  }

  private String replaceExpression(String expression) {
    return expression
            .replaceAll("SIGMA\\{[^}]*\\}", "1.0")
            .replaceAll("M\\d+", "1.0")
            .replaceAll("S\\d+", "1.0");
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
    checkInvalidCharacters(context, expression);
  }

  private boolean checkExpressions(ConstraintValidatorContext context, String expression, StableMatchingProblemDto dto ) {
    boolean isValid = true;
    try {
      Set<String> variables = extractVariables(expression);

      for (String var : variables) {
          if (!validateVariableLimits(context,expression, var, dto)) {
            isValid = false;
          }
      }
    } catch (Exception e) {
      addViolation(context, "expression",
              "Error in SIGMA expression: " + e.getMessage());
      isValid = false;
    }
    return isValid ;
  }

  private boolean validateVariableLimits(ConstraintValidatorContext context, String function, String var, StableMatchingProblemDto dto) {
    boolean isValid = true;
      if (var.matches("SIGMA\\{S[0-9]+}")) {
        String innerExpression = var.substring(6, var.length() - 1);
        try {
          int index = Integer.parseInt(innerExpression.substring(1));
          int wrongIndex = function.indexOf(String.valueOf(index));
          if (index > dto.getNumberOfSets() || index < 1) {
            addViolation(
                    context,
                    "fitnessFunction",
                    "Invalid after SIGMA index: " + index + ", at position " + wrongIndex + ". Must be between 1 and " + dto.getNumberOfSets()
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          int wrongIndex = function.indexOf(var.substring(1)) + 5;
          addViolation(context, "evaluateFunctions", "Invalid after SIGMA variable format: " + var + ", at position " + wrongIndex);
          isValid = false;
        }
      } else if (var.matches("M\\d+.*")) {
        try {
          int index = Integer.parseInt(var.substring(1));
          int wrongIndex = function.indexOf(String.valueOf(index));
          int maxM = dto.getIndividualSetIndices().length;
          if (index > maxM || index < 1) {
            addViolation(
                    context,
                    "fitnessFunction",
                    "Invalid M index: " + index + ", at position " + wrongIndex + ". Must be between 1 and " + maxM
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          int wrongIndex = function.indexOf(var.substring(1));
          addViolation(context, "evaluateFunctions", "Invalid M variable format: " + var + ", at position " + wrongIndex);
          isValid = false;
        }
      } else if (var.matches("S\\d+.*")) {
        try {
          int index = Integer.parseInt(var.substring(1));
          int wrongIndex = function.indexOf(String.valueOf(index));
          if (index > dto.getNumberOfSets() || index < 1) {
            addViolation(
                    context,
                    "fitnessFunction",
                    "Invalid S index: " + index + ", at position " + wrongIndex + ". Must be between 1 and " + dto.getNumberOfSets()
            );
            isValid = false;
          }
        } catch (NumberFormatException e) {
          int wrongIndex = function.indexOf(var.substring(1));
          addViolation(context, "evaluateFunctions", "Invalid S variable format: " + var + ", at position " + wrongIndex);
          isValid = false;
        }
      } else {
        int wrongIndex = function.indexOf(var.substring(1)) + 5;
        addViolation(context, "evaluateFunctions", "Invalid inner Sigma format: " + var + ", at position " + wrongIndex);
        isValid = false;
      }
    return isValid;
  }

  private void checkInvalidCharacters(ConstraintValidatorContext context, String expression) {
    StringBuilder currentToken = new StringBuilder();
    int position = 0;
    int i = 0;

    while (i < expression.length()) {
      char c = expression.charAt(i);

      if (i + 4 < expression.length() && expression.substring(i, i + 5).equals("SIGMA")) {
        if (currentToken.length() > 0) {
          validateToken(context, currentToken.toString(), position);
          currentToken = new StringBuilder();
        }

        int openBracket = expression.indexOf('{', i + 5);
        if (openBracket != -1) {
          int closeBracket = findClosingBracket(expression, openBracket);
          if (closeBracket != -1) {
            i = closeBracket + 1;
            continue;
          }
        }
        i++;
        continue;
      }

      // Xử lý khoảng trắng
      if (Character.isWhitespace(c)) {
        if (currentToken.length() > 0) {
          validateToken(context, currentToken.toString(), position);
          currentToken = new StringBuilder();
        }
        i++;
        continue;
      }

      // Xử lý ký tự đơn
      if (isSingleCharacterToken(c)) {
        if (currentToken.length() > 0) {
          validateToken(context, currentToken.toString(), position);
          currentToken = new StringBuilder();
        }

        if (!isValidSingleCharacter(c)) {
          addViolation(context, "evaluateFunctions", "Invalid character '" + c + "' at position " + i);
          return;
        }
        i++;
        continue;
      }

      if (currentToken.length() == 0) {
        position = i;
      }
      currentToken.append(c);
      i++;
    }

    if (currentToken.length() > 0) {
      validateToken(context, currentToken.toString(), position);
    }
  }

  private int findClosingBracket(String expression, int openBracketPos) {
    int count = 1;
    for (int i = openBracketPos + 1; i < expression.length(); i++) {
      if (expression.charAt(i) == '{') {
        count++;
      } else if (expression.charAt(i) == '}') {
        count--;
        if (count == 0) {
          return i;
        }
      }
    }
    return -1;
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