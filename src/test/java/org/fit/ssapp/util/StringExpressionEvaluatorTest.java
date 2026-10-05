package org.fit.ssapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying StringExpressionEvaluator with native exp4j variables,
 * specifically ensuring no variable prefix substitution collision (e.g. u1 corrupting u10, u2 corrupting u20, p1 corrupting p10).
 */
public class StringExpressionEvaluatorTest {

  @Test
  @DisplayName("Verify u1 + u10 evaluates to 25.0 (was 55.0 with replaceAll bug)")
  void testFitnessFunction_u1_plus_u10() {
    double[] payoffs = new double[10];
    Arrays.fill(payoffs, 1.0);
    payoffs[0] = 5.0;  // u1 = 5
    payoffs[9] = 20.0; // u10 = 20

    BigDecimal result = StringExpressionEvaluator.evaluateFitnessValue(payoffs, "u1 + u10");
    assertEquals(25.0, result.doubleValue(), 0.0001,
        "u1 + u10 must equal 25.0 instead of 55.0");
  }

  @Test
  @DisplayName("Verify u2 + u20 evaluates to 25.0 (was 55.0 with replaceAll bug)")
  void testFitnessFunction_u2_plus_u20() {
    double[] payoffs = new double[20];
    Arrays.fill(payoffs, 1.0);
    payoffs[1] = 5.0;   // u2 = 5
    payoffs[19] = 20.0; // u20 = 20

    BigDecimal result = StringExpressionEvaluator.evaluateFitnessValue(payoffs, "u2 + u20");
    assertEquals(25.0, result.doubleValue(), 0.0001,
        "u2 + u20 must equal 25.0 instead of 55.0");
  }

  @Test
  @DisplayName("Verify u3 + u30 evaluates to 25.0 (was 55.0 with replaceAll bug)")
  void testFitnessFunction_u3_plus_u30() {
    double[] payoffs = new double[30];
    Arrays.fill(payoffs, 1.0);
    payoffs[2] = 5.0;   // u3 = 5
    payoffs[29] = 20.0; // u30 = 20

    BigDecimal result = StringExpressionEvaluator.evaluateFitnessValue(payoffs, "u3 + u30");
    assertEquals(25.0, result.doubleValue(), 0.0001,
        "u3 + u30 must equal 25.0 instead of 55.0");
  }

  @Test
  @DisplayName("Verify u1 + u20 evaluates to 35.0 (unaffected case)")
  void testFitnessFunction_u1_plus_u20() {
    double[] payoffs = new double[20];
    Arrays.fill(payoffs, 1.0);
    payoffs[0] = 5.0;   // u1 = 5
    payoffs[19] = 30.0; // u20 = 30

    BigDecimal result = StringExpressionEvaluator.evaluateFitnessValue(payoffs, "u1 + u20");
    assertEquals(35.0, result.doubleValue(), 0.0001);
  }

  @Test
  @DisplayName("Verify complex expression with parentheses: (u1 + u10) * u2 - u20")
  void testFitnessFunction_complexExpression() {
    double[] payoffs = new double[20];
    Arrays.fill(payoffs, 1.0);
    payoffs[0] = 5.0;   // u1 = 5
    payoffs[1] = 3.0;   // u2 = 3
    payoffs[9] = 10.0;  // u10 = 10
    payoffs[19] = 15.0; // u20 = 15

    // (5 + 10) * 3 - 15 = 15 * 3 - 15 = 45 - 15 = 30
    BigDecimal result = StringExpressionEvaluator.evaluateFitnessValue(payoffs, "(u1 + u10) * u2 - u20");
    assertEquals(30.0, result.doubleValue(), 0.0001);
  }

  @Test
  @DisplayName("Verify out of bounds player throws IllegalArgumentException")
  void testFitnessFunction_outOfBounds() {
    double[] payoffs = new double[20]; // only 20 players (u1..u20)

    assertThrows(IllegalArgumentException.class, () ->
        StringExpressionEvaluator.evaluateFitnessValue(payoffs, "u1 + u30"));
  }

  @Test
  @DisplayName("Verify p1 + p10 in Payoff function avoids prefix collision")
  void testPayoffFunctionNoRelative_p1_plus_p10() {
    Strategy strategy = new Strategy();
    List<Double> props = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      props.add(1.0);
    }
    props.set(0, 5.0);  // p1 = 5
    props.set(9, 20.0); // p10 = 20
    strategy.setProperties(props);

    BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(strategy, "p1 + p10");
    assertEquals(25.0, result.doubleValue(), 0.0001,
        "p1 + p10 must equal 25.0 instead of 55.0");
  }

  @Test
  @DisplayName("Verify relative payoff function P2p1 + p1")
  void testPayoffFunctionWithRelative() {
    Strategy s1 = new Strategy();
    s1.setProperties(List.of(10.0, 20.0)); // p1 = 10, p2 = 20

    Strategy s2 = new Strategy();
    s2.setProperties(List.of(5.0, 15.0));  // p1 = 5, p2 = 15

    NormalPlayer p1 = new NormalPlayer();
    p1.setStrategies(List.of(s1));

    NormalPlayer p2 = new NormalPlayer();
    p2.setStrategies(List.of(s2));

    List<NormalPlayer> players = List.of(p1, p2);
    int[] chosen = new int[]{0, 0};

    // P2p1 = Player 2 property 1 (5.0), p1 = Player 1 property 1 (10.0) -> 5.0 + 10.0 = 15.0
    BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
        s1, "P2p1 + p1", players, chosen);
    assertEquals(15.0, result.doubleValue(), 0.0001);
  }

  @Test
  @DisplayName("Verify default aggregator functions still work correctly")
  void testDefaultAggregators() {
    double[] payoffs = new double[]{2.0, 4.0, 6.0};

    assertEquals(12.0, StringExpressionEvaluator.evaluateFitnessValue(payoffs, "SUM").doubleValue(), 0.0001);
    assertEquals(4.0, StringExpressionEvaluator.evaluateFitnessValue(payoffs, "AVERAGE").doubleValue(), 0.0001);
    assertEquals(2.0, StringExpressionEvaluator.evaluateFitnessValue(payoffs, "MIN").doubleValue(), 0.0001);
    assertEquals(6.0, StringExpressionEvaluator.evaluateFitnessValue(payoffs, "MAX").doubleValue(), 0.0001);
    assertEquals(48.0, StringExpressionEvaluator.evaluateFitnessValue(payoffs, "PRODUCT").doubleValue(), 0.0001);
    assertEquals(4.0, StringExpressionEvaluator.evaluateFitnessValue(payoffs, "MEDIAN").doubleValue(), 0.0001);
    assertEquals(4.0, StringExpressionEvaluator.evaluateFitnessValue(payoffs, "RANGE").doubleValue(), 0.0001);
  }
}
