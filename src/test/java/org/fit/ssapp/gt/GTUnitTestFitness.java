package org.fit.ssapp.gt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluateFitnessValue;
import java.util.stream.Stream;

/**
 * This class contains unit tests for fitness calculation in game theory.
 * It tests various fitness functions, including default and custom functions,
 * and handles edge cases such as empty payoffs, null functions, and invalid
 * functions.
 */
public class GTUnitTestFitness {

  /**
   * Test the fitness calculation for various fitness functions and maximizing
   * scenarios.
   *
   * @param fitnessFunction The fitness function to be evaluated .
   * @param isMaximizing    Indicates whether the fitness value should be negated
   *                        .
   * @param expected        The expected result of the fitness calculation.
   */
  @ParameterizedTest
  @CsvSource({
      "SUM, false, 12.0",
      "AVERAGE, true, -4.0",
      "MAX, true, -5.0",
      "MIN, false, 3.0",
      "RANGE, true, -2.0",
      "MEDIAN, true, -4.0",
      "u1+u2+u3, false, 12.0",
      "u1*3+u2+u3, true, -18.0",
      "ceil(10 / u2), true, -3.0",
      "sqrt(u2) + u1, false, 5.0",
      "(u1+u2+u3)/4, false, 3.0",
      "ceil(sin(u1) + cos(u2) + sqrt(u3)), false , 2.0"
  })
  public void testFitnessCalculation(String fitnessFunction, boolean isMaximizing, double expected) {
    double[] payoffs = { 3.0, 4.0, 5.0 };

    BigDecimal result = evaluateFitnessValue(payoffs, fitnessFunction);

    // Apply maximizing
    if (isMaximizing) {
      result = result.negate();
    }

    assertEquals(expected, result.doubleValue(), 0.0001);
  }

  private static Stream<Arguments> provideDefaultFunction() {
    return Stream.of(
        // Empty/blank/null fitness function cases
        Arguments.of("", new double[] { 3.0, 4.0, 5.0 }, 12.0, "Empty string should default to sum of payoffs"),
        Arguments.of("   ", new double[] { 3.0, 4.0, 5.0 }, 12.0, "Blank string should default to sum of payoffs"),
        Arguments.of(null, new double[] { 3.0, 4.0, 5.0, 100.001 }, 112.001, "Null should default to sum of payoffs"),
        Arguments.of(null, new double[] { 3.0, 4.0, 5.0, 200.020 }, 212.020, "Null should default to sum of payoffs"),
        Arguments.of(null, new double[] { 3.0, 4.0, 5.0, 123.123 }, 135.123, "Null should default to sum of payoffs"));
  }

  /**
   * Tests the fitness calculation for empty payoff scenarios.
   */
  @ParameterizedTest
  @MethodSource("provideDefaultFunction")
  public void defaultFunction(String function, double[] payoffs, double expected, String message) {
    BigDecimal result = evaluateFitnessValue(payoffs, function);
    assertEquals(expected, result.doubleValue(), 0.0001, message);
  }

  /**
   * Tests the fitness calculation for empty payoff scenarios.
   */
  @Test
  public void testFitnessValueWithEmptyPayoffs() {
    double[] emptyPayoffs = {};
    BigDecimal result = evaluateFitnessValue(emptyPayoffs, "SUM");
    assertEquals(0.0, result.doubleValue(), 0.0001);
  }

  @ParameterizedTest
  @MethodSource("provideCustomFunction")
  public void customFunction(String fitnessFunction, double[] payoffs, double expected,
      String testDescription) {
    BigDecimal result = evaluateFitnessValue(payoffs, fitnessFunction);
    assertEquals(expected, result.doubleValue(), 0.0001, testDescription);
  }

  private static Stream<Arguments> provideCustomFunction() {
    return Stream.of(
        // Default functions with standard input
        Arguments.of("SUM", new double[] { 3.0, 4.0, 5.0 }, 12.0, "SUM function"),
        Arguments.of("AVERAGE", new double[] { 3.0, 4.0, 5.0 }, 4.0, "AVERAGE function"),
        Arguments.of("MIN", new double[] { 3.0, 4.0, 5.0 }, 3.0, "MIN function"),
        Arguments.of("MAX", new double[] { 3.0, 4.0, 5.0 }, 5.0, "MAX function"),
        Arguments.of("PRODUCT", new double[] { 3.0, 4.0, 5.0 }, 60.0, "PRODUCT function"),
        Arguments.of("MEDIAN", new double[] { 3.0, 4.0, 5.0 }, 4.0, "MEDIAN function"),
        Arguments.of("RANGE", new double[] { 3.0, 4.0, 5.0 }, 2.0, "RANGE function"),

        // Case insensitive tests
        Arguments.of("sum", new double[] { 3.0, 4.0, 5.0 }, 12.0, "sum function (lowercase)"),
        Arguments.of("Sum", new double[] { 3.0, 4.0, 5.0 }, 12.0, "Sum function (mixed case)"),
        Arguments.of("AVERAGE", new double[] { 3.0, 4.0, 5.0 }, 4.0, "AVERAGE function (uppercase)"),

        // Empty array with different default functions
        Arguments.of("SUM", new double[] {}, 0.0, "Empty array with SUM"),
        Arguments.of("PRODUCT", new double[] {}, 1.0, "Empty array with PRODUCT"),
        Arguments.of("AVERAGE", new double[] {}, 0.0, "Empty array with AVERAGE"));
  }

  @ParameterizedTest
  @CsvSource({
      "u1+u2+*u3",
      "INVALID FUNCTION",
      "MIN*Max",
      "MIN/0",
  })
  public void testFitnessValueWithInvalidFunction(String fitnessFunction) {
    double[] payoffs = { 3.0, 4.0, 5.0 };

    assertThrows(IllegalArgumentException.class, () -> evaluateFitnessValue(payoffs, fitnessFunction));
  }
}