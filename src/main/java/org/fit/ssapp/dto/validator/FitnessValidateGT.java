package org.fit.ssapp.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Collections;
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

  // Pattern to match standard mathematical functions
  private static final Pattern FUNCTION_PATTERN = Pattern.compile("(abs|sqrt|log|exp|ceil|floor|sin|cos|tan)\\(");
  
  // Pattern to match player 
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(u([0-9]+))");
  
  // Pattern to match function with arguments
  private static final Pattern FUNCTION_ARGS_PATTERN = Pattern.compile("(abs|sqrt|log|exp|ceil|floor|sin|cos|tan)\\(([^()]*)\\)");

  // [a-zA-Z0-9] - match all alphanumeric characters plus math operators and punctuation
  private static final Pattern VALID_CHAR_PATTERN = Pattern.compile("[a-zA-Z0-9+\\-*/()^%,. ]");

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
          .addPropertyNode("fitnessFunction")
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
    int highestIndex = 1;
    // Using the non-regex method to find player indices
    Set<Integer> indices = findPlayerIndices(expression);

    // Find the highest index
    for (Integer idx : indices) {
      highestIndex = Math.max(highestIndex, idx);
    }

    return highestIndex;
  }

  /**
   * Checks if a string is a default fitness function
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
   * Finds all player indices in the expression using regex pattern
   *
   * @param expression Expression to analyze
   * @return Set of player indices found
   */
  private Set<Integer> findPlayerIndices(String expression) {
    Set<Integer> indices = new HashSet<>();
    if (expression == null || expression.isEmpty()) {
      return indices;
    }
    
    Matcher matcher = VARIABLE_PATTERN.matcher(expression);
    while (matcher.find()) {
      try {
        // Group 2 is the number after 'u'
        int playerIndex = Integer.parseInt(matcher.group(2));
        indices.add(playerIndex);
      } catch (NumberFormatException e) {
        // Skip if parsing fails
      }
    }

    return indices;
  }

  /**
   * Checks if player indices in the expression exceed the maximum allowed
   *
   * @param expression Expression to check
   * @param maxPlayerCount Maximum number of players allowed
   * @return Set of invalid player indices
   */
  private Set<Integer> checkInvalidPlayerIndices(String expression, int maxPlayerCount) {
    Set<Integer> invalidIndices = new HashSet<>();

    // Find all player indices
    Set<Integer> allIndices = findPlayerIndices(expression);

    // Check which ones exceed the limit
    for (Integer index : allIndices) {
      if (index < 1 || index > maxPlayerCount) {
        invalidIndices.add(index);
      }
    }

    return invalidIndices;
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
          .addPropertyNode("fitnessFunction")
          .addConstraintViolation();
      return false;
    }

    if (value.equalsIgnoreCase(GameTheoryConst.DEFAULT_PAYOFF_FUNC) || isDefaultFunction(value)) {
      return true;
    }

    // Pre-check for direct division by zero pattern
    if (value.matches(".*\\/\\s*0[^\\d].*") || value.matches(".*\\/\\s*0$")) {
      context.disableDefaultConstraintViolation();
      int divZeroPos = value.indexOf("/0");
      if (divZeroPos == -1) {
        divZeroPos = value.indexOf("/ 0");
        if (divZeroPos == -1) {
          divZeroPos = 0;
        }
      }
      context.buildConstraintViolationWithTemplate("Invalid syntax: Division by zero detected at position " + divZeroPos + " in '" + value + "'")
          .addPropertyNode("fitnessFunction")
          .addConstraintViolation();
      return false;
    }

    // Check player index limit based on actual player count
    Set<Integer> invalidIndices = checkInvalidPlayerIndices(value, playerCount);

    if (!invalidIndices.isEmpty()) {
      context.disableDefaultConstraintViolation();
      
      StringBuilder errorMsg = new StringBuilder();
      
      if (invalidIndices.size() == 1) {
        Integer invalidIdx = invalidIndices.iterator().next();
        String varPattern = "u" + invalidIdx;
        int varPos = value.indexOf(varPattern);
        errorMsg.append("Invalid fitness function: Variable ").append(varPattern)
               .append(" at position ").append(varPos)
               .append(" refers to non-existent player. The request contains only ").append(playerCount).append(" players.");
      } else {
        errorMsg.append("Invalid fitness function: Variable variables ")
               .append(formatInvalidIndices(invalidIndices))
               .append(" refers to non-existent player. The request contains only ").append(playerCount).append(" players.");
      }
      
      context.buildConstraintViolationWithTemplate(errorMsg.toString())
          .addPropertyNode("fitnessFunction")
          .addConstraintViolation();
      return false;
    }

    // First, perform detailed validation
    List<ValidationError> errors = validateDetailed(value);
    if (!errors.isEmpty()) {
      // Report the first error
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(errors.get(0).getMessage())
          .addPropertyNode("fitnessFunction")
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
        
        String errorMessage = validationResult.getErrors().get(0);
        if (errorMessage.startsWith("Not enough arguments for")) {
          String funcName = errorMessage.substring("Not enough arguments for '".length(), errorMessage.length() - 1);
          int position = value.indexOf(funcName + "()");
          if (position == -1) {
            position = value.indexOf(funcName + "( )");
            if (position == -1) {
              position = value.indexOf(funcName + "(");
            }
          }
          
          context.buildConstraintViolationWithTemplate(
                  "Invalid function syntax: Missing argument for " + funcName + " function at position " + position + " in '" + funcName + "()'")
              .addPropertyNode("fitnessFunction")
              .addConstraintViolation();
        } else if (errorMessage.startsWith("Unknown function or variable")) {
          Matcher funcMatcher = Pattern.compile("Unknown function or variable '([^']+)' at pos (\\d+)").matcher(errorMessage);
          if (funcMatcher.find()) {
            String funcName = funcMatcher.group(1);
            int position = Integer.parseInt(funcMatcher.group(2));
            
            context.buildConstraintViolationWithTemplate(
                    "Invalid function: Function '" + funcName + "' at position " + position + " does not exist in '" + value + "'")
                .addPropertyNode("fitnessFunction")
                .addConstraintViolation();
          } else {
            context.buildConstraintViolationWithTemplate(
                    "Invalid fitness function syntax: '" + errorMessage + "'")
                .addPropertyNode("fitnessFunction")
                .addConstraintViolation();
          }
        } else {
          context.buildConstraintViolationWithTemplate(
                  "Invalid fitness function syntax: '" + errorMessage + "'")
              .addPropertyNode("fitnessFunction")
              .addConstraintViolation();
        }
        return false;
      }

      // will be caught at runtime instead of pre-validation
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
   * Format invalid indices for error message
   *
   * @param indices Set of invalid indices
   * @return Formatted string of invalid indices
   */
  private String formatInvalidIndices(Set<Integer> indices) {
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

    // invalid char
    for (int i = 0; i < func.length(); i++) {
      char c = func.charAt(i);
      if (!VALID_CHAR_PATTERN.matcher(String.valueOf(c)).matches()) {
        errors.add(new ValidationError("Invalid character: Unrecognized character '" + c + "' at position " + i + " in '" + func + "'", func));
        break;
      }
    }

    // Tokenize the expression
    List<Token> tokens = tokenize(func);
    
    // Validate parentheses using tokens
    ValidationError parenError = validateParentheses(tokens, func);
    if (parenError != null) {
      errors.add(parenError);
      return errors;
    }
    
    // Validate operator usage
    ValidationError operatorError = validateOperators(tokens, func);
    if (operatorError != null) {
      errors.add(operatorError);
    }

    // Validate function arguments
    ValidationError functionError = validateFunctionArguments(tokens, func);
    if (functionError != null) {
      errors.add(functionError);
    }

    return errors;
  }

  // Token types for expression parsing
  private enum TokenType {
    NUMBER, VARIABLE, OPERATOR, FUNCTION, OPEN_PAREN, CLOSE_PAREN, COMMA, WHITESPACE
  }

  // Token class to hold token information
  private static class Token {
    private final TokenType type;
    private final String value;
    private final int position;

    public Token(TokenType type, String value, int position) {
      this.type = type;
      this.value = value;
      this.position = position;
    }
  }

  // Tokenize the expression into a list of tokens
  private List<Token> tokenize(String expression) {
    List<Token> tokens = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();
    TokenType currentTokenType = null;
    
    // Define lists of single-character tokens that should be processed immediately
    List<Character> singleCharTokens = new ArrayList<>();
    Collections.addAll(singleCharTokens, '+', '-', '*', '/', '(', ')', ',', '^', '%');
    
    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);
      
      // Handle whitespace
      if (Character.isWhitespace(c)) {
        if (buffer.length() > 0) {
          addToken(tokens, currentTokenType, buffer.toString(), i - buffer.length());
          buffer = new StringBuilder();
          currentTokenType = null;
        }
        continue;
      }
      
      // Determine token type for current character
      TokenType charType = getTokenType(c, expression, i);
      
      // Special cases for single-character tokens (operators, parentheses, comma)
      if (singleCharTokens.contains(c)) {
        // If there's a token in the buffer, add it first
        if (buffer.length() > 0) {
          addToken(tokens, currentTokenType, buffer.toString(), i - buffer.length());
          buffer = new StringBuilder();
        }
        
        // Add the single-character token
        tokens.add(new Token(charType, String.valueOf(c), i));
        currentTokenType = null;
        continue;
      }
      
      // If token type changes, add the current token and start a new one
      if (currentTokenType != null && charType != currentTokenType) {
        addToken(tokens, currentTokenType, buffer.toString(), i - buffer.length());
        buffer = new StringBuilder();
      }
      
      // Add character to current buffer
      buffer.append(c);
      currentTokenType = charType;
    }
    
    // Add final token if buffer is not empty
    if (buffer.length() > 0) {
      addToken(tokens, currentTokenType, buffer.toString(), expression.length() - buffer.length());
    }
    
    return tokens;
  }
  
  // Helper method to add a token to the list
  private void addToken(List<Token> tokens, TokenType type, String value, int position) {
    if (type != null) {
      tokens.add(new Token(type, value, position));
    }
  }
  
  // Determine token type for a character
  private TokenType getTokenType(char c, String expression, int position) {
    // Define lists of characters for each type
    List<Character> operators = new ArrayList<>();
    Collections.addAll(operators, '+', '-', '*', '/', '^', '%');
    
    List<Character> punctuation = new ArrayList<>();
    Collections.addAll(punctuation, '(', ')', ',');
    
    if (Character.isDigit(c) || c == '.') {
      return TokenType.NUMBER;
    } else if (c == 'u' && position + 1 < expression.length() && 
               Character.isDigit(expression.charAt(position + 1))) {
      return TokenType.VARIABLE;
    } else if (c == '(') {
      return TokenType.OPEN_PAREN;
    } else if (c == ')') {
      return TokenType.CLOSE_PAREN;
    } else if (c == ',') {
      return TokenType.COMMA;
    } else if (operators.contains(c)) {
      return TokenType.OPERATOR;
    } else if (Character.isLetter(c)) {
      // Check if this might be a function
      StringBuilder potentialFunction = new StringBuilder();
      potentialFunction.append(c);
      
      int j = position + 1;
      while (j < expression.length() && Character.isLetter(expression.charAt(j))) {
        potentialFunction.append(expression.charAt(j));
        j++;
      }
      
      String func = potentialFunction.toString().toLowerCase();
      if (FUNCTION_ARGS_COUNT.containsKey(func)) {
        return TokenType.FUNCTION;
      }
      
      return TokenType.VARIABLE;
    }
    
    return TokenType.WHITESPACE;
  }
  
  // Validate parentheses using tokens
  private ValidationError validateParentheses(List<Token> tokens, String expression) {
    Stack<Integer> parenStack = new Stack<>();
    
    for (Token token : tokens) {
      if (token.type == TokenType.OPEN_PAREN) {
        parenStack.push(token.position);
      } else if (token.type == TokenType.CLOSE_PAREN) {
        if (parenStack.isEmpty()) {
          return new ValidationError("Invalid syntax: Extra closing parenthesis at position " + token.position + " in '" + expression + "'", expression);
        }
        parenStack.pop();
      }
    }
    
    if (!parenStack.isEmpty()) {
      int position = parenStack.peek();
      return new ValidationError("Invalid syntax: Unclosed opening parenthesis at position " + position + " in '" + expression + "'", expression);
    }
    
    return null;
  }
  
  // Validate operator usage
  private ValidationError validateOperators(List<Token> tokens, String expression) {
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      
      // Check for variables next to each other without operators
      if (token.type == TokenType.VARIABLE && i > 0) {
        Token prevToken = tokens.get(i - 1);
        if (prevToken.type == TokenType.VARIABLE) {
          return new ValidationError(
              "Invalid syntax: Missing operator between variables at position " + token.position +
              " in '" + expression + "'. Variables must be separated by operators.",
              expression);
        }
        
        // Check for variables followed by open parentheses without an operator
        if (prevToken.type == TokenType.VARIABLE && token.type == TokenType.OPEN_PAREN) {
          return new ValidationError(
              "Invalid syntax: Missing operator between variable and '(' at position " + token.position +
              " in '" + expression + "'. Implicit multiplication is not supported.",
              expression);
        }
      }
      
      // Two operators in a row is invalid (exception: unary minus can follow another operator)
      if (token.type == TokenType.OPERATOR && i > 0) {
        Token prevToken = tokens.get(i - 1);
        List<String> allowedBeforeMinus = new ArrayList<>();
        Collections.addAll(allowedBeforeMinus, "+", "-", "*", "/", "^");
        
        if (prevToken.type == TokenType.OPERATOR && 
            !(token.value.equals("-") && allowedBeforeMinus.contains(prevToken.value))) {
          return new ValidationError(
              "Invalid syntax: Two operators in a row at position " + token.position +
              " in '" + expression + "'.",
              expression);
        }
      }
    }
    
    return null;
  }
  
  // Validate function arguments
  private ValidationError validateFunctionArguments(List<Token> tokens, String expression) {
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      
      if (token.type == TokenType.FUNCTION && i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.OPEN_PAREN) {
        String funcName = token.value.toLowerCase();
        
        // Find matching closing parenthesis
        int openCount = 1;
        int j = i + 2;
        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        
        while (j < tokens.size() && openCount > 0) {
          Token current = tokens.get(j);
          
          if (current.type == TokenType.OPEN_PAREN) {
            openCount++;
            currentArg.append(current.value);
          } else if (current.type == TokenType.CLOSE_PAREN) {
            openCount--;
            if (openCount > 0) {
              currentArg.append(current.value);
            } else if (currentArg.length() > 0) {
              args.add(currentArg.toString());
            }
          } else if (current.type == TokenType.COMMA && openCount == 1) {
            args.add(currentArg.toString());
            currentArg = new StringBuilder();
          } else {
            currentArg.append(current.value);
          }
          
          j++;
        }
        // Check if function exists
        if (!FUNCTION_ARGS_COUNT.containsKey(funcName)) {
          return new ValidationError("Invalid function: Function '" + funcName + "' at position " + token.position + " does not exist in '" + expression + "'", expression);
        }
        
        // Check empty arguments
        if (args.isEmpty() || (args.size() == 1 && args.get(0).trim().isEmpty())) {
          return new ValidationError("Invalid function syntax: Missing argument for " + funcName + " function at position " + token.position + " in '" + funcName + "()'", expression);
        }
        
        // Check argument count
        int expectedArgCount = FUNCTION_ARGS_COUNT.get(funcName);
        if (args.size() != expectedArgCount) {
          return new ValidationError(
              "Invalid payoff function syntax: Function " + funcName + " at position " + token.position + " requires " + expectedArgCount +
              " argument(s), but found " + args.size() + " in '" + funcName + "(" + String.join(",", args) + ")'", 
              expression);
        }
      }
    }
    return null;
  }

  /**
   * Extracts valid variable names from a mathematical function.
   * Uses regex pattern to identify variables (u1, u2, etc).
   *
   * @param func The mathematical function as a string.
   * @return A set of valid variable names found in the function.
   */
  private Set<String> extractVariables(String func) {
    Set<String> variables = new HashSet<>();

    if (func == null || func.isEmpty()) {
      return variables;
    }

    Matcher matcher = VARIABLE_PATTERN.matcher(func);
    while (matcher.find()) {
      //group1 is the complete variable (u1, u2, ...)
      variables.add(matcher.group(1));
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
