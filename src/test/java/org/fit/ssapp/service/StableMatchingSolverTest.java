package org.fit.ssapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 */
public class StableMatchingSolverTest {

  StableMatchingProblemDto stableMatchingProblemDto;
  SampleDataGenerator sampleData;
  int numberOfIndividuals1;
  int numberOfIndividuals2;
  int numberOfProperties;
  private Validator validator;

  /**
   *
   */
  @BeforeEach
  public void setUp() {
    numberOfIndividuals1 = 20;
    numberOfIndividuals2 = 200;
    sampleData = new SampleDataGenerator(
        MatchingProblemType.MTM,
        numberOfIndividuals1, numberOfIndividuals2,
        numberOfProperties
    );
    stableMatchingProblemDto = sampleData.generateDto();
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

  }

  /**
   *
   */
  @Test
  public void testEvaluateFunctions() {
    stableMatchingProblemDto.setEvaluateFunctions(new String[]{"default", "default"});
    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(
        stableMatchingProblemDto);
    assert (violations.isEmpty());
  }

  /**
   *
   */
  //    @Test
//    public void testFitnessCalculation() {
//        TwoSetFitnessEvaluator newEvaluator = new TwoSetFitnessEvaluator(sampleData.generateProblem().getMatchingData());
//        newEvaluator.withFitnessFunctionEvaluation(new double[]{}, sampleData.getFnf());
//    }
  @Test
  public void testFitnessCalculation() {
    // Create sample data
    double[] satisfactions = {1.0, 2.0, 3.0, 4.0, 5.0};
    String fitnessFunction = "SIGMA{S1} + SIGMA{S2}"; // Assuming S1 and S2 are the sets

    // Create a sample MatchingData object
    int numberOfIndividuals1 = 5;
    int numberOfIndividuals2 = 5;
    SampleDataGenerator sampleData = new SampleDataGenerator(
        MatchingProblemType.MTM,
        numberOfIndividuals1, numberOfIndividuals2,
        3 // number of properties
    );
    MatchingData matchingData = sampleData.generateProblem().getMatchingData();

    // Create the evaluator
    TwoSetFitnessEvaluator evaluator = new TwoSetFitnessEvaluator(matchingData);

    // Perform the fitness function evaluation
    double result = evaluator.withFitnessFunctionEvaluation(satisfactions, fitnessFunction);

    // Verify the result (assuming the function is correctly defined)
    // Here, we assume SIGMA{S1} = 15 and SIGMA{S2} = 0 (since S2 is not populated in this example)
    double expected = 15.0;
    assertEquals(expected, result, 0.001);
  }

  /**
   *
   */
  @Test
  public void testStableSolverMTM() {
    StableMatchingService solver = new StableMatchingService(null);
    // Solve the problem
    ResponseEntity<Response> response = solver.solve(stableMatchingProblemDto);

    // Verify the response
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  /**
   *
   */
  @Test
  public void testStableSolverOTM() {
    StableMatchingOtmService stableMatchingOTMProblemDTO = new StableMatchingOtmService(null);
    // Solve the problem
    ResponseEntity<Response> response = stableMatchingOTMProblemDTO.solve(
        stableMatchingProblemDto);

    // Verify the response
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

//    @Test
//    public void testStableSolverOTO() {
//        int numberOfProperties = 5;
//        SampleDataGenerator generator = new SampleDataGenerator(
//                MatchingProblemType.OTO,
//                20, 50,
//                numberOfProperties);
//        generator.setCapacities.put(0, 5);
//        generator.setCapacities.put(1, 5);
//        generator.setCapRandomize(new boolean[]{false, false});
//        generator.setEvaluateFunctions(new String[]{DEFAULT_EVALUATE_FUNC, DEFAULT_EVALUATE_FUNC});
//        generator.setFnf(DEFAULT_FITNESS_FUNC);
//
//        String algo = "IBEA";
//        MatchingProblem problem = generator.generateProblem();
//        // Run the algorithm
//        NondominatedPopulation result = new Executor()
//                .withProblem(problem)
//                .withAlgorithm(algo)
//                .withMaxEvaluations(100)
//                .withProperty("populationSize", 1000)
//                .distributeOnAllCores()
//                .run();
//
//    }
}

