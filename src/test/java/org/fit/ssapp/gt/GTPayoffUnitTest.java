package org.fit.ssapp.gt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.math.BigDecimal;

import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.fit.ssapp.util.StringExpressionEvaluator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for GameTheory Payoff Function functionality.
 *
 * Tests focus on:
 * 1. Default payoff function (sum of all properties)
 * 2. Custom non-relative payoff function (using pi syntax)
 * 3. Custom relative payoff function (using Pipj syntax)
 * 4. Built-in mathematical operations from exp4j
 * 5. Input validation and error handling
 */
public class GTPayoffUnitTest {
    /**
     * Test default payoff function (sum of all properties)
     */
    @ParameterizedTest
    @MethodSource("defaultPayoffTestCases")
    void defaultPayoffFunction(List<Double> properties, double expected) {
        Strategy strategy = new Strategy();
        strategy.setProperties(properties);

        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy, "");

        assertEquals(expected, result.doubleValue(), 0.0001);
    }

    /**
     * Provides test cases for default payoff function evaluation.
     * 
     */
    private static Stream<Arguments> defaultPayoffTestCases() {
        return Stream.of(
            // Format: properties, expected sum
            Arguments.of(List.of(1.0, 2.0, 3.0), 6.0),
            Arguments.of(List.of(5.0, 10.0, 15.0), 30.0),
            Arguments.of(List.of(1.5, 2.5, 3.5), 7.5),
            Arguments.of(List.of(-1.0, 2.0, 3.0), 4.0),
            Arguments.of(List.of(0.0, 0.0, 0.0), 0.0),
            Arguments.of(List.of(100.0, 200.0, 300.0, 400.0), 1000.0)
        );
    }

    /**
     * Test custom non-relative payoff function (using pi syntax)
     */
    @ParameterizedTest
    @MethodSource("nonRelativePayoff")
    void customNonRelativePayoff(List<Double> properties, String expression, double expected) {
        Strategy strategy = new Strategy();
        strategy.setProperties(properties);

        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy, expression);
        assertEquals(expected, result.doubleValue(), 0.0001);
    }

    /**
     * Provides test cases for non-relative payoff function evaluation.
     * 
     */
    private static Stream<Arguments> nonRelativePayoff() {
        return Stream.of(
            // Format: properties, expression, expected result
            Arguments.of(List.of(1.0, 2.0, 3.0), "p1", 1.0),
            Arguments.of(List.of(1.0, 2.0, 3.0), "p2", 2.0),
            Arguments.of(List.of(1.0, 2.0, 3.0), "p3", 3.0),
            Arguments.of(List.of(1.0, 2.0, 3.0), "p1 + p2", 3.0),
            Arguments.of(List.of(1.0, 2.0, 3.0), "p2 * p3", 6.0),
            Arguments.of(List.of(1.0, 2.0, 3.0), "p3 / p1", 3.0),
            Arguments.of(List.of(2.0, 4.0, 6.0), "p1 * p2 + p3", 14.0),
            Arguments.of(List.of(5.0, 10.0, 15.0), "(p1 + p2) * p3", 225.0),
            Arguments.of(List.of(1.0, 2.0, 3.0), "p1^2 + p2^2", 5.0),
            Arguments.of(List.of(4.0, 9.0, 16.0), "sqrt(p1) + sqrt(p2)", 5.0)
        );
    }

    /**
     * Test custom relative payoff function (using Pipj syntax)
     * where Pi refers to player i, and pj refers to property j of that player's strategy
     */
    @ParameterizedTest
    @MethodSource("relativePayoff")
    void customRelativePayoff(List<Double> player1Properties, List<Double> player2Properties,
                                 String expression, double expected) {
        NormalPlayer player1 = new NormalPlayer();
        Strategy strategy1 = new Strategy();
        strategy1.setProperties(player1Properties);
        player1.setStrategies(List.of(strategy1));

        NormalPlayer player2 = new NormalPlayer();
        Strategy strategy2 = new Strategy();
        strategy2.setProperties(player2Properties);
        player2.setStrategies(List.of(strategy2));

        List<NormalPlayer> players = List.of(player1, player2);
        int[] chosenStrategyIndices = {0, 0};

        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
            strategy1, expression, players, chosenStrategyIndices);

        assertEquals(expected, result.doubleValue(), 0.0001);
    }

    /**
     * Provides test cases for relative payoff function evaluation.
     */
    private static Stream<Arguments> relativePayoff() {
        return Stream.of(
            Arguments.of(List.of(1.0, 2.0), List.of(3.0, 4.0), "P1p1 + P2p2", 5.0),
            Arguments.of(List.of(1.0, 2.0), List.of(3.0, 4.0), "P1p2 + P2p1", 5.0),
            // sum
            Arguments.of(
                List.of(2.0, 4.0, 6.0),
                List.of(8.0, 10.0, 12.0),
                "P1p1 + P1p2 + P1p3 + P2p1 + P2p2 + P2p3",
                42.0
            ),
            // rounding
            Arguments.of(
                List.of(5.0, 10.0, 15.0),
                List.of(20.0, 25.0, 30.0),
                "ceil(P1p1 / 3) + floor( 12 +  1)",
                15.0
            ),
            // average
            Arguments.of(
                List.of(10.0, 20.0),
                List.of(30.0, 40.0),
                "(P1p1 + P1p2 + P2p1 + P2p2) / 4",
                25.0
            ),
            // square root and cubic root
            Arguments.of(
                List.of(5.0, 15.0),
                List.of(10.0, 20.0),
                "sqrt(P1p1 + P2p1) + cbrt(P1p2 + P2p2)",
                7.1440
            ),
            // logarithm
            Arguments.of(
                List.of(5.0, 10.0, 15.0),
                List.of(20.0, 25.0, 30.0),
                "log2(P1p1 + P2p1) + log(P1p2 + P2p2) - log10(P1p2 + P2p2)",
                6.6551
            ),
            // Standard deviation approximation (for 2 values only)
            Arguments.of(
                         List.of(10.0),
                         List.of(20.0),
                         "abs(P1p1 - P2p1) / 2",
                         5.0
                         )
        );
    }

    /**
     * Test built-in exp4j operations for both relative and non-relative expressions.
     * Tests mathematical functions and operations provided by the exp4j library.
     */
    @ParameterizedTest
    @MethodSource("exp4jOperation")
    void exp4jFunctions(List<Double> properties, List<Double> player2Properties,
                                  String expression, String relativeExpression, boolean isRelative, double expected, double epsilon) {
        Strategy strategy = new Strategy();
        strategy.setProperties(properties);

        if (!isRelative) {
            BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
                strategy, expression);
            assertEquals(expected, result.doubleValue(), 0.00001);
        } else {
            NormalPlayer player1 = new NormalPlayer();
            player1.setStrategies(List.of(strategy));

            NormalPlayer player2 = new NormalPlayer();
            Strategy strategy2 = new Strategy();
            strategy2.setProperties(player2Properties);
            player2.setStrategies(List.of(strategy2));

            List<NormalPlayer> players = List.of(player1, player2);
            int[] chosenStrategyIndices = {0, 0};

            BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
                strategy, relativeExpression, players, chosenStrategyIndices);
            assertEquals(expected, result.doubleValue(), 0.00001);
        }
    }

    /**
     * Provides test cases for exp4j mathematical operations in payoff functions. 
     */
    private static Stream<Arguments> exp4jOperation() {
        return Stream.of(
            // Format: properties, player2Properties, nonRelativeExpression, relativeExpression, isRelative, expected, epsilon
            Arguments.of(
                List.of(2.0, 4.0, 8.0),
                List.of(),
                "2^3",
                "",
                false,
                8.0, 0.00001
            ),
            Arguments.of(List.of(2.0, 3.0), List.of(4.0, 5.0), "", "2^P1p1", true, 4.0, 0.00001),

            Arguments.of(List.of(1.0, 2.0, 3.0), List.of(), "cbrt(p2^3)", "", false, 2.0, 0.00001),
            Arguments.of(List.of(2.0, 3.0), List.of(4.0, 5.0), "", "cbrt(P2p1^3)", true, 4.0, 0.00001),

            Arguments.of(List.of(1.0, 2.0, 3.0), List.of(), "ceil(p1 + 0.5)", "", false, 2.0, 0.00001),
            Arguments.of(List.of(1.5, 2.5), List.of(3.5, 4.5), "", "ceil(P1p1)", true, 2.0, 0.00001),

            Arguments.of(List.of(1.0, 2.0, 3.0), List.of(), "floor(p2 + 0.9)", "", false, 2.0, 0.00001),
            Arguments.of(List.of(1.7, 2.7), List.of(3.7, 4.7), "", "floor(P1p1)", true, 1.0, 0.00001),

            Arguments.of(List.of(1.0, 2.0, 3.0), List.of(), "log(p3)", "", false, 1.0986, 0.0001),
            Arguments.of(List.of(1.0, 2.0), List.of(3.0, 4.0), "", "log(P2p1)", true, 1.0986, 0.0001),

            Arguments.of(List.of(1.0, 2.0, 3.0), List.of(), "sqrt(p2^2)", "", false, 2.0, 0.00001),
            Arguments.of(List.of(4.0, 5.0), List.of(9.0, 16.0), "", "sqrt(P1p1)", true, 2.0, 0.00001),

            Arguments.of(List.of(2.0, 4.0, 8.0), List.of(), "sqrt(p1) + cbrt(p3)", "", false, 3.4142, 0.0001), //non-relative
            Arguments.of(List.of(4.0, 5.0), List.of(9.0, 16.0), "", "sqrt(P1p1) + sqrt(P2p1)", true, 5.0, 0.00001) //relative
        );
    }

    /**
     * Tests invalid syntax and values in payoff functions to ensure proper error handling.
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "p-1",
        "p1 * p-1",
        "sqrt(p1 - p2)",
        "log(p1 - p2)",
        "1/(p1 - p1)",
        "p1 +",
        "p1 + p2)",
        "@@@@ + abcbab"
    })
    void testInvalid(String expression) {
        Strategy strategy = new Strategy();
        List<Double> properties = new ArrayList<>();
        properties.add(1.0);
        properties.add(2.0);
        properties.add(3.0);
        strategy.setProperties(properties);

        assertThrows(Exception.class, () -> {
            StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(strategy, expression);
        });

        NormalPlayer player1 = new NormalPlayer();
        player1.setStrategies(List.of(strategy));
        List<NormalPlayer> players = List.of(player1);
        int[] chosenStrategyIndices = {0};

        assertThrows(Exception.class, () -> {
            StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
                strategy, expression, players, chosenStrategyIndices);
        });
    }
}
