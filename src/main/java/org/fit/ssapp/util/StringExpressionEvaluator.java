package org.fit.ssapp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import net.objecthunter.exp4j.function.Function;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;

/**
 * A utility class to evaluate mathematical expressions in string format. Supports variable
 * replacement, basic arithmetic operations, and trigonometric functions.
 */
public class StringExpressionEvaluator {

  public static Pattern nonRelativePattern = Pattern.compile("p[0-9]+");
  public static Pattern fitnessPattern = Pattern.compile("u([1-9]\\d*)");
//  public static Pattern fitnessPattern = Pattern.compile("u[0-9]+");

  /**
   * Evaluates a payoff function relative to other players.
   */
  public enum DefaultFunction {
    SUM, AVERAGE, MIN, MAX, PRODUCT, MEDIAN, RANGE
  }

  static DecimalFormat decimalFormat = new DecimalFormat("#.##############");

  /**
   * Evaluates a payoff function relative to other players.
   *
   * @param strategy              The strategy of the current player.
   * @param payoffFunction        The payoff function as a string.
   * @param normalPlayers         List of all normal players.
   * @param chosenStrategyIndices Indices of chosen strategies.
   * @return The calculated payoff as a {@code BigDecimal}.
   */
  public static BigDecimal evaluatePayoffFunctionWithRelativeToOtherPlayers(Strategy strategy,
                                                                            String payoffFunction,
                                                                            List<NormalPlayer> normalPlayers,
                                                                            int[] chosenStrategyIndices) {
    if (payoffFunction == null || payoffFunction.isBlank() || payoffFunction.equalsIgnoreCase("DEFAULT")) {
      // the payoff function is the sum function of all properties by default
      return calculateByDefault(strategy.getProperties(), "SUM");
    }

    if (checkIfIsDefaultFunction(payoffFunction)) {
      return calculateByDefault(strategy.getProperties(), payoffFunction);
    }

    // Validate payoff function syntax
    try {
      String expression = payoffFunction;
      Pattern generalPattern = Pattern.compile("(P[0-9]+)?" + nonRelativePattern.pattern());
      Matcher generalMatcher = generalPattern.matcher(expression);
      
      while (generalMatcher.find()) {
        String placeholder = generalMatcher.group();
        if (placeholder.contains("P")) {
          // relative variables - syntax Pjpi with j the player index, and i the property index
          int[] ji = Arrays.stream(placeholder
                          .substring(1) // remove P
                          .split("p")) // split at p
                  .mapToInt(Integer::parseInt)
                  .map(x -> x - 1)
                  .toArray(); // [j, i]
          
          // Validate player index
          if (ji[0] < 0 || ji[0] >= normalPlayers.size()) {
            throw new IllegalArgumentException("Invalid player index: " + (ji[0] + 1));
          }
          
          NormalPlayer otherPlayer = normalPlayers.get(ji[0]);
          Strategy otherPlayerStrategy = otherPlayer.getStrategyAt(chosenStrategyIndices[ji[0]]);
          
          // Validate property index
          if (ji[1] < 0 || ji[1] >= otherPlayerStrategy.getProperties().size()) {
            throw new IllegalArgumentException("Invalid property index: " + (ji[1] + 1) + " for player " + (ji[0] + 1));
          }
          
          double propertyValue = otherPlayerStrategy.getProperties().get(ji[1]);
          expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
        } else {
          // non-relative variables
          int index = Integer.parseInt(placeholder.substring(1)) - 1;
          if (index < 0 || index >= strategy.getProperties().size()) {
            throw new IllegalArgumentException("Invalid property index: " + (index + 1));
          }
          double propertyValue = strategy.getProperties().get(index);
          expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
        }
      }

      // Try to evaluate the expression to validate syntax
      evaluateExpression(expression);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid payoff function: " + payoffFunction, e);
    }

    String expression = payoffFunction;
    Pattern generalPattern = Pattern.compile("(P[0-9]+)?" + nonRelativePattern.pattern());
    Matcher generalMatcher = generalPattern.matcher(expression);
    
    while (generalMatcher.find()) {
      String placeholder = generalMatcher.group();
      if (placeholder.contains("P")) {
        int[] ji = Arrays.stream(placeholder
                        .substring(1)
                        .split("p"))
                .mapToInt(Integer::parseInt)
                .map(x -> x - 1)
                .toArray();
        NormalPlayer otherPlayer = normalPlayers.get(ji[0]);
        Strategy otherPlayerStrategy = otherPlayer.getStrategyAt(chosenStrategyIndices[ji[0]]);
        double propertyValue = otherPlayerStrategy.getProperties().get(ji[1]);
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      } else {
        int index = Integer.parseInt(placeholder.substring(1)) - 1;
        double propertyValue = strategy.getProperties().get(index);
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      }
    }

    double val = evaluateExpression(expression);
    return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP);
  }

  /**
   * Evaluates a payoff function without relative variables.
   *
   * @param strategy       The strategy containing properties used in the function.
   * @param payoffFunction The payoff function as a string.
   * @return A {@code BigDecimal} result of the evaluated function.
   * @throws IllegalArgumentException If the function contains invalid variables.
   */
  public static BigDecimal evaluatePayoffFunctionNoRelative(Strategy strategy,
                                                            String payoffFunction) {

    if (payoffFunction == null || payoffFunction.isBlank() || payoffFunction.equalsIgnoreCase("DEFAULT")) {
      // the payoff function is the sum function of all properties by default
      return calculateByDefault(strategy.getProperties(), "SUM");
    }

    if (checkIfIsDefaultFunction(payoffFunction)) {
      return calculateByDefault(strategy.getProperties(), payoffFunction);
    }

    // Validate payoff function syntax
    try {
      String expression = payoffFunction;
      
      Matcher nonRelativeMatcher = nonRelativePattern.matcher(expression);
      while (nonRelativeMatcher.find()) {
        String placeholder = nonRelativeMatcher.group();
        int index = Integer.parseInt(placeholder.substring(1)) - 1;
        if (index < 0 || index >= strategy.getProperties().size()) {
          throw new IllegalArgumentException("Invalid property index: " + (index + 1));
        }
        double propertyValue = strategy.getProperties().get(index);
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      }

      // Handle relative variables
      Matcher relativeMatcher = Pattern.compile("P([0-9]+)p([0-9]+)").matcher(expression);
      while (relativeMatcher.find()) {
        String placeholder = relativeMatcher.group();
        expression = expression.replaceAll(placeholder, "1.0");
      }

      evaluateExpression(expression);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid payoff function: " + payoffFunction, e);
    }

    // If validation passed, evaluate the actual expression
    String expression = payoffFunction;
    Matcher nonRelativeMatcher = nonRelativePattern.matcher(expression);
    while (nonRelativeMatcher.find()) {
      String placeholder = nonRelativeMatcher.group();
      int index = Integer.parseInt(placeholder.substring(1)) - 1;
      double propertyValue = strategy.getProperties().get(index);
      expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
    }

    double val = evaluateExpression(expression);
    return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP);
  }


  /**
   * Evaluates the fitness function based on given payoffs.
   *
   * @param payoffs         Array of payoff values.
   * @param fitnessFunction The fitness function as a string.
   * @return The computed fitness value as a {@code BigDecimal}.
   * @throws IllegalArgumentException If the function contains invalid variables.
   */
  public static BigDecimal evaluateFitnessValue(double[] payoffs, String fitnessFunction) {
    if (fitnessFunction == null || fitnessFunction.isBlank() || fitnessFunction.equalsIgnoreCase("DEFAULT")) {
      // if the fitnessFunction is absent or DEFAULT,
      // the fitness value is the average of all payoffs of all chosen strategies by default
      List<Double> payoffList = new ArrayList<>();
      for (double payoff : payoffs) {
        payoffList.add(payoff);
      }
      return calculateByDefault(payoffList, "AVERAGE");
    }

    if (checkIfIsDefaultFunction(fitnessFunction)) {
      List<Double> payoffList = new ArrayList<>();
      for (double payoff : payoffs) {
        payoffList.add(payoff);
      }
      return calculateByDefault(payoffList, fitnessFunction);
    }

    try {
      String expression = fitnessFunction;
      Matcher fitnessMatcher = fitnessPattern.matcher(expression);
      while (fitnessMatcher.find()) {
        String placeholder = fitnessMatcher.group();
        // indices should account for offset from base 1 index of variables
        int index = Integer.parseInt(placeholder.substring(1)) - 1;
        if (index >= payoffs.length) {
          throw new IllegalArgumentException("Invalid payoff index: " + (index + 1) + ". Maximum allowed index is " + payoffs.length);
        }
        double propertyValue = payoffs[index];
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      }

      double val = evaluateExpression(expression);
      return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid fitness function: " + fitnessFunction, e);
    }
  }

  /**
   * Finds the length of a numeric token after a given index in a string.
   *
   * @param function   The input string containing numbers.
   * @param startIndex The starting index to check after.
   * @return The length of the numeric sequence following the startIndex.
   */
  public static int afterTokenLength(String function, int startIndex) {
    int length = 0;
    for (int c = startIndex + 1; c < function.length(); c++) {
      char ch = function.charAt(c);
      if (isNumericValue(ch)) {
        length++;
      } else {
        return length;
      }
    }
    return length;
  }

  /**
   * Checks if a character represents a numeric digit.
   *
   * @param c The character to check.
   * @return {@code true} if the character is a digit, otherwise {@code false}.
   */
  public static boolean isNumericValue(char c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Converts a double to a string without scientific notation, ensuring proper formatting.
   *
   * @param value The double value to convert.
   * @return A string representation of the value without scientific notation.
   */
  public static String convertToStringWithoutScientificNotation(double value) {
    String stringValue;
    if (value > 9999999) {
      stringValue = String.format("%.15f", value);
    } else {
      stringValue = Double.toString(value);
    }
    stringValue = stringValue.replaceAll("0*$", "").replaceAll(",", ".");
    return stringValue;
  }

  private static String formatDouble(double propertyValue) {
    return decimalFormat.format(propertyValue);
  }

  private static boolean checkIfIsDefaultFunction(String function) {
    return Arrays
            .stream(DefaultFunction.values())
            .anyMatch(f -> f.name().equalsIgnoreCase(function));
  }

  private static double calSum(List<Double> values) {
    return values.stream().mapToDouble(Double::doubleValue).sum();
  }

  private static double calProduct(List<Double> values) {
    return values.stream().reduce(1.0, (a, b) -> a * b);
  }

  private static double calMax(List<Double> values) {
    return values.stream()
        .mapToDouble(Double::doubleValue)
        .max()
        .orElseThrow(() -> new IllegalArgumentException("Cannot calculate maximum of empty list"));
  }

  private static double calMin(List<Double> values) {
    return values.stream()
        .mapToDouble(Double::doubleValue)
        .min()
        .orElseThrow(() -> new IllegalArgumentException("Cannot calculate minimum of empty list"));
  }

  private static double calAverage(List<Double> values) {
    return values.stream().mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);
  }

  private static double calMedian(List<Double> values) {
    double[] arr = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
    int n = arr.length;
    if (n % 2 == 0) {
      return (arr[n / 2] + arr[n / 2 - 1]) / 2;
    } else {
      return arr[n / 2];
    }
  }

  private static double calRange(List<Double> values) {
    double[] arr = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
    return arr[arr.length - 1] - arr[0];
  }

  public static BigDecimal calculateByDefault(List<Double> values, String defaultFunction) {
    DefaultFunction function = (!StringUtils.isEmptyOrNull(defaultFunction))
            ? DefaultFunction.valueOf(defaultFunction.toUpperCase()) : DefaultFunction.AVERAGE;
    double val = switch (function) {
      case PRODUCT -> calProduct(values);
      case MAX -> calMax(values);
      case MIN -> calMin(values);
      case AVERAGE -> calAverage(values);
      case MEDIAN -> calMedian(values);
      case RANGE -> calRange(values);
      default -> calSum(values);
    };

    return new BigDecimal(val);
  }

  /**
   * Evaluates a mathematical string expression.
   *
   * @param expression The expression to evaluate.
   * @return The computed result as a double.
   */
  private static double evaluateExpression(String expression) {
    try {
      // Replace NaN with 0
      String formattedExpression = expression.replaceAll("NaN", "0")
              .replaceAll("\\s+", "") // Remove all whitespace characters
              .replaceAll(",", ".");  // Replace , to . (default double decimal separator)
      Expression expr = getExpression(formattedExpression);

      // validate the expression
      ValidationResult validationResult = expr.validate();
      if (!validationResult.isValid()) {
        throw new RuntimeException("Invalid expression: " + validationResult.getErrors().toString());
      }

      // eva the expression
      return expr.evaluate();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid expression: " + expression, e);
    }
  }

  private static Expression getExpression(String formattedExpression) {
    ExpressionBuilder builder = new ExpressionBuilder(formattedExpression);

    Function logFunction = new Function("logb", 2) {
      @Override
      public double apply(double... args) {
        if (args[0] <= 0 || args[1] <= 0) {
          throw new IllegalArgumentException("Logarithm base and argument must be positive");
        }
        return Math.log(args[1]) / Math.log(args[0]);
      }
    };

    builder.function(logFunction);
    // Build the expression
    Expression expr = builder.build();
    return expr;
  }

  /**
   * Main method to demonstrate the conversion of a large number without scientific notation.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    System.out.println(convertToStringWithoutScientificNotation(222222222222.2222222222222));
  }

  /**
   * Validates a payoff function containing both relative and non-relative variables.
   * This method only checks the syntax without evaluating the actual values.
   *
   * @param payoffFunction The payoff function to validate
   * @return true if the function is valid, false otherwise
   */
  public static Pattern relativePattern = Pattern.compile("P[0-9]+p[0-9]+");

  public static boolean validatePayoffFunction(String payoffFunction) {
    // Check for null, blank or DEFAULT
    if (payoffFunction == null || payoffFunction.isBlank() || payoffFunction.equals("DEFAULT")) {
      return true;
    }

    payoffFunction = payoffFunction.trim();
    String modifiedExpression = payoffFunction;
    System.out.println("Validating payoff function: " + payoffFunction);

    // check invalid characters
    if (!payoffFunction.matches("^[a-zA-Z0-9\\s+\\-*/%().,P\\[\\]^]+$")) {
      System.out.println("Invalid characters in payoff function: " + payoffFunction);
      return false;
    }

    // First replace all relative variables (P1p1, P2p2, etc.)
    Matcher relativeMatcher = relativePattern.matcher(modifiedExpression);
    while (relativeMatcher.find()) {
      String placeholder = relativeMatcher.group();
      System.out.println("Found relative variable: " + placeholder);
      modifiedExpression = modifiedExpression.replace(placeholder, "1.0");
    }

    // Then replace all non-relative variables (p1, p2, etc.)
    Pattern nonRelativePattern = Pattern.compile("p[0-9]+");
    Matcher nonRelativeMatcher = nonRelativePattern.matcher(modifiedExpression);
    while (nonRelativeMatcher.find()) {
      String placeholder = nonRelativeMatcher.group();
      System.out.println("Found non-relative variable: " + placeholder);
      modifiedExpression = modifiedExpression.replace(placeholder, "1.0");
    }

    System.out.println("Modified expression: " + modifiedExpression);

    // Check for any remaining invalid variables
    if (modifiedExpression.matches(".*[pP][0-9].*")) {
      System.out.println("Remaining invalid variables found");
      return false;
    }

    // Check for balanced parentheses
    int openCount = 0;
    for (int i = 0; i < modifiedExpression.length(); i++) {
      char c = modifiedExpression.charAt(i);
      if (c == '(') {
        openCount++;
      } else if (c == ')') {
        openCount--;
        if (openCount < 0) {
          System.out.println("Unbalanced parentheses");
          return false;
        }
      }
    }
    
    if (openCount != 0) {
      System.out.println("Unbalanced parentheses");
      return false;
    }

    // Check for consecutive operators
    if (modifiedExpression.matches(".*[+\\-*/%][+\\-*/%].*")) {
      System.out.println("Consecutive operators found");
      return false;
    }

    // Check for empty operations
    if (modifiedExpression.matches(".*[+\\-*/%]\\s*[)].*") || 
        modifiedExpression.matches(".*[(]\\s*[+\\-*/%].*") ||
        modifiedExpression.matches(".*[+\\-*/%]\\s*$")) {
      System.out.println("Empty operations found");
      return false;
    }

    // Try to evaluate the expression
    try {
      // Replace any remaining whitespace
      modifiedExpression = modifiedExpression.replaceAll("\\s+", "");
      
      // Create expression builder
      ExpressionBuilder builder = new ExpressionBuilder(modifiedExpression);
      
      // Add basic functions
      builder.function(new Function("max", 2) {
        @Override
        public double apply(double... args) {
          return Math.max(args[0], args[1]);
        }
      });
      
      builder.function(new Function("min", 2) {
        @Override
        public double apply(double... args) {
          return Math.min(args[0], args[1]);
        }
      });

      // Build and validate expression
      Expression expr = builder.build();
      ValidationResult result = expr.validate();
      
      if (!result.isValid()) {
        System.out.println("Expression validation failed: " + result.getErrors());
        return false;
      }

      // Try to evaluate
      double evalResult = expr.evaluate();
      System.out.println("Expression evaluated successfully with result: " + evalResult);
      return true;
    } catch (Exception e) {
      System.out.println("Expression evaluation failed: " + e.getMessage());
      return false;
    }
  }
}
