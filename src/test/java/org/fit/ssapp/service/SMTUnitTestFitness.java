package org.fit.ssapp.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  @ParameterizedTest
  @CsvSource({
          "SIGMA{*S1}, 12.0",
          "M1 *+ M2, 7.0",
          "SIGMA{S1} - M1 / 0, 9.0",
          "INVALID_FUNCTION{S1}, 0.0",
          "SIGMA{S1 + M2, 0.0"
  })
  public void testInvalidFitnessCustom(String fitnessFunction, double expected) {
    MatchingData matchingData = setupMatchingData(3, 1, 3);
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    double[] satisfaction = {3.0, 4.0, 5.0};
    double fitnessScore = evaluator.withFitnessFunctionEvaluation(satisfaction, fitnessFunction);
    assertEquals(expected, fitnessScore, 0.001);
  }

  /**
   * Tests the default fitness function, which sums up the satisfaction values.
   */
  @ParameterizedTest
  @CsvSource({
          "{3.0,4.0,5.0}",
          "{0.0,0.0,0.0}",
          "{5.0}",
          "{-2.0,3.0,-1.0}",
          "{2.5,3.5,4.0}",
          "{1.0,2.0,3.0,4.0,5.0}",
  })
  public void testFitnessDefault(String satisfactionStr) {
    MatchingData matchingData = setupMatchingData(3, 1, 3);
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    double[] satisfaction = stringToDoubleArray(satisfactionStr);
    double fitnessScore = evaluator.defaultFitnessEvaluation(satisfaction);

    double expected = Arrays.stream(satisfaction).sum();

    assertEquals(expected, fitnessScore, 0.001);
  }

  /**
   * Tests the fitness function evaluation with an empty satisfaction array.
   */
  @Test
  public void testFitnessValueWithEmptySatisfaction() {
    MatchingData matchingData = setupMatchingData(0, 1, 3);
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    double[] satisfaction = {};
    double result = evaluator.withFitnessFunctionEvaluation(satisfaction, "SUM");
    assertEquals(0.0, result, 0.001);
  }

  /**
   * Tests the fitness function evaluation with a null fitness function.
   */
  @Test
  public void testFitnessValueWithNullFunction() {
    MatchingData matchingData = setupMatchingData(3, 1, 3);
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    double[] satisfaction = {3.0, 4.0, 5.0};
    double fitnessScore = evaluator.withFitnessFunctionEvaluation(satisfaction, null);
    assertEquals(12.0, fitnessScore, 0.001);
  }

  /**
   * Converse string to double array.
   */
  public static double[] stringToDoubleArray(String input) {
    if (input == null || !input.contains("{") || !input.contains("}")) {
      return new double[0];
    }
    int startIndex = input.indexOf('{') + 1;
    int endIndex = input.indexOf('}');

    String insideBrackets = input.substring(startIndex, endIndex);

    String[] stringArray = insideBrackets.split(", ");

    double[] doubleArray = new double[stringArray.length];
    for (int i = 0; i < stringArray.length; i++) {
      doubleArray[i] = Double.parseDouble(stringArray[i]);
    }

    return doubleArray;
  }

}