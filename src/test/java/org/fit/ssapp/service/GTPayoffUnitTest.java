package org.fit.ssapp.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.fit.ssapp.util.StringExpressionEvaluator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    @Test
    void testDefaultPayoffFunction() {
        Strategy strategy = new Strategy();
        List<Double> properties = new ArrayList<>();
        properties.add(1.0);
        properties.add(2.0);
        properties.add(3.0);
        strategy.setProperties(properties);

        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy, "");
        
        assertEquals(6.0, result.doubleValue(), 0.00001);
    }

    /**
     * Test custom non-relative payoff function (using pi syntax)
     * where p1, p2, p3 refer to properties of the strategy
     */
    @ParameterizedTest
    @CsvSource({
        "p1 + p2, 3.0",         
        "p2 * p3, 6.0",      
        "p3 / p1, 3.0"       
    })
    void testCustomNonRelativePayoff(String expression, double expected) {
        Strategy strategy = new Strategy();
        List<Double> properties = new ArrayList<>();
        properties.add(1.0); 
        properties.add(2.0);  
        properties.add(3.0);  
        strategy.setProperties(properties);
        
        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy, expression);
        
        assertEquals(expected, result.doubleValue(), 0.00001);
    }

    /**
     * Test custom relative payoff function (using Pipj syntax)
     * where Pi refers to player i, and pj refers to property j of that player's strategy
     */
    @Test
    void testCustomRelativePayoff() {
        NormalPlayer player1 = new NormalPlayer();
        Strategy strategy1 = new Strategy();
        List<Double> properties1 = new ArrayList<>();
        properties1.add(1.0);
        properties1.add(2.0);
        strategy1.setProperties(properties1);
        player1.setStrategies(List.of(strategy1));

        NormalPlayer player2 = new NormalPlayer();
        Strategy strategy2 = new Strategy();
        List<Double> properties2 = new ArrayList<>();
        properties2.add(3.0);
        properties2.add(4.0);
        strategy2.setProperties(properties2);
        player2.setStrategies(List.of(strategy2));

        List<NormalPlayer> players = List.of(player1, player2);
        int[] chosenStrategyIndices = {0, 0}; 

        String expression = "P1p1 + P2p2"; 
        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(
            strategy1, expression, players, chosenStrategyIndices);
        
        assertEquals(5.0, result.doubleValue(), 0.00001); 
    }

    /**
     * Test built-in mathematical operations from exp4j
     */
    @ParameterizedTest
    @CsvSource({
        "p1^2, 1.0",             
        "abs(p1 - p2), 1.0", 
        "cbrt(p2^3), 2.0",    
        "ceil(p1 + 0.5), 2.0",   
        "floor(p2 + 0.9), 2.0", 
        "sqrt(p2^2), 2.0",
        "log(p3), 1.0986"
    })
    void exp4jOperations(String expression, double expected) {
        Strategy strategy = new Strategy();
        List<Double> properties = new ArrayList<>();
        properties.add(1.0);  // p1
        properties.add(2.0);  // p2
        properties.add(3.0);  // p3
        strategy.setProperties(properties);
        
        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy, expression);
        
        // For ln(3), we need a larger epsilon
        double epsilon = expression.startsWith("log") ? 0.001 : 0.00001;
        assertEquals(expected, result.doubleValue(), epsilon);
    }

    /**
     * Test indexing rules - using base 1 index
     */
    @ParameterizedTest
    @CsvSource({
        "p1, 1.0",   
        "p2, 2.0",   
        "p3, 3.0"    
    })
    void testIndexingRules(String expression, double expected) {
        Strategy strategy = new Strategy();
        List<Double> properties = new ArrayList<>();
        properties.add(1.0);
        properties.add(2.0);
        properties.add(3.0);
        strategy.setProperties(properties);
        
        BigDecimal result = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy, expression);
        
        assertEquals(expected, result.doubleValue(), 0.00001);
    }

    /**
     * Test invalid syntax and values
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "p1p2",                      
        "p1 + p0",                
        "p1 * p-1",                 
        "sqrt(p1 - p2)",            
        "log(p1 - p2)",               
        "p1 + p2)",                   
        "invalid",                    
        "p1 + invalid",             
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

    /**
     * Test function independence from object state
     */
    @Test
    void functionIndependence() {

        Strategy strategy1 = new Strategy();
        List<Double> properties1 = new ArrayList<>();
        properties1.add(1.0);
        properties1.add(2.0);
        strategy1.setProperties(properties1);

        Strategy strategy2 = new Strategy();
        List<Double> properties2 = new ArrayList<>();
        properties2.add(1.0);
        properties2.add(2.0);
        strategy2.setProperties(properties2);

        String expression = "p1 + p2";
        BigDecimal result1 = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy1, expression);
        BigDecimal result2 = StringExpressionEvaluator.evaluatePayoffFunctionNoRelative(
            strategy2, expression);

        assertEquals(result1, result2);
        assertEquals(3.0, result1.doubleValue(), 0.00001);
    }
} 
