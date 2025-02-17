package org.fit.ssapp.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;

/**
 * A utility class to evaluate mathematical expressions in string format. Supports variable
 * replacement, basic arithmetic operations, and trigonometric functions.
 */
public class StringExpressionEvaluator {

  public static Pattern nonRelativePattern = Pattern.compile("p[0-9]+");
  public static Pattern fitnessPattern = Pattern.compile("u[0-9]+");

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
    String expression = payoffFunction;

    Pattern generalPattern = Pattern.compile("(P[0-9]+)?" + nonRelativePattern.pattern());
    Matcher generalMatcher = generalPattern.matcher(expression);
    while (generalMatcher.find()) {
      String placeholder = generalMatcher.group();
      // indices should account for offset from base 1 index of variables
      if (placeholder.contains("P")) {
        // relative variables - syntax Pjpi with j the player index, and i the property index
        int[] ji = Arrays.stream(placeholder
                .substring(1) // remove P
                .split("p")) // split at p
            .mapToInt(Integer::parseInt)
            .map(x -> x - 1)
            .toArray(); // [j, i]
        NormalPlayer otherPlayer = normalPlayers.get(ji[0]);
        Strategy otherPlayerStrategy = otherPlayer.getStrategyAt(chosenStrategyIndices[ji[0]]);
        double propertyValue = otherPlayerStrategy.getProperties().get(ji[1]);
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      } else {
        // non-relative variables
        int index = Integer.parseInt(placeholder.substring(1)) - 1;
        double propertyValue = strategy.getProperties().get(index);
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      }
    }

    // evaluate this string expression to get the result
    double val = eval(expression);
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

    String expression = payoffFunction;

    if (payoffFunction.isBlank()) {
      // the payoff function is the sum function of all properties by default
      return calculateByDefault(strategy.getProperties(), null);
    } else {

      if (checkIfIsDefaultFunction(payoffFunction)) {
        return calculateByDefault(strategy.getProperties(), payoffFunction);
      }

      Matcher nonRelativeMatcher = nonRelativePattern.matcher(expression);
      // replace non-relative variables with value
      while (nonRelativeMatcher.find()) {
        String placeholder = nonRelativeMatcher.group();
        // indices should account for offset from base 1 index of variables
        int index = Integer.parseInt(placeholder.substring(1)) - 1;
        double propertyValue = strategy.getProperties().get(index);
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      }

      // evaluate this string expression to get the result
      double val = eval(expression);
      return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP);

    }

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
    String expression = fitnessFunction;
    List<Double> payoffList = new ArrayList<>();
    for (double payoff : payoffs) {
      payoffList.add(payoff);
    }

    if (fitnessFunction.isBlank()) {
      // if the fitnessFunction is absent,
      // the fitness value is the average of all payoffs of all chosen strategies by default
      return calculateByDefault(payoffList, null);
    } else {
      // replace placeholders for players' payoffs with the actual values

      if (checkIfIsDefaultFunction(fitnessFunction)) {
        return calculateByDefault(payoffList, fitnessFunction);
      }
      Matcher fitnessMatcher = fitnessPattern.matcher(expression);
      while (fitnessMatcher.find()) {
        String placeholder = fitnessMatcher.group();
        // indices should account for offset from base 1 index of variables
        int index = Integer.parseInt(placeholder.substring(1)) - 1;
        double propertyValue = payoffs[index];
        expression = expression.replaceAll(placeholder, formatDouble(propertyValue));
      }

      double val = eval(expression);
      return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP);

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


  private static BigDecimal calculateByDefault(List<Double> values, String defaultFunction) {
    DefaultFunction function = (!StringUtils.isEmptyOrNull(defaultFunction))
        ? DefaultFunction.valueOf(defaultFunction.toUpperCase()) : DefaultFunction.SUM;
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
   * @param strExpression The expression to evaluate.
   * @return The computed result as a double.
   */
  public static double eval(String strExpression) {

    String formattedExpression = strExpression.replaceAll("NaN",
            "0")// Replace NaN(Not A Number) with 0, so that the expression can be evaluated
        .replaceAll("\\s+",
            "")
        .replaceAll(",", "."); // Replace , to . (default double decimal separator)

    return new Object() {
      int pos = -1;
      int ch;

      void nextChar() {
        ch = (++pos < formattedExpression.length()) ? formattedExpression.charAt(pos) : -1;
      }

      boolean eat(int charToEat) {
        while (ch == ' ') {
          nextChar();
        }

        if (ch == charToEat) {
          nextChar();
          return true;
        }
        return false;
      }

      double parse() {
        nextChar();
        double x = parseExpression();

        if (pos < formattedExpression.length()) {
          System.out.println("wrong expression: " + formattedExpression);
          throw new RuntimeException("Unexpected: " + (char) ch);
        }
        return x;
      }

      // Grammar:
      // expression = term | expression `+` term | expression `-` term
      //  = factor | term `*` factor | term `/` factor
      // factor = `+` factor | `-` factor | `(` expression `)` | number
      //        | functionName `(` expression `)` | functionName factor
      //        | factor `^` factor

      double parseExpression() {
        double x = parseTerm();
        for (; ; ) {
          if (eat('+')) {
            x += parseTerm(); // addition
          } else if (eat('-')) {
            x -= parseTerm(); // subtraction
          } else {
            return x;
          }
        }
      }

      double parseTerm() {
        double x = parseFactor();
        for (; ; ) {
          if (eat('*')) {
            x *= parseFactor(); // multiplication
          } else if (eat('/')) {
            x /= parseFactor(); // division
          } else {
            return x;
          }
        }
      }

      double getArgForFunction() {
        double a;
        if (eat('(')) {
          if (eat('e') || eat('E')) {
            a = Math.E;
          } else {
            a = parseExpression();
          }
          if (!eat(')')) {
            throw new RuntimeException("Missing ')' after argument to log");
          }
        } else {
          throw new RuntimeException("Incorrect arguments for log function");
        }
        return a;
      }

      double parseFactor() {
        if (eat('+')) {
          return +parseFactor(); // unary plus
        }
        if (eat('-')) {
          return -parseFactor(); // unary minus
        }

        double x;
        int startPos = this.pos;
        if (eat('(')) { // parentheses
          x = parseExpression();
          if (!eat(')')) {
            System.out.println("Missing ')'");
            System.out.println(formattedExpression);
            throw new RuntimeException("Missing ')'");
          }
        } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
          while ((ch >= '0' && ch <= '9') || ch == '.') {
            nextChar();
          }
          x = Double.parseDouble(formattedExpression.substring(startPos, this.pos));
        } else if (ch >= 'a' && ch <= 'z') { // functions
          while (ch >= 'a' && ch <= 'z') {
            nextChar();
          }
          String func = formattedExpression.substring(startPos, this.pos);
          if (Objects.equals(func, "log")) {
            double a = getArgForFunction();
            double b = getArgForFunction();
            return customLog(a, b);
          }
          if (eat('(')) {
            x = parseExpression();
            if (!eat(')')) {
              throw new RuntimeException("Missing ')' after argument to " + func);
            }
          } else {
            x = parseFactor();
          }
          x = switch (func) {
            case "abs" -> Math.abs(x);
            case "sqrt" -> Math.sqrt(x);
            case "sin" -> Math.sin(Math.toRadians(x));
            case "cos" -> Math.cos(Math.toRadians(x));
            case "tan" -> Math.tan(Math.toRadians(x));
            default -> throw new RuntimeException("Unknown function: " + func);
          };
        } else {
          System.out.println("wrong expression: " + formattedExpression);
          throw new RuntimeException("Unexpected: " + (char) ch);
        }
        if (eat('^')) {
          x = Math.pow(x, parseFactor()); // exponentiation
        }
        return x;
      }
    }.parse();
  }

  private static double customLog(double base, double logNumber) {
    return Math.log(logNumber) / Math.log(base);
  }

  /**
   * Main method to demonstrate the conversion of a large number without scientific notation.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    System.out.println(convertToStringWithoutScientificNotation(222222222222.2222222222222));
  }

}




