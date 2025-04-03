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
 */
public class GTUnitTestFitness {

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

  @Test
  public void testFitnessValueWithEmptyPayoffs() {
    double[] emptyPayoffs = {};
    BigDecimal result = evaluateFitnessValue(emptyPayoffs, "SUM");
    assertEquals(0.0, result.doubleValue(), 0.0001);
  }

  @Test
  public void testFitnessValueWithNullFunction() {
    double[] payoffs = {3.0, 4.0, 5.0};
    BigDecimal result = evaluateFitnessValue(payoffs, null);
    // Default should be SUM
    assertEquals(12.0, result.doubleValue(), 0.0001);
  }
  
  @Test
  public void testFitnessValueWithEmptyString() {
    double[] payoffs = {3.0, 4.0, 5.0};
    BigDecimal result = evaluateFitnessValue(payoffs, "");
    assertEquals(12.0, result.doubleValue(), 0.0001);
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
