package org.fit.ssapp.service;

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
 * and handles edge cases such as empty payoffs, null functions, and invalid functions.
 */
public class GTUnitTestFitness {

  /**
   * Test the fitness calculation for various fitness functions and maximizing scenarios.
   *
   * @param fitnessFunction The fitness function to be evaluated .
   * @param isMaximizing    Indicates whether the fitness value should be negated .
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
    double[] payoffs = {3.0, 4.0, 5.0};

    BigDecimal result = evaluateFitnessValue(payoffs, fitnessFunction);

    // Apply maximizing
    if (isMaximizing) {
      result = result.negate();
    }

    assertEquals(expected, result.doubleValue(), 0.0001);
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

  /**
   * Tests the fitness calculation for null fitness.
   */
  @Test
  public void testFitnessValueWithNullFunction() {
    double[] payoffs = {3.0, 4.0, 5.0};
    BigDecimal result = evaluateFitnessValue(payoffs, null);
    // Default should be SUM
    assertEquals(12.0, result.doubleValue(), 0.0001);
  }

  @ParameterizedTest
  @MethodSource("provideDefaultFitnessTestData")
  public void testFitnessValueWithDefaultInput(String fitnessFunction, String payoffsStr, double expected, String testDescription) {
    double[] payoffs;
    if (payoffsStr.isEmpty()) {
      payoffs = new double[0];
    } else {
      String[] payoffStrings = payoffsStr.split(",");
      payoffs = new double[payoffStrings.length];
      for (int i = 0; i < payoffStrings.length; i++) {
        payoffs[i] = Double.parseDouble(payoffStrings[i].trim());
      }
    }

    BigDecimal result = evaluateFitnessValue(payoffs, fitnessFunction);
    assertEquals(expected, result.doubleValue(), 0.0001, testDescription);
  }

  private static Stream<Arguments> provideDefaultFitnessTestData() {
    return Stream.of(
      // Empty/blank/null fitness function cases
      Arguments.of("", "3.0,4.0,5.0", 12.0, "Empty string should default to sum of payoffs"),
      Arguments.of("   ", "3.0,4.0,5.0", 12.0, "Blank string should default to sum of payoffs"),
      Arguments.of(null, "3.0,4.0,5.0", 12.0, "Null should default to sum of payoffs"),

      // Default functions with standard input
      Arguments.of("SUM", "3.0,4.0,5.0", 12.0, "SUM function"),
      Arguments.of("AVERAGE", "3.0,4.0,5.0", 4.0, "AVERAGE function"),
      Arguments.of("MIN", "3.0,4.0,5.0", 3.0, "MIN function"),
      Arguments.of("MAX", "3.0,4.0,5.0", 5.0, "MAX function"),
      Arguments.of("PRODUCT", "3.0,4.0,5.0", 60.0, "PRODUCT function"),
      Arguments.of("MEDIAN", "3.0,4.0,5.0", 4.0, "MEDIAN function"),
      Arguments.of("RANGE", "3.0,4.0,5.0", 2.0, "RANGE function"),

      // Case insensitive tests
      Arguments.of("sum", "3.0,4.0,5.0", 12.0, "sum function (lowercase)"),
      Arguments.of("Sum", "3.0,4.0,5.0", 12.0, "Sum function (mixed case)"),
      Arguments.of("AVERAGE", "3.0,4.0,5.0", 4.0, "AVERAGE function (uppercase)"),

      // Empty array with different default functions
      Arguments.of("SUM", "", 0.0, "Empty array with SUM"),
      Arguments.of("PRODUCT", "", 1.0, "Empty array with PRODUCT"),
      Arguments.of("AVERAGE", "", 0.0, "Empty array with AVERAGE")
    );
  }

  @ParameterizedTest
  @CsvSource({
          "u1+u2+*u3",
          "INVALID FUNCTION",
          "MIN*Max",
          "MIN/0",
  })
  public void testFitnessValueWithInvalidFunction(String fitnessFunction) {
    double[] payoffs = {3.0, 4.0, 5.0};

    assertThrows(ArithmeticException.class, () ->
        evaluateFitnessValue(payoffs, fitnessFunction));
  }
}
