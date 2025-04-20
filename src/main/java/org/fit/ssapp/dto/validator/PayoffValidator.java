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
  //pattern to match property
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("(P[0-9]+)?(p([0-9]+))");

  // Pattern for matching all built-in functions from AppConst
  private static final String FUNCTION_NAMES_REGEX = AppConst.BUILTIN_FUNCTION_NAMES.stream()
      .collect(Collectors.joining("|"));

  private static final Pattern VALID_PATTERN = Pattern.compile(
      "^[\\s]*([pP]\\d+|P\\d+p\\d+|[\\d.]+|[+\\-*/()\\s]|" + FUNCTION_NAMES_REGEX + "|SUM|AVERAGE|MIN|MAX|PRODUCT|MEDIAN|RANGE)+[\\s]*$",
      Pattern.CASE_INSENSITIVE
  );

  // Pattern to match function with arguments - using all built-in functions from AppConst
  private static final Pattern FUNCTION_ARGS_PATTERN = Pattern.compile(
      "(" + FUNCTION_NAMES_REGEX + ")\\(([^()]*)\\)"
  );

  // [a-zA-Z0-9] - match all alphanumeric characters plus math operators and punctuation
  private static final Pattern VALID_CHAR_PATTERN = Pattern.compile("[a-zA-Z0-9+\\-*/()^%,. ]");

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
        return isValidString(expression, context, maxPropertyIndex, 0);
      }
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid data type for payoff function validation")
          .addPropertyNode("defaultPayoffFunction")
          .addConstraintViolation();
      return false;
    }

    // Get payoff function from DTO
    String payoffFunction = dto.getDefaultPayoffFunction();

    int maxPropertyCount = 0; // default value
    int playerCount = 0; // default value
    
    if (dto.getNormalPlayers() != null && !dto.getNormalPlayers().isEmpty()) {
      playerCount = dto.getNormalPlayers().size();
      
      var firstPlayer = dto.getNormalPlayers().get(0);
      if (firstPlayer.getStrategies() != null && !firstPlayer.getStrategies().isEmpty()) {
        var firstStrategy = firstPlayer.getStrategies().get(0);
        if (firstStrategy.getProperties() != null) {
          maxPropertyCount = firstStrategy.getProperties().size();
        }
      }
    }
    return isValidString(payoffFunction, context, maxPropertyCount, playerCount);
  }

  /**
   * Finds the highest property index referenced (e.g., p1, p2, p10)
   *
   * @param expression The payoff function expression to analyze
   * @return The highest property index found, or 5 if no variables found
   */
  private int findHighestPropertyVariable(String expression) {
    int highestIndex = 5;
    Matcher matcher = VARIABLE_PATTERN.matcher(expression);

    while (matcher.find()) {
      int propertyIndex = Integer.parseInt(matcher.group(3)); // group3 is the number after p
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
    Matcher matcher = VARIABLE_PATTERN.matcher(expression);
    
    while (matcher.find()) {
      try {
        int propertyIndex = Integer.parseInt(matcher.group(3));
          if (propertyIndex < 1 || propertyIndex > maxPropertyCount) {
            invalidIndices.add(propertyIndex);
          }
        } catch (NumberFormatException e) {
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
   * @param playerCount Maximum player count available
   * @return true if the expression is valid, false otherwise
   */
  private boolean isValidString(String value, ConstraintValidatorContext context, int propertyCount, int playerCount) {
    if (value == null || value.trim().isEmpty()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid expression: Empty expression")
          .addPropertyNode("defaultPayoffFunction")
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
          .addPropertyNode("defaultPayoffFunction")
          .addConstraintViolation();
      return false;
    }

    // check if there is any player index in the expression
    if (playerCount > 0) {
      Set<Integer> invalidPlayerIndices = new HashSet<>();
      
      Pattern playerPropertyPattern = Pattern.compile("P(\\d+)p\\d+");
      Matcher matcher = playerPropertyPattern.matcher(value);
      
      while (matcher.find()) {
        try {
          int playerIndex = Integer.parseInt(matcher.group(1));
          //check if the player index is valid
          if (playerIndex < 1 || playerIndex > playerCount) {
            invalidPlayerIndices.add(playerIndex);
          }
        } catch (NumberFormatException e) {
        }
      }
      if (!invalidPlayerIndices.isEmpty()) {
        context.disableDefaultConstraintViolation();
        StringBuilder errorMsg = new StringBuilder("Invalid payoff function: Player ");
        
        if (invalidPlayerIndices.size() == 1) {
          errorMsg.append("P").append(invalidPlayerIndices.iterator().next());
        } else {
          int count = 0;
          for (Integer idx : invalidPlayerIndices) {
            if (count > 0) {
              errorMsg.append(count == invalidPlayerIndices.size() - 1 ? " and " : ", ");
            }
            errorMsg.append("P").append(idx);
            count++;
          }
        }
        errorMsg.append(" exceeds available players. Maximum player count is ").append(playerCount)
               .append(" (valid players are P1 to P").append(playerCount).append(")");
        
        context.buildConstraintViolationWithTemplate(errorMsg.toString())
            .addPropertyNode("defaultPayoffFunction")
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
          .addPropertyNode("defaultPayoffFunction")
          .addConstraintViolation();
      return false;
    }

    if (!VALID_PATTERN.matcher(value).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid payoff function syntax")
          .addPropertyNode("defaultPayoffFunction")
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
            .addPropertyNode("defaultPayoffFunction")
            .addConstraintViolation();
        return false;
      }

      // Mathematical errors (like division by zero, log of negative numbers) 
      // will be caught at runtime instead of pre-validation

    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Invalid payoff function syntax: '" + e.getMessage() + "'")
          .addPropertyNode("defaultPayoffFunction")
          .addConstraintViolation();
      return false;
    }

    return true;
  }

  private List<ValidationError> validateDetailed(String func) {
    List<ValidationError> errors = new ArrayList<>();

    // Check for empty function
    if (func.trim().isEmpty()) {
      errors.add(new ValidationError("Invalid expression: Empty expression", func));
      return errors;
    }

    // Check for invalid characters using regex pattern
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
      
      buffer.append(c);
      currentTokenType = charType;
    }
    

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
    } else if ((c == 'p' || c == 'P') && position + 1 < expression.length() && 
              (Character.isDigit(expression.charAt(position + 1)) || 
               (c == 'P' && expression.charAt(position + 1) == 'p'))) {
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
        int startArgPos = j;
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
          return new ValidationError("Invalid function: Function '" + funcName + "' does not exist in '" + expression + "'", expression);
        }
        
        // Check empty arguments
        if (args.isEmpty() || (args.size() == 1 && args.get(0).trim().isEmpty())) {
          return new ValidationError("Invalid function syntax: Missing argument for " + funcName + " function in '" + funcName + "()'", expression);
        }
        
        // Check argument count
        int expectedArgCount = FUNCTION_ARGS_COUNT.get(funcName);
        if (args.size() != expectedArgCount) {
          return new ValidationError(
              "Invalid function syntax: " + funcName + " requires " + expectedArgCount +
              " argument(s), but found " + args.size() + " in '" + funcName + "(" + String.join(",", args) + ")'", 
              expression);
        }
      }
    }
    
    return null;
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
}