package org.fit.ssapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.fit.ssapp.constants.MessageConst.ErrMessage;
import org.fit.ssapp.dto.validator.*;


/**
 * Dto class for SMT problem request.
 */
@ValidIndividualArraysSize
@ValidEvaluateFunctionCount
@ValidIndividualArrayPropertyCount
@ValidStableMatching
@Getter
@Setter
@NoArgsConstructor
public class StableMatchingProblemDto extends ProblemDto {
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

  private int numberOfIndividuals;

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
}
