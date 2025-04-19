package org.fit.ssapp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

      // Collect all invalid indices before processing
      Set<String> invalidReferences = new HashSet<>();

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
            invalidReferences.add("P" + (ji[0] + 1) + " (player index out of bounds, max: " + normalPlayers.size() + ")");
            continue;
          }

          NormalPlayer otherPlayer = normalPlayers.get(ji[0]);
          Strategy otherPlayerStrategy = otherPlayer.getStrategyAt(chosenStrategyIndices[ji[0]]);

          // Validate property index
          if (ji[1] < 0 || ji[1] >= otherPlayerStrategy.getProperties().size()) {
            invalidReferences.add("p" + (ji[1] + 1) + " for player " + (ji[0] + 1) + 
                " (property index out of bounds, max: " + otherPlayerStrategy.getProperties().size() + ")");
            continue;
          }

          double propertyValue = otherPlayerStrategy.getProperties().get(ji[1]);
          expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
        } else {
          // non-relative variables
          int index = Integer.parseInt(placeholder.substring(1)) - 1;
          if (index < 0 || index >= strategy.getProperties().size()) {
            invalidReferences.add(placeholder + " (property index out of bounds, max: " + strategy.getProperties().size() + ")");
            continue;
          }
          double propertyValue = strategy.getProperties().get(index);
          expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
        }
      }

      // If there are any invalid references, throw an exception with detailed message
      if (!invalidReferences.isEmpty()) {
        throw new IllegalArgumentException("Invalid property reference(s) in payoff function '" + payoffFunction + "': " + 
            String.join(", ", invalidReferences));
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
      
      // Collect all invalid indices before processing
      Set<String> invalidReferences = new HashSet<>();

      Matcher nonRelativeMatcher = nonRelativePattern.matcher(expression);
      while (nonRelativeMatcher.find()) {
        String placeholder = nonRelativeMatcher.group();
        int index = Integer.parseInt(placeholder.substring(1)) - 1;
        if (index < 0 || index >= strategy.getProperties().size()) {
          invalidReferences.add(placeholder + " (property index out of bounds, max: " + strategy.getProperties().size() + ")");
          continue;
        }
        double propertyValue = strategy.getProperties().get(index);
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      }
      
      // If there are any invalid references, throw an exception with detailed message
      if (!invalidReferences.isEmpty()) {
        throw new IllegalArgumentException("Invalid property reference(s) in payoff function '" + payoffFunction + "': " + 
            String.join(", ", invalidReferences));
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
   * @return A {@code BigDecimal} result of the fitness function.
   */
  public static BigDecimal evaluateFitnessValue(double[] payoffs, String fitnessFunction) {
    // Check for null or default case
    if (fitnessFunction == null || fitnessFunction.isEmpty() || fitnessFunction.equalsIgnoreCase("DEFAULT")) {
      // Calculate the sum of payoffs if DEFAULT
      double sum = 0;
      for (double payoff : payoffs) {
        sum += payoff;
      }
      return new BigDecimal(sum).setScale(10, RoundingMode.HALF_UP);
    }

    // If it's a built-in function (SUM, AVERAGE, etc.)
    if (checkIfIsDefaultFunction(fitnessFunction)) {
      List<Double> values = new ArrayList<>();
      for (double payoff : payoffs) {
        values.add(payoff);
      }
      return calculateByDefault(values, fitnessFunction);
    }

    // Actual number of players
    int playerCount = payoffs.length;

    // Find all 'u' variables in the expression
    Pattern uPattern = Pattern.compile("u(\\d+)");
    Matcher matcher = uPattern.matcher(fitnessFunction);
    
    // Check if all 'u' variables are valid before proceeding
    Set<Integer> invalidIndices = new HashSet<>();
    while (matcher.find()) {
      int playerIndex = Integer.parseInt(matcher.group(1));
      if (playerIndex < 1 || playerIndex > playerCount) {
        invalidIndices.add(playerIndex);
      }
    }
    
    // If any invalid indices are found, throw an exception with detailed message
    if (!invalidIndices.isEmpty()) {
      String message = String.format(
          "Error in fitness function '%s': Variable %s refers to non-existent player. " +
          "Current player count is %d (valid variables are u1 to u%d).",
          fitnessFunction,
          invalidIndices.size() == 1 ? "u" + invalidIndices.iterator().next() : "variables " + formatInvalidIndices(invalidIndices),
          playerCount,
          playerCount
      );
      throw new IllegalArgumentException(message);
    }

    String expression = fitnessFunction;
    matcher.reset();
    
    while (matcher.find()) {
      String variable = matcher.group(0);
      int index = Integer.parseInt(matcher.group(1)) - 1; // Convert to 0-based index
      double value = payoffs[index];
      expression = expression.replaceAll(variable, formatDouble(value));
    }

    double val = evaluateExpression(expression);
    return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP);
  }

  /**
   * Format a list of invalid indices for error message
   */
  private static String formatInvalidIndices(Set<Integer> indices) {
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
    // Handle "default" as a special case that defaults to SUM
    if (StringUtils.isEmptyOrNull(defaultFunction) ||
        defaultFunction.equalsIgnoreCase("default") ||
        defaultFunction.equalsIgnoreCase("DEFAULT")) {
      return new BigDecimal(calSum(values));
    }

    try {
      DefaultFunction function = DefaultFunction.valueOf(defaultFunction.toUpperCase());
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
    } catch (IllegalArgumentException e) {
      // If the function name is not valid, default to SUM
      return new BigDecimal(calSum(values));
    }
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

    Function ceilFunction = new Function("ceil", 1) {
      @Override
      public double apply(double... args) {
        return Math.ceil(args[0]);
      }
    };

    Function sqrtFunction = new Function("sqrt", 1) {
      @Override
      public double apply(double... args) {
        if (args[0] < 0) {
          throw new IllegalArgumentException("Square root of negative number is not allowed");
        }
        return Math.sqrt(args[0]);
      }
    };

    builder.function(logFunction)
        .function(ceilFunction)
        .function(sqrtFunction);

    // Build the expression
    Expression expr = builder.build();
    return expr;
  }

}
