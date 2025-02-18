package org.fit.ssapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.constants.MessageConst.ErrMessage;
import org.fit.ssapp.dto.validator.ValidDistributedCores;
import org.fit.ssapp.dto.validator.ValidEvaluateFunction;
import org.fit.ssapp.dto.validator.ValidEvaluateFunctionCount;
import org.fit.ssapp.dto.validator.ValidFitnessFunction;
import org.fit.ssapp.dto.validator.ValidIndividualArrayPropertyCount;
import org.fit.ssapp.dto.validator.ValidIndividualArraysSize;
import org.fit.ssapp.dto.validator.ValidRequirementSyntax;


/**
 * StableMatchingProblemDto - Data Transfer Object for Stable Matching Problem requests.
 * This DTO is used to receive and validate input data for a Stable Matching Problem (SMT).
 * It contains all necessary parameters to define the problem, including individuals, sets,
 * properties, requirements, and algorithm settings.
 * ## Key Fields:
 * - Problem Definition: `problemName`, `numberOfSets`, `numberOfIndividuals`, `numberOfProperty`
 * - Individual Assignments: `individualSetIndices`, `individualCapacities`
 * - Preferences & Weights: `individualRequirements`, `individualWeights`, `individualProperties`
 * - Function Evaluations: `evaluateFunctions`, `fitnessFunction`
 * - Algorithm Configurations: `populationSize`, `generation`, `maxTime`, `algorithm`
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidIndividualArraysSize
@ValidEvaluateFunctionCount
@ValidIndividualArrayPropertyCount
public class StableMatchingProblemDto implements ProblemRequestDto {

  @Size(max = 255, message = ErrMessage.PROBLEM_NAME)
  private String problemName;

  @Min(value = 2, message = ErrMessage.MES_001)
  private int numberOfSets;

  @Min(value = 3, message = ErrMessage.MES_002)
  private int numberOfIndividuals;

  @Min(value = 1, message = ErrMessage.MES_003)
  private int numberOfProperty;

  @Size(min = 1, message = ErrMessage.MES_004)
  private int[] individualSetIndices;

  @Size(min = 1, message = ErrMessage.MES_004)
  private int[] individualCapacities;

  @Size(min = 3, message = ErrMessage.MES_002)
  @ValidRequirementSyntax
  private String[][] individualRequirements;

  @Size(min = 3, message = ErrMessage.MES_002)
  private double[][] individualWeights;

  @Size(min = 3, message = ErrMessage.MES_002)
  private double[][] individualProperties;

  @NotNull(message = ErrMessage.NOT_BLANK)
  @ValidEvaluateFunction
  private String[] evaluateFunctions;

  @NotEmpty(message = ErrMessage.NOT_BLANK)
  @ValidFitnessFunction
  private String fitnessFunction;

  private int[][] excludedPairs;

  @Max(value = 3000, message = ErrMessage.POPULATION_SIZE)
  private int populationSize;

  @Max(value = 1000, message = ErrMessage.GENERATION)
  private int generation;

  private int maxTime;

  //    @NotEmpty(message = ErrMessage.NOT_BLANK)
  private String algorithm;

  @ValidDistributedCores
  private String distributedCores;

  @Override
  public String toString() {
    return "StableMatchingProblemDto{" + "problemName='" + problemName + '\'' + ", numberOfSets="
            + numberOfSets + ", numberOfIndividuals=" + numberOfIndividuals + ", numberOfProperty="
            + numberOfProperty + ", individualSetIndices=" + Arrays.toString(individualSetIndices)
            + ", individualCapacities=" + Arrays.toString(individualCapacities) + Arrays.toString(
            individualRequirements) + ", individualRequirements=" + ", individualWeights="
            + Arrays.toString(individualWeights) + ", individualProperties=" + Arrays.toString(
            individualProperties) + ", evaluateFunctions=" + Arrays.toString(evaluateFunctions)
            + ", fitnessFunction='" + fitnessFunction + '\'' + ", excludedPairs=" + Arrays.toString(
            excludedPairs) + ", populationSize=" + populationSize + ", generation=" + generation
            + ", maxTime=" + maxTime + ", algorithm='" + algorithm + '\'' + ", distributedCores='"
            + distributedCores + '\'' + '}';
  }
}