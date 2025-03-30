package org.fit.ssapp.service;

import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the fitness evaluation functionality in the Stable Matching Problem (SMT).
 * It tests both custom and default fitness functions .
 */
public class SMTUnitTestFitness {
  private TwoSetFitnessEvaluator evaluator;

  private MatchingData setupMatchingData(int numberOfIndividuals1, int numberOfIndividuals2, int numberOfProperties) {
    SampleDataGenerator sampleData = new SampleDataGenerator(
            MatchingProblemType.MTM,
            numberOfIndividuals1,
            numberOfIndividuals2,
            numberOfProperties
    );
    return sampleData.generateProblem().getMatchingData();
  }

  /**
   * Provides a stream of test cases for the default fitness evaluation.
   * Each test case consists of a satisfaction array .
   */
  static Stream<Arguments> satisfactionForDefault() {
    return Stream.of(
            Arguments.of(new double[]{3.0, 4.0, 5.0}),
            Arguments.of(new double[]{0.0, 0.0, 0.0}),
            Arguments.of(new double[]{5.0}),
            Arguments.of(new double[]{-2.0, 3.0, -1.0}),
            Arguments.of(new double[]{2.5, 3.5, 4.0}),
            Arguments.of(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}),
            Arguments.of(new double[]{})
    );
  }

  /**
   * Tests the default fitness function, which sums up the satisfaction values.
   *
   * @param satisfaction The array of satisfaction values to evaluate.
   */
  @ParameterizedTest
  @MethodSource("satisfactionForDefault")
  public void testFitnessDefault(double[] satisfaction) {
    MatchingData matchingData = setupMatchingData(3, 1, 3);
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    double fitnessScore = evaluator.defaultFitnessEvaluation(satisfaction);

    double expected = Arrays.stream(satisfaction).sum();

    assertEquals(expected, fitnessScore, 0.001);
  }

  /**
   * Tests custom fitness functions using parameterized inputs.
   *
   * @param fitnessFunction The fitness function to evaluate.
   * @param expected       The expected result of the fitness function evaluation.
   */
  @ParameterizedTest
  @CsvSource({
          "SIGMA{S1}, 12.0",
          "M1 + M2, 7.0",
          "SIGMA{S1} - M1, 9.0",
          "ceil(sqrt(SIGMA{S1})) + 2, 6",
          "ceil(SIGMA{S1} / 4), 3.0"
  })
  public void testFitnessCustom(String fitnessFunction, double expected) {
    MatchingData matchingData = setupMatchingData(3, 1, 3);
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    double[] satisfaction = {3.0, 4.0, 5.0};
    double fitnessScore = evaluator.withFitnessFunctionEvaluation(satisfaction, fitnessFunction);
    assertEquals(expected, fitnessScore, 0.001);
  }


  /**
   * Tests custom fitness functions using parameterized inputs.
   *
   * @param fitnessFunction The fitness function to evaluate.
   * @param expected       The expected result of the fitness function evaluation.
   */
  // @ParameterizedTest
  @CsvSource({
//          "SIGMA{*S1}",
          "M1 *+ M2",
//          "SIGMA{S1} - M1 / 0",
//          "INVALID_FUNCTION{S1}",
//          "SIGMA{S1 + M2",
//          "null"
  })
  public void testInvalidFitnessCustom(String fitnessFunction) {
    MatchingData matchingData = setupMatchingData(3, 1, 3);
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    double[] satisfaction = {3.0, 4.0, 5.0};

    assertThrows(Exception.class, () ->
        evaluator.withFitnessFunctionEvaluation(satisfaction, fitnessFunction)
    );
  }
}
