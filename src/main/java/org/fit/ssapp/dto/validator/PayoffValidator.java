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
import java.util.stream.Collectors;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.constants.GameTheoryConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.Strategy;

public class PayoffValidator implements ConstraintValidator<ValidPayoffFunction, Object> {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P[0-9]+)?p[0-9]+");
  private static final Pattern PROPERTY_PATTERN = Pattern.compile("p(\\d+)");
  
  // Pattern for matching all built-in functions from AppConst
  private static final String FUNCTION_NAMES_REGEX = AppConst.BUILTIN_FUNCTION_NAMES.stream()
      .collect(Collectors.joining("|"));
  
  private static final Pattern VALID_PATTERN = Pattern.compile(
      "^[\\s]*([pP]\\d+|P\\d+p\\d+|[\\d.]+|[+\\-*/()\\s]|" + FUNCTION_NAMES_REGEX + "|SUM|AVERAGE|MIN|MAX|PRODUCT|MEDIAN|RANGE)+[\\s]*$",
      Pattern.CASE_INSENSITIVE
  );
  
  // Pattern to match potential division by zero
  private static final Pattern DIVISION_PATTERN = Pattern.compile("([^\\s\\)\\(]+)/([^\\s\\)\\(]+)");
  
  // Pattern to match function with arguments - using all built-in functions from AppConst
  private static final Pattern FUNCTION_ARGS_PATTERN = Pattern.compile(
      "(" + FUNCTION_NAMES_REGEX + ")\\(([^()]*)\\)"
  );
  
  // Pattern to match invalid operators
  private static final Pattern INVALID_OPERATOR_PATTERN = Pattern.compile("[^+\\-*/()\\d\\w\\s,.\\^%]");
  
  // Map of function names to number of arguments
  private static final Map<String, Integer> FUNCTION_ARGS_COUNT = new HashMap<>();
  
  static {
    for (String funcName : AppConst.BUILTIN_FUNCTION_NAMES) {
      FUNCTION_ARGS_COUNT.put(funcName, funcName.equals("pow") ? 2 : 1);
    }
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
          .addPropertyNode("payoffFunction")
          .addConstraintViolation();
      return false;
    }

    // Get payoff function from DTO
    String payoffFunction = dto.getDefaultPayoffFunction();
    String fitnessFunction = dto.getFitnessFunction();
    
    int maxPropertyCount = 5; // default value
    if (dto.getNormalPlayers() != null && !dto.getNormalPlayers().isEmpty()) {
      var firstPlayer = dto.getNormalPlayers().get(0);
      if (firstPlayer.getStrategies() != null && !firstPlayer.getStrategies().isEmpty()) {
        var firstStrategy = firstPlayer.getStrategies().get(0);
        if (firstStrategy.getProperties() != null) {
          maxPropertyCount = firstStrategy.getProperties().size();
        }
      }
    }
    
    int playerCount = getPlayerCount(dto);
    
    boolean payoffValid = isValidString(payoffFunction, context, maxPropertyCount);
    
    if (payoffValid && playerCount > 0 && fitnessFunction != null) {
      // Validate fitness function with player count
      return validateFitnessFunction(fitnessFunction, context, playerCount);
    }
    
    return payoffValid;
  }
  
  /**
   * Get the number of players from the DTO
   * 
   * @param dto The GameTheoryProblemDto
   * @return The number of players
   */
  private int getPlayerCount(GameTheoryProblemDto dto) {
    return dto.getNormalPlayers() != null ? dto.getNormalPlayers().size() : 0;
  }
  
  /**
   * Finds the highest property index referenced (e.g., p1, p2, p10)
   * 
   * @param expression The payoff function expression to analyze
   * @return The highest property index found, or 5 if no variables found
   */
  private int findHighestPropertyVariable(String expression) {
    int highestIndex = 5;
    Matcher matcher = PROPERTY_PATTERN.matcher(expression);
    
    while (matcher.find()) {
      int propertyIndex = Integer.parseInt(matcher.group(1));
      highestIndex = Math.max(highestIndex, propertyIndex);
    }
    
    return highestIndex; // return the highest index found
  }
  
  /**
   * Check if a string is a default function
   * 
   * @param function Function name to check
   * @return true if it's a default function
   */
  private boolean isDefaultFunction(String function) {
    if (function == null || function.trim().isEmpty()) {
      return false;
    }
    
    String upperFunc = function.toUpperCase().trim();
    return upperFunc.equals(GameTheoryConst.DEFAULT_PAYOFF_FUNC.toUpperCase()) || 
           GameTheoryConst.AGGREGATION_FUNCTIONS.contains(upperFunc);
  }
  
  /**
   * check property indices (p1, p2, ...) in the expression
   * 
   * @param expression expression to check
   * @param maxPropertyCount maximum property count
   * @return set of invalid indices
   */
  private Set<Integer> checkPropertyIndices(String expression, int maxPropertyCount) {
    Set<Integer> invalidIndices = new HashSet<>();
    
    // Chuẩn bị cho việc phân tích
    char[] chars = expression.toCharArray();
    
    for (int i = 0; i < chars.length - 1; i++) {
      if (chars[i] == 'p' && Character.isDigit(chars[i + 1])) {
        StringBuilder indexStr = new StringBuilder();
        int j = i + 1;
        
        while (j < chars.length && Character.isDigit(chars[j])) {
          indexStr.append(chars[j]);
          j++;
        }
        try {
          int propertyIndex = Integer.parseInt(indexStr.toString());
          if (propertyIndex < 1 || propertyIndex > maxPropertyCount) {
            invalidIndices.add(propertyIndex);
          }
        } catch (NumberFormatException e) {
        }
      }
    }
    
    return invalidIndices;
  }
  
  /**
   * index of utility variables (u1, u2, ...) in the expression
   * 
   * @param expression expression to check
   * @param maxPlayerCount maximum player count
   * @return set of invalid indices
   */
  private Set<Integer> checkPlayerIndices(String expression, int maxPlayerCount) {
    Set<Integer> invalidIndices = new HashSet<>();

    char[] chars = expression.toCharArray();
    
    for (int i = 0; i < chars.length - 1; i++) {
      if (chars[i] == 'u' && Character.isDigit(chars[i + 1])) {
        StringBuilder indexStr = new StringBuilder();
        int j = i + 1;
        
        while (j < chars.length && Character.isDigit(chars[j])) {
          indexStr.append(chars[j]);
          j++;
        }

        try {
          int playerIndex = Integer.parseInt(indexStr.toString());
          if (playerIndex < 1 || playerIndex > maxPlayerCount) {
            invalidIndices.add(playerIndex);
          }
        } catch (NumberFormatException e) {
        }
      }
    }
    
    return invalidIndices;
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
          .addPropertyNode("payoffFunction")
          .addConstraintViolation();
      return false;
    }

    if (isDefaultFunction(value)) {
      return true;
    }
    
    // Check property index limit based on actual property count
    Set<Integer> invalidIndices = checkPropertyIndices(value, propertyCount);
    
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
          .addPropertyNode("payoffFunction")
          .addConstraintViolation();
      return false;
    }
    
    // First, perform detailed validation
    List<ValidationError> errors = validateDetailed(value);
    if (!errors.isEmpty()) {
      // Report the first error
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(errors.get(0).getMessage())
          .addPropertyNode("payoffFunction")
          .addConstraintViolation();
      return false;
    }

    if (!VALID_PATTERN.matcher(value).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid payoff function syntax")
          .addPropertyNode("payoffFunction")
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
            .addPropertyNode("payoffFunction")
            .addConstraintViolation();
        return false;
      }
      
      // Check for mathematical issues
      List<ValidationError> mathErrors = validateMathOperations(expression, variables);
      if (!mathErrors.isEmpty()) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(mathErrors.get(0).getMessage())
            .addPropertyNode("payoffFunction")
            .addConstraintViolation();
        return false;
      }
      
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          "Invalid payoff function syntax: '" + e.getMessage() + "'")
          .addPropertyNode("payoffFunction")
          .addConstraintViolation();
      return false;
    }
    
    return true;
  }
  
  /**
   * Validates a fitness function with player count validation
   * 
   * @param value The fitness function to validate
   * @param context Validator context
   * @param playerCount Maximum player count
   * @return true if valid, false otherwise
   */
  private boolean validateFitnessFunction(String value, ConstraintValidatorContext context, int playerCount) {
    if (value == null || value.trim().isEmpty()) {
      return true;
    }

    if (isDefaultFunction(value)) {
      return true;
    }
    
    // Check player indices
    Set<Integer> invalidPlayerIndices = checkPlayerIndices(value, playerCount);
    
    if (!invalidPlayerIndices.isEmpty()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          "Invalid fitness function: Variable " + 
          (invalidPlayerIndices.size() == 1 ? 
              "u" + invalidPlayerIndices.iterator().next() : 
              "variables " + formatInvalidPlayerIndices(invalidPlayerIndices)) + 
          " refers to non-existent player. Maximum player count is " + playerCount + 
          " (valid variables are u1 to u" + playerCount + ").")
          .addPropertyNode("fitnessFunction")
          .addConstraintViolation();
      return false;
    }
    
    String cleanFunc = value.replaceAll("\\s+", "");
    try {
      // Basic syntax validation with exp4j
      ExpressionBuilder builder = new ExpressionBuilder(cleanFunc);
      
      // Add u1, u2, ... variables
      for (int i = 1; i <= playerCount; i++) {
        builder.variable("u" + i);
      }
      
      // Try to build and validate
      Expression expression = builder.build();
      
      // Set all variables to 1.0 for validation
      for (int i = 1; i <= playerCount; i++) {
        expression.setVariable("u" + i, 1.0);
      }
      
      ValidationResult validationResult = expression.validate();
      if (!validationResult.isValid()) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Invalid fitness function syntax: '" + validationResult.getErrors().get(0) + "'")
            .addPropertyNode("fitnessFunction")
            .addConstraintViolation();
        return false;
      }
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          "Invalid fitness function syntax: '" + e.getMessage() + "'")
          .addPropertyNode("fitnessFunction")
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
  
  /**
   * Format a list of invalid player indices for error message
   */
  private String formatInvalidPlayerIndices(Set<Integer> indices) {
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (Integer idx : indices) {
      if (count > 0) {
        sb.append(count == indices.size() - 1 ? " and " : ", ");
      }
      sb.append("u").append(idx);
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

    // Check for missing operators between variables or after variables
    ValidationError missingOperatorError = checkMissingOperators(func);
    if (missingOperatorError != null) {
      errors.add(missingOperatorError);
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
   * Checks if operators are missing between variables or after variables in the expression.
   * This ensures that expressions like "p1p2" or "P1p1P2p2" are detected as invalid.
   *
   * @param func Function expression to check
   * @return ValidationError if missing operators found, null otherwise
   */
  private ValidationError checkMissingOperators(String func) {
    if (func == null || func.isEmpty()) {
      return null;
    }
    
    // Chuẩn bị cho việc phân tích
    char[] chars = func.toCharArray();
    boolean inVariable = false;
    boolean expectOperator = false;
    int variableStartPos = -1;
    
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      
      // Skip whitespace
      if (Character.isWhitespace(c)) {
        continue;
      }
      
      // Check if we're at the start of a possible player property variable (p1, P1p1)
      if (!inVariable && (c == 'p' || c == 'P') && i + 1 < chars.length && 
          (Character.isDigit(chars[i + 1]) || (c == 'P' && chars[i + 1] == 'p'))) {
        inVariable = true;
        variableStartPos = i;
        continue;
      }
      
      // If we're in a variable and find a digit or in a Player-property (P1p1) pattern, continue
      if (inVariable && (Character.isDigit(c) || (c == 'p' && chars[variableStartPos] == 'P'))) {
        continue;
      }
      
      // If we were in a variable but now find something else, check if it's the end of a variable
      if (inVariable && !Character.isDigit(c) && !(chars[variableStartPos] == 'P' && c == 'p')) {
        inVariable = false;
        expectOperator = true;
        
        // If the current character is a variable start (p, P) but we're expecting an operator,
        // it means we have variables next to each other without an operator
        if ((c == 'p' || c == 'P') && i + 1 < chars.length && 
            (Character.isDigit(chars[i + 1]) || (c == 'P' && i + 2 < chars.length && chars[i + 1] == 'p'))) {
          return new ValidationError(
              "Invalid syntax: Missing operator between variables at position " + i + 
              " in '" + func + "'. Variables must be separated by operators.", 
              func);
        }
      }
      
      // If we're expecting an operator, check if we got one
      if (expectOperator) {
        if (c == '+' || c == '-' || c == '*' || c == '/' || c == ')' || c == ',' || c == '^') {
          expectOperator = false;
        } else if (c == '(') {
          // Opening parenthesis after a variable requires an implicit multiplication operator
          // which is not allowed in our syntax
          return new ValidationError(
              "Invalid syntax: Missing operator between variable and '(' at position " + i + 
              " in '" + func + "'. Implicit multiplication is not supported.", 
              func);
        } else if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
          return new ValidationError(
              "Invalid syntax: Expected operator after variable at position " + i + 
              " in '" + func + "'.", 
              func);
        }
      }
    }
    
    return null;
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