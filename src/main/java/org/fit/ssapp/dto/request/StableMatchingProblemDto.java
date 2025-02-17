package org.fit.ssapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.constants.MessageConst.ErrMessage;
import org.fit.ssapp.dto.validator.*;


/**
 * Dto class for SMT problem request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidIndividualArraysSize
@ValidEvaluateFunctionCount
@ValidIndividualArrayPropertyCount
@ValidStableMatching
public class StableMatchingProblemDto implements ProblemRequestDto {

  @Size(max = 255, message = ErrMessage.PROBLEM_NAME)
  private String problemName;

  @Min(value = 2, message = ErrMessage.MES_001)
  private int numberOfSets;

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

  private int populationSize;

  private int generation;

  private int numberOfIndividuals;

  private int maxTime;

  private String algorithm;

  @ValidDistributedCores
  private String distributedCores;

  @Override
  public String toString() {
    return "StableMatchingProblemDto{" +
            "problemName='" + problemName + '\'' +
            ", numberOfSets=" + numberOfSets +
            ", numberOfIndividuals=" + numberOfIndividuals +
            ", numberOfProperty=" + numberOfProperty +
            ", individualSetIndices=" + Arrays.toString(individualSetIndices) +
            ", individualCapacities=" + Arrays.toString(individualCapacities) +
            ", individualRequirements=" + Arrays.toString(individualRequirements) +
            ", individualWeights=" + Arrays.toString(individualWeights) +
            ", individualProperties=" + Arrays.toString(individualProperties) +
            ", evaluateFunctions=" + Arrays.toString(evaluateFunctions) +
            ", fitnessFunction='" + fitnessFunction + '\'' +
            ", excludedPairs=" + Arrays.toString(excludedPairs) +
            ", populationSize=" + populationSize +
            ", generation=" + generation +
            ", maxTime=" + maxTime +
            ", algorithm='" + algorithm + '\'' +
            ", distributedCores='" + distributedCores + '\'' +
            '}';
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }
}