package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.constants.GameTheoryConst;

public class PayoffValidator implements ConstraintValidator<ValidPayoffFunction, String> {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P[0-9]+)?p[0-9]+");
  private static final Pattern VALID_PATTERN = Pattern.compile(
      "^[\\s]*([pP]\\d+|[\\d.]+|[+\\-*/()\\s]|sqrt|log|ceil|floor|abs|sin|cos|tan|SUM|AVERAGE|MIN|MAX|PRODUCT|MEDIAN|RANGE)+[\\s]*$",
      Pattern.CASE_INSENSITIVE
  );

  @Override
  public void initialize(ValidPayoffFunction constraintAnnotation) {
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

    // Accept default functions
    if (value.equalsIgnoreCase("SUM") ||
        value.equalsIgnoreCase("AVERAGE") ||
        value.equalsIgnoreCase("MIN") ||
        value.equalsIgnoreCase("MAX") ||
        value.equalsIgnoreCase("PRODUCT") ||
        value.equalsIgnoreCase("MEDIAN") ||
        value.equalsIgnoreCase("RANGE")) {
      return true;
    }

    if (!VALID_PATTERN.matcher(value).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid payoff function syntax")
          .addConstraintViolation();
      return false;
    }
    int openParen = 0;
    for (char c : value.toCharArray()) {
      if (c == '(') openParen++;
      if (c == ')') openParen--;
      if (openParen < 0) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Mismatched parentheses")
            .addConstraintViolation();
        return false;
      }
    }
    if (openParen != 0) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Mismatched parentheses")
          .addConstraintViolation();
      return false;
    }

    String[] tokens = value.split("\\s+");
    for (int i = 0; i < tokens.length - 1; i++) {
      if (isOperator(tokens[i]) && isOperator(tokens[i + 1])) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Consecutive operators are not allowed")
            .addConstraintViolation();
        return false;
      }
    }

    try {
      String temp = value.replaceAll("P([0-9]+)p([0-9]+)", "1");
      temp = temp.replaceAll("p([0-9]+)", "1");
      Expression builder = new ExpressionBuilder(temp).build();
      return builder.validate().isValid();
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid mathematical expression: " + e.getMessage())
          .addConstraintViolation();
      return false;
    }
  }

  private boolean isOperator(String token) {
    return token.matches("[+\\-*/]");
  }
}