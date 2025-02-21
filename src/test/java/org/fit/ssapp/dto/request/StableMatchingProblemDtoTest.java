package org.fit.ssapp.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for StableMatchingProblemDto.
 */
public class StableMatchingProblemDtoTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidDTO() {
    StableMatchingProblemDto dto = new StableMatchingProblemDto();
    dto.setProblemName("Stable Matching Problem");
    dto.setNumberOfSets(2);
    dto.setNumberOfProperty(3);
    dto.setNumberOfIndividuals(3);
    dto.setIndividualSetIndices(new int[]{1, 1, 0});
    dto.setIndividualCapacities(new int[]{1, 2, 1});
    dto.setIndividualRequirements(new String[][]{
            {"1", "1.1", "1--"},
            {"1++", "1.1", "1.1"},
            {"1", "1", "2"}
    });
    dto.setIndividualWeights(new double[][]{
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
    });
    dto.setIndividualProperties(new double[][]{
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
    });
    dto.setEvaluateFunctions(new String[]{
            "10*(P1*W1) + 5*(P1*W2) + (P6*W6) + (P7*W7)",
            "(sqrt(P8*W8)) + 2*(P9*W9) + (e)*W10) + 5*(P11*W11)"
    });
    dto.setFitnessFunction("default");
    dto.setExcludedPairs(new int[][]{
            {1, 2},
            {2, 3}
    });
    dto.setPopulationSize(500);
    dto.setGeneration(50);
    dto.setMaxTime(3600);
    dto.setAlgorithm("Genetic Algorithm");
    dto.setDistributedCores("4");

    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(dto);
    violations.forEach(violation -> System.out.println(violation.getMessage()));
    assertTrue(violations.isEmpty(), "There should be no constraint violations");
  }

  @Test
  void testInvalidDTO() {
    StableMatchingProblemDto invalidDto = new StableMatchingProblemDto();
    invalidDto.setProblemName("");
    invalidDto.setNumberOfSets(1); // Less than 2 sets
    invalidDto.setNumberOfProperty(2); // Less than 3 properties
    invalidDto.setNumberOfIndividuals(2); // Less than 3 individuals
    invalidDto.setIndividualSetIndices(new int[]{1, 0});
    invalidDto.setIndividualCapacities(new int[]{1, 2});
    invalidDto.setIndividualRequirements(new String[][]{
            {"1", "1.1"},
            {"1++", "1.1"}
    });
    invalidDto.setIndividualWeights(new double[][]{
            {1.0, 2.0},
            {4.0, 5.0}
    });
    invalidDto.setIndividualProperties(new double[][]{
            {1.0, 2.0},
            {4.0, 5.0}
    });
    invalidDto.setEvaluateFunctions(new String[]{
            "10*(P1*W1) + 5*(P1*W2) + (P6*W6) + (P7*W7)"
    });
    invalidDto.setFitnessFunction(""); // Empty fitness function
    invalidDto.setExcludedPairs(new int[][]{
            {1, 2},
            {2, 3}
    });
    invalidDto.setPopulationSize(500);
    invalidDto.setGeneration(50);
    invalidDto.setMaxTime(3600);
    invalidDto.setAlgorithm("Genetic Algorithm");
    invalidDto.setDistributedCores("4");

    Set<ConstraintViolation<StableMatchingProblemDto>> violations = validator.validate(invalidDto);
    assertTrue(violations.size() > 0, "There should be constraint violations");
  }
}