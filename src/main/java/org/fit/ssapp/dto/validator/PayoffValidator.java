package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.fit.ssapp.constants.GameTheoryConst;

public class PayoffValidator implements ConstraintValidator<ValidPayoffFunction, String> {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P[0-9]+)?p[0-9]+");
  private static final Pattern VALID_PATTERN = Pattern.compile(
      "^[\\s]*([pP]\\d+|P\\d+p\\d+|[\\d.]+|[+\\-*/()\\s]|sqrt|log|ceil|floor|abs|sin|cos|tan|SUM|AVERAGE|MIN|MAX|PRODUCT|MEDIAN|RANGE)+[\\s]*$",
      Pattern.CASE_INSENSITIVE
  );
  
  // Pattern to match potential division by zero
  private static final Pattern DIVISION_PATTERN = Pattern.compile("([^\\s\\)\\(]+)/([^\\s\\)\\(]+)");
  
  // Pattern to match function with arguments
  private static final Pattern FUNCTION_ARGS_PATTERN = Pattern.compile("(sqrt|log|ceil|floor|abs|sin|cos|tan)\\(([^()]*)\\)");
  
  // Pattern to match invalid operators
  private static final Pattern INVALID_OPERATOR_PATTERN = Pattern.compile("[^+\\-*/()\\d\\w\\s,.\\^%]");
  
  // Map of function names to number of arguments
  private static final Map<String, Integer> FUNCTION_ARGS_COUNT = new HashMap<>();
  
  static {
    FUNCTION_ARGS_COUNT.put("abs", 1);
    FUNCTION_ARGS_COUNT.put("sqrt", 1);
    FUNCTION_ARGS_COUNT.put("log", 1);
    FUNCTION_ARGS_COUNT.put("ceil", 1);
    FUNCTION_ARGS_COUNT.put("floor", 1);
    FUNCTION_ARGS_COUNT.put("sin", 1);
    FUNCTION_ARGS_COUNT.put("cos", 1);
    FUNCTION_ARGS_COUNT.put("tan", 1);
  }
  
  // Class to store validation errors
  private static class ValidationError {
    private final String message;
    private final String expression;
    
    public ValidationError(String message, String expression) {
      this.message = message;
      this.expression = expression;
    }
    
    public String getMessage() {
      return message;
    }
  }

  @Override
  public void initialize(ValidPayoffFunction constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid expression: Empty expression")
          .addConstraintViolation();
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
    
    // First, perform detailed validation
    List<ValidationError> errors = validateDetailed(value);
    if (!errors.isEmpty()) {
      // Report the first error
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(errors.get(0).getMessage())
          .addConstraintViolation();
      return false;
    }

    if (!VALID_PATTERN.matcher(value).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid payoff function syntax")
          .addConstraintViolation();
      return false;
    }

    try {
      // Extract variables
      Set<String> variables = extractVariables(value);
      
      // Prepare expression for validation
      String tempExpr = value;
      for (String var : variables) {
        tempExpr = tempExpr.replaceAll(Pattern.quote(var), "1");
      }
      
      // Build and validate the expression
      ExpressionBuilder builder = new ExpressionBuilder(tempExpr);
      Expression expression = builder.build();
      ValidationResult validationResult = expression.validate();
      
      if (!validationResult.isValid()) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Invalid mathematical expression: " + validationResult.getErrors().get(0))
            .addConstraintViolation();
        return false;
      }
      
      // Perform math validation with variables
      if (!variables.isEmpty()) {
        // Add variables to expression builder
        builder = new ExpressionBuilder(value);
        for (String var : variables) {
          builder.variable(var);
        }
        expression = builder.build();
        
        // Check for math errors with test values
        errors = validateMathOperations(expression, variables);
        if (!errors.isEmpty()) {
          context.disableDefaultConstraintViolation();
          context.buildConstraintViolationWithTemplate(errors.get(0).getMessage())
              .addConstraintViolation();
          return false;
        }
      }
      
      return true;
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid mathematical expression: " + e.getMessage())
          .addConstraintViolation();
      return false;
    }
  }
  
  /**
   * Performs detailed validation of the function syntax.
   *
   * @param func The function to validate.
   * @return A list of validation errors.
   */
  private List<ValidationError> validateDetailed(String func) {
    List<ValidationError> errors = new ArrayList<>();
    
    // Check for empty function
    if (func.trim().isEmpty()) {
      errors.add(new ValidationError("Invalid expression: Empty expression", func));
      return errors;
    }
    
    // Check for invalid operators
    Matcher invalidOpMatcher = INVALID_OPERATOR_PATTERN.matcher(func);
    if (invalidOpMatcher.find()) {
      String invalidOp = invalidOpMatcher.group();
      errors.add(new ValidationError("Invalid operator: Unrecognized operator '" + invalidOp + "' in '" + func + "'", func));
    }

    // Check parentheses matching
    int openParenCount = 0;
    for (char c : func.toCharArray()) {
      if (c == '(') {
        openParenCount++;
      } else if (c == ')') {
        openParenCount--;
        if (openParenCount < 0) {
          errors.add(new ValidationError("Invalid syntax: Mismatched parentheses in '" + func + "'", func));
          break;
        }
      }
    }
    if (openParenCount > 0) {
      errors.add(new ValidationError("Invalid syntax: Missing closing parenthesis in '" + func + "'", func));
    }

    // Check function arguments
    Matcher funcArgsMatcher = FUNCTION_ARGS_PATTERN.matcher(func);
    while (funcArgsMatcher.find()) {
      String funcName = funcArgsMatcher.group(1).toLowerCase();
      String args = funcArgsMatcher.group(2).trim();
      
      // Verify function exists
      if (!FUNCTION_ARGS_COUNT.containsKey(funcName)) {
        errors.add(new ValidationError("Invalid function: Function '" + funcName + "' does not exist in '" + func + "'", func));
        continue;
      }
      
      // Check empty arguments
      if (args.isEmpty()) {
        errors.add(new ValidationError("Invalid function syntax: Missing argument for " + funcName + " function in '" + funcName + "()'", func));
        continue;
      }
      
      // Check if arguments contain placeholders or incomplete data
      if (args.contains("?") || args.contains("...")) {
        errors.add(new ValidationError("Invalid function syntax: Incomplete arguments for " + funcName + " function in '" + funcName + "(" + args + ")'", func));
        continue;
      }
      
      // Count arguments
      int expectedArgCount = FUNCTION_ARGS_COUNT.get(funcName);
      String[] argArray = args.split(",");
      if (argArray.length != expectedArgCount) {
        errors.add(new ValidationError(
            "Invalid function syntax: " + funcName + " requires " + expectedArgCount + 
            " argument(s), but found " + argArray.length + " in '" + funcName + "(" + args + ")'", func));
      }
    }

    return errors;
  }
  
  /**
   * Validates math operations by testing with boundary values.
   *
   * @param expression The expression to validate.
   * @param variables The set of variables in the expression.
   * @return A list of validation errors.
   */
  private List<ValidationError> validateMathOperations(Expression expression, Set<String> variables) {
    List<ValidationError> errors = new ArrayList<>();
    String expressionString = expression.toString();
    
    // Test for division by zero
    Matcher divisionMatcher = DIVISION_PATTERN.matcher(expressionString);
    while (divisionMatcher.find()) {
      String denominator = divisionMatcher.group(2);
      // If denominator is a constant 0
      if (denominator.equals("0")) {
        errors.add(new ValidationError(
            "Invalid math operation: Division by zero in '" + divisionMatcher.group() + "'", 
            expressionString));
        continue;
      }
      
      // If denominator is a variable, test with 0
      if (variables.contains(denominator)) {
        try {
          // Backup all variables with default value 1
          Map<String, Double> varBackup = new HashMap<>();
          for (String var : variables) {
            varBackup.put(var, 1.0);
            expression.setVariable(var, 1.0);
          }
          
          // Test with denominator = 0
          expression.setVariable(denominator, 0.0);
          expression.evaluate();
          
          // Restore variables
          for (Map.Entry<String, Double> entry : varBackup.entrySet()) {
            expression.setVariable(entry.getKey(), entry.getValue());
          }
        } catch (ArithmeticException | IllegalArgumentException e) {
          errors.add(new ValidationError(
              "Invalid math operation: Potential division by zero with variable '" + denominator + "'", 
              expressionString));
        }
      }
    }
    
    // Check for sqrt of negative numbers
    if (expressionString.contains("sqrt(")) {
      // Test with negative values for sqrt arguments
      for (String var : variables) {
        try {
          // Set all variables to positive values
          for (String v : variables) {
            expression.setVariable(v, 1.0);
          }
          
          // Then test with this variable negative
          expression.setVariable(var, -1.0);
          expression.evaluate();
        } catch (IllegalArgumentException e) {
          if (e.getMessage().contains("sqrt")) {
            errors.add(new ValidationError(
                "Invalid math operation: Square root of negative number when " + var + " is negative", 
                expressionString));
          }
        }
      }
    }
    
    // Check for log of non-positive numbers
    if (expressionString.contains("log(")) {
      // Test with zero and negative values
      for (String var : variables) {
        // Test with zero
        try {
          // Set all variables to positive
          for (String v : variables) {
            expression.setVariable(v, 1.0);
          }
          
          // Test with this variable = 0
          expression.setVariable(var, 0.0);
          expression.evaluate();
        } catch (IllegalArgumentException e) {
          if (e.getMessage().contains("log")) {
            errors.add(new ValidationError(
                "Invalid math operation: Logarithm of zero when " + var + " = 0", 
                expressionString));
          }
        }
        
        // Test with negative
        try {
          // Set all variables to positive
          for (String v : variables) {
            expression.setVariable(v, 1.0);
          }
          
          // Test with this variable negative
          expression.setVariable(var, -1.0);
          expression.evaluate();
        } catch (IllegalArgumentException e) {
          if (e.getMessage().contains("log")) {
            errors.add(new ValidationError(
                "Invalid math operation: Logarithm of negative number when " + var + " is negative", 
                expressionString));
          }
        }
      }
    }
    
    return errors;
  }
  
  /**
   * Extracts valid variable names from a payoff function.
   * Uses regex matching to identify variables like p1, p2, P1p1, P2p3, etc.
   *
   * @param func The payoff function as a string.
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

  private boolean isOperator(String token) {
    return token.matches("[+\\-*/]");
  }
}