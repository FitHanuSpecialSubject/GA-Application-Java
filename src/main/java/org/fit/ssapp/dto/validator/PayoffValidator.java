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
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.Strategy;

public class PayoffValidator implements ConstraintValidator<ValidPayoffFunction, Object> {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P[0-9]+)?p[0-9]+");
  private static final Pattern PROPERTY_PATTERN = Pattern.compile("p(\\d+)");
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
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    // Handle case when value is not a GameTheoryProblemDto
    if (!(value instanceof GameTheoryProblemDto dto)) {
      // If it's a String (for backward compatibility)
      if (value instanceof String) {
        // Find the highest property index in the expression
        String expression = (String) value;
        int maxPropertyIndex = findHighestPropertyVariable(expression);
        return isValidString(expression, context, maxPropertyIndex);
      }
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid data type for payoff function validation")
          .addConstraintViolation();
      return false;
    }

    // Get payoff function from DTO
    String payoffFunction = dto.getDefaultPayoffFunction();
    
    // Find max property count from strategies
    int maxPropertyCount = findMaxPropertyCount(dto);
    
    return isValidString(payoffFunction, context, maxPropertyCount);
  }
  
  /**
   * Finds the maximum number of properties in any strategy of any player
   * 
   * @param dto The GameTheoryProblemDto containing players and strategies
   * @return The maximum property count found
   */
  private int findMaxPropertyCount(GameTheoryProblemDto dto) {
    int maxCount = 0;
    
    if (dto.getNormalPlayers() != null) {
      for (var player : dto.getNormalPlayers()) {
        if (player.getStrategies() != null) {
          for (Strategy strategy : player.getStrategies()) {
            if (strategy.getProperties() != null) {
              maxCount = Math.max(maxCount, strategy.getProperties().size());
            }
          }
        }
      }
    }
    
    return maxCount > 0 ? maxCount : 5; // Default to 5 if no properties found
  }

  /**
   * Finds the highest property index referenced (e.g., p1, p2, p10)
   * 
   * @param expression The payoff function expression to analyze
   * @return The highest property index found, or 5 if no variables found
   */
  private int findHighestPropertyVariable(String expression) {
    int highestIndex = 5; // Default to at least 5 properties
    Pattern pPattern = Pattern.compile("p(\\d+)");
    Matcher matcher = pPattern.matcher(expression);
    
    while (matcher.find()) {
      int propertyIndex = Integer.parseInt(matcher.group(1));
      highestIndex = Math.max(highestIndex, propertyIndex);
    }
    
    return highestIndex; // Return the highest index found
  }
  
  /**
   * Validates a payoff function expression string
   * 
   * @param value String expression to validate
   * @param context Validator context
   * @param propertyCount Maximum property count available
   * @return true if the expression is valid, false otherwise
   */
  private boolean isValidString(String value, ConstraintValidatorContext context, int propertyCount) {
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
    
    // Check property index limit based on actual property count
    Pattern pPattern = Pattern.compile("p(\\d+)");
    Matcher pMatcher = pPattern.matcher(value);
    
    // Collect all invalid property indices
    Set<Integer> invalidIndices = new HashSet<>();
    while (pMatcher.find()) {
      int propertyIndex = Integer.parseInt(pMatcher.group(1));
      if (propertyIndex < 1 || propertyIndex > propertyCount) {
        invalidIndices.add(propertyIndex);
      }
    }
    
    // If any invalid indices are found, report the error
    if (!invalidIndices.isEmpty()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          "Invalid payoff function: Property " + 
          (invalidIndices.size() == 1 ? 
              "p" + invalidIndices.iterator().next() : 
              "variables " + formatInvalidIndices(invalidIndices)) + 
          " exceeds available properties. Maximum property count is " + propertyCount + 
          " (valid variables are p1 to p" + propertyCount + ").")
          .addConstraintViolation();
      return false;
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
    
    String cleanFunc = value.replaceAll("\\s+", "");
    try {
      Set<String> variables = extractVariables(cleanFunc);
      
      // Create evaluable expression with dummy values for validation
      ExpressionBuilder builder = new ExpressionBuilder(cleanFunc);
      
      // Add all variables with dummy values
      for (String var : variables) {
        builder.variable(var);
      }
      
      Expression expression = builder.build();
      
      // Set dummy values to validate expression
      for (String var : variables) {
        expression.setVariable(var, 1.0);
      }
      
      // Validate expression
      ValidationResult validationResult = expression.validate();
      if (!validationResult.isValid()) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Invalid payoff function syntax: '" + validationResult.getErrors().get(0) + "'")
            .addConstraintViolation();
        return false;
      }
      
      // Check for mathematical issues
      List<ValidationError> mathErrors = validateMathOperations(expression, variables);
      if (!mathErrors.isEmpty()) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(mathErrors.get(0).getMessage())
            .addConstraintViolation();
        return false;
      }
      
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          "Invalid payoff function syntax: '" + e.getMessage() + "'")
          .addConstraintViolation();
      return false;
    }
    
    return true;
  }

  /**
   * Format a list of invalid indices for error message
   */
  private String formatInvalidIndices(Set<Integer> indices) {
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (Integer idx : indices) {
      if (count > 0) {
        sb.append(count == indices.size() - 1 ? " and " : ", ");
      }
      sb.append("p").append(idx);
      count++;
    }
    return sb.toString();
  }

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