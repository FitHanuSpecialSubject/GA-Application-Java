package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.fit.ssapp.constants.GameTheoryConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;

/**
 * **FitnessFunctionValidator** - Validator for fitness function syntax in Game Theory.
 * This class ensures that the provided fitness function follows **correct mathematical syntax**
 * and only includes **allowed variables**. It supports:
 * - **u{number}** → Represents utility variables for players in game theory.
 * - Various math functions like abs(), sqrt(), log(), ceil(), etc.
 */
public class FitnessValidateGT implements ConstraintValidator<ValidFitnessFunctionGT, Object> {

  // Pattern to match utility variables u1, u2, etc.
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(u\\d+)");

  // Pattern to match standard mathematical functions
  private static final Pattern FUNCTION_PATTERN = Pattern.compile("(abs|sqrt|log|exp|ceil|floor|sin|cos|tan|pow)\\(");

  // Pattern to match potential division by zero
  private static final Pattern DIVISION_PATTERN = Pattern.compile("([^\\s\\)\\(]+)/([^\\s\\)\\(]+)");

  // Pattern to match function with arguments
  private static final Pattern FUNCTION_ARGS_PATTERN = Pattern.compile("(abs|sqrt|log|exp|ceil|floor|sin|cos|tan|pow)\\(([^()]*)\\)");

  // Pattern to match invalid operators
  private static final Pattern INVALID_OPERATOR_PATTERN = Pattern.compile("[^+\\-*/()\\d\\w\\s,.\\^%]");

  private static final TreeSet<String> VALID_FUNCTIONS = new TreeSet<>(Set.of("SUM","AVERAGE","MIN","MAX","PRODUCT","MEDIAN","RANGE"));

  // Map of function names to number of arguments
  private static final Map<String, Integer> FUNCTION_ARGS_COUNT = new HashMap<>();
  
  static {
    FUNCTION_ARGS_COUNT.put("abs", 1);
    FUNCTION_ARGS_COUNT.put("sqrt", 1);
    FUNCTION_ARGS_COUNT.put("log", 1);
    FUNCTION_ARGS_COUNT.put("exp", 1);
    FUNCTION_ARGS_COUNT.put("ceil", 1);
    FUNCTION_ARGS_COUNT.put("floor", 1);
    FUNCTION_ARGS_COUNT.put("sin", 1);
    FUNCTION_ARGS_COUNT.put("cos", 1);
    FUNCTION_ARGS_COUNT.put("tan", 1);
    FUNCTION_ARGS_COUNT.put("pow", 2);
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

  /**
   * Initializes the validator with annotation information.
   *
   * @param constraintAnnotation The annotation instance for this validator.
   */
  @Override
  public void initialize(ValidFitnessFunctionGT constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    // Handle case when value is not a GameTheoryProblemDto
    if (!(value instanceof GameTheoryProblemDto dto)) {
      // If it's a String (for backward compatibility)
      if (value instanceof String) {
        String expression = (String) value;
        int minimumPlayerCount = findHighestUVariable(expression);
        return isValidString(expression, context, minimumPlayerCount);
      }
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid data type for fitness function validation")
          .addConstraintViolation();
      return false;
    }

    // Get fitness function from DTO
    String fitnessFunction = dto.getFitnessFunction();
    // Get actual player count
    int actualPlayerCount = dto.getNormalPlayers() != null ? dto.getNormalPlayers().size() : 0;

    return isValidString(fitnessFunction, context, actualPlayerCount);
  }

  /**
   * Finds the highest player index referenced in u variables (e.g., u1, u2, u10)
   * 
   * @param expression The fitness function expression to analyze
   * @return The highest player index found, or 1 if no variables found
   */
  private int findHighestUVariable(String expression) {
    int highestIndex = 1; // Default to at least 1 player
    Pattern uPattern = Pattern.compile("u(\\d+)");
    Matcher matcher = uPattern.matcher(expression);
    
    while (matcher.find()) {
      int playerIndex = Integer.parseInt(matcher.group(1));
      highestIndex = Math.max(highestIndex, playerIndex);
    }
    
    return highestIndex; // Return the highest index found
  }

  /**
   * Validates a fitness function expression string
   * 
   * @param value String expression to validate
   * @param context Validator context
   * @param playerCount Actual number of players
   * @return true if the expression is valid, false otherwise
   */
  private boolean isValidString(String value, ConstraintValidatorContext context, int playerCount) {
    if (value == null || value.trim().isEmpty()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid expression: Empty expression")
          .addConstraintViolation();
      return false;
    }

    if (value.equalsIgnoreCase(GameTheoryConst.DEFAULT_PAYOFF_FUNC)) {
      return true;
    }

    // Check if it's a default function
    if (VALID_FUNCTIONS.contains(value.toUpperCase().trim())) {
      return true;
    }

    // Pre-check for direct division by zero pattern
    if (value.matches(".*\\/\\s*0[^\\d].*") || value.matches(".*\\/\\s*0$")) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid fitness function: Division by zero detected")
          .addConstraintViolation();
      return false;
    }

    // Check player index limit based on actual player count
    Pattern uPattern = Pattern.compile("u(\\d+)");
    Matcher uMatcher = uPattern.matcher(value);
    
    while (uMatcher.find()) {
      int playerIndex = Integer.parseInt(uMatcher.group(1));
      if (playerIndex > playerCount) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Invalid fitness function: Variable u" + playerIndex + " refers to non-existent player. The request contains only " + playerCount + " players.")
            .addConstraintViolation();
        return false;
      }
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

    String cleanFunc = value.replaceAll("\\s+", "");
    try {
      Set<String> variables = extractVariables(cleanFunc);
      Set<String> functions = extractFunctions(cleanFunc);

      // Create evaluable expression
      String evaluableExpression = cleanFunc;

      ExpressionBuilder builder = new ExpressionBuilder(evaluableExpression);

      // Add all extracted variables to the builder
      for (String var : variables) {
        builder.variable(var);
      }

      Expression expression = builder.build();
      
      // Test by setting all variables to 1 to avoid uninitialized variables
      for (String var : variables) {
        expression.setVariable(var, 1.0);
      }

      // Check if expression is valid
      ValidationResult validationResult = expression.validate();
      if (!validationResult.isValid()) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                "Invalid fitness function syntax: '" + validationResult.getErrors().get(0) + "'")
            .addConstraintViolation();
        return false;
      }

      // Perform math operation validation with test values
      errors = validateMathOperations(expression, variables);
      if (!errors.isEmpty()) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errors.get(0).getMessage())
            .addConstraintViolation();
        return false;
      }
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Invalid fitness function syntax: '" + e.getMessage() + "'")
          .addConstraintViolation();
      return false;
    }
    return true;
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

    // Check parentheses matching using Stack
    Stack<Integer> parenStack = new Stack<>();
    for (int i = 0; i < func.length(); i++) {
      char c = func.charAt(i);
      if (c == '(') {
        parenStack.push(i);
      } else if (c == ')') {
        if (parenStack.isEmpty()) {
          errors.add(new ValidationError("Invalid syntax: Extra closing parenthesis at position " + i + " in '" + func + "'", func));
        } else {
          parenStack.pop();
        }
      }
    }
    
    if (!parenStack.isEmpty()) {
      int position = parenStack.peek();
      errors.add(new ValidationError("Invalid syntax: Unclosed opening parenthesis at position " + position + " in '" + func + "'", func));
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
            "Invalid fitness function: Division by zero detected in '" + divisionMatcher.group() + "'", 
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
              "Invalid fitness function: Potential division by zero with variable '" + denominator + "'", 
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
   * Extracts valid variable names from a mathematical function.
   * - Uses **regex matching** to identify valid variables (u1, u2, etc).
   * - Returns a **set of unique variable names** found in the function.
   *
   * @param func The mathematical function as a string.
   * @return A set of valid variable names found in the function.
   */
  private Set<String> extractVariables(String func) {
    Set<String> variables = new HashSet<>();
    Matcher matcher = VARIABLE_PATTERN.matcher(func);
    while (matcher.find()) {
      String variable = matcher.group(0);
      variables.add(variable);
    }
    return variables;
  }

  /**
   * Extracts mathematical function names from an expression.
   *
   * @param func The mathematical function as a string.
   * @return A set of function names found in the expression.
   */
  private Set<String> extractFunctions(String func) {
    Set<String> functions = new HashSet<>();
    Matcher matcher = FUNCTION_PATTERN.matcher(func);
    while (matcher.find()) {
      // Remove the trailing parenthesis
      String function = matcher.group(0);
      function = function.substring(0, function.length() - 1);
      functions.add(function);
    }
    return functions;
  }
}