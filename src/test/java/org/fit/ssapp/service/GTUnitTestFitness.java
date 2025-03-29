package org.fit.ssapp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluateFitnessValue;

/**
 * This class contains unit tests for fitness calculation in game theory.
 * It tests various fitness functions, including default and custom functions,
 * and handles edge cases such as empty payoffs, null functions, and invalid functions.
 * 
 * The tests verify:
 * 1. Standard fitness functions (SUM, AVERAGE, MAX, MIN, etc)
 * 2. Custom mathematical expressions using payoff variables (u1, u2, u3)
 * 3. Complex expressions with mathematical functions (sqrt, ceil, sin, cos)
 * 4. Handling of maximizing vs minimizing scenarios
 * 5. Edge cases (empty arrays, null values, invalid expressions)
 */
public class GTUnitTestFitness {

  /**
   * Test the fitness calculation for various fitness functions and maximizing scenarios.
   * This parameterized test covers a wide range of fitness functions:
   * - Built-in functions (SUM, AVERAGE, MAX, MIN, RANGE, MEDIAN)
   * - Custom arithmetic expressions using payoff variables (u1, u2, u3)
   * - Complex expressions with mathematical functions (sqrt, ceil, sin, cos)
   *
   * Each test case uses the fixed payoff array {3.0, 4.0, 5.0} and verifies that:
   * 1. The fitness function is correctly evaluated
   * 2. The maximizing flag properly negates results when true
   * 3. The final value matches the expected result
   *
   * @param fitnessFunction The fitness function to be evaluated (built-in or custom expression)
   * @param isMaximizing    Whether the fitness value should be negated (for maximization problems)
   * @param expected        The expected result of the fitness calculation after applying maximization
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

    assertEquals(expected, result.doubleValue(), 0.001);
  }

  /**
   * Tests the fitness calculation for empty payoff scenarios.
   * 
   * This test verifies that the system correctly handles the edge case of an empty payoff array.
   * When provided with an empty array and the "SUM" function, the system should return zero
   * rather than throwing an exception or returning an incorrect value.
   * 
   * The test creates an empty payoff array, evaluates it with the SUM function,
   * and verifies that the result is 0.0.
   */
  @Test
  public void testFitnessValueWithEmptyPayoffs() {
    double[] emptyPayoffs = {};
    BigDecimal result = evaluateFitnessValue(emptyPayoffs, "SUM");
    assertEquals(0.0, result.doubleValue(), 0.001);
  }

  /**
   * Tests the fitness calculation for null fitness functions.
   * 
   * This test verifies that the system applies the default behavior (SUM) when the fitness function
   * is null. The system should handle null values gracefully instead of throwing exceptions.
   * 
   * The test:
   * 1. Creates a payoff array {3.0, 4.0, 5.0}
   * 2. Evaluates it with a null fitness function
   * 3. Verifies that the result is 12.0 (the sum of all payoffs)
   */
 // @Test
  public void testFitnessValueWithNullFunction() {
    double[] payoffs = {3.0, 4.0, 5.0};
    BigDecimal result = evaluateFitnessValue(payoffs, null);
    // Default should be SUM
    assertEquals(12.0, result.doubleValue(), 0.001);
  }

  /**
   * Tests the fitness calculation for invalid function expressions.
   * 
   * This parameterized test verifies that the system properly handles invalid expressions
   * by throwing an appropriate exception rather than returning incorrect results or crashing.
   * 
   * Test cases include:
   * 1. Syntax errors (missing operators, invalid symbols)
   * 2. Invalid mathematical operations (e.g., division by zero)
   * 3. Incorrect function names or formats
   * 
   * For each test case, the system should throw a RuntimeException when attempting to
   * evaluate the invalid expression.
   *
   * @param fitnessFunction The invalid fitness function expression to test
   */
  // @ParameterizedTest
  @CsvSource({
          "u1+u2+*u3",
          "u2ceil(10 / u2)",
          "INVALID FUNCTION",
          "MIN*Max",
          "MIN/0",
  })
  public void testFitnessValueWithInvalidFunction(String fitnessFunction) {
    double[] payoffs = {3.0, 4.0, 5.0};

    assertThrows(RuntimeException.class, () ->
        evaluateFitnessValue(payoffs, fitnessFunction));
    }
}
