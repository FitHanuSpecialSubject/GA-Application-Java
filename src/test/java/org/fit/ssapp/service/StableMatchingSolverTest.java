package org.fit.ssapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Arrays;
import java.util.Set;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StableMatchingSolverTest {

  StableMatchingProblemDto stableMatchingProblemDto;
  SampleDataGenerator sampleData;
  int numberOfIndividuals1;
  int numberOfIndividuals2;
  int numberOfProperties;
  private Validator validator;

  @BeforeEach
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    numberOfIndividuals1 = 20;
    numberOfIndividuals2 = 200;
    numberOfProperties = 5;
    sampleData = new SampleDataGenerator(MatchingProblemType.MTM, numberOfIndividuals1,
            numberOfIndividuals2, numberOfProperties);
    stableMatchingProblemDto = sampleData.generateDto();
  }


//  @Test
//  public void testEvaluateFunctions() {
//    stableMatchingProblemDto.setEvaluateFunctions(new String[]{"SUM", ""});
//    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(
//            stableMatchingProblemDto);
//    assert (violations.isEmpty());
//  }

  @Test
  public void testFitnessCalculation() {
    int testNumberOfIndividuals1 = 5;
    int testNumberOfIndividuals2 = 1;  //or any positive number
    int testNumberOfProperties = 3;
    double[] satisfactions = {1.0, 2.0, 3.0, 4.0, 5.0};
    String fitnessFunction = "SIGMA{S1}";

    SampleDataGenerator sampleData = new SampleDataGenerator(
            MatchingProblemType.MTM,
            testNumberOfIndividuals1, testNumberOfIndividuals2,
            testNumberOfProperties
    );

    MatchingData matchingData = sampleData.generateProblem().getMatchingData();

    // Create the evaluator
    TwoSetFitnessEvaluator evaluator = new TwoSetFitnessEvaluator(matchingData);

    // Perform the fitness function evaluation
    double result = evaluator.withFitnessFunctionEvaluation(satisfactions, fitnessFunction);

    // Verify the result
    double expected = 15.0;
    assertEquals(expected, result, 0.001);
  }


  @Test
  public void testStableSolverMTM() {
    sampleData = new SampleDataGenerator(MatchingProblemType.MTM, numberOfIndividuals1,
            numberOfIndividuals2, numberOfProperties);
    assertDoesNotThrow(() -> sampleData.generateProblem());
  }

  @Test
  public void testStableSolverOTM() {
    sampleData = new SampleDataGenerator(MatchingProblemType.OTM, numberOfIndividuals1,
            numberOfIndividuals2, numberOfProperties);
    assertDoesNotThrow(() -> sampleData.generateProblem());
  }

}