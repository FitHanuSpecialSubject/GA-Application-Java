package com.example.SS2_Backend.dto.request;

import com.example.SS2_Backend.constants.MessageConst.ErrMessage;
import com.example.SS2_Backend.dto.validator.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidIndividualArraysSize
@ValidEvaluateFunctionCount
@ValidIndividualArrayPropertyCount
public class StableMatchingProblemDTO implements ProblemRequestDto {

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

    private int [][] excludedPairs;

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
        return "StableMatchingProblemDTO{" + "problemName='" + problemName + '\'' +
                ", numberOfSets=" + numberOfSets + ", numberOfIndividuals=" + numberOfIndividuals +
                ", numberOfProperty=" + numberOfProperty + ", individualSetIndices=" +
                Arrays.toString(individualSetIndices) + ", individualCapacities=" +
                Arrays.toString(individualCapacities) + ", individualRequirements=" +
                Arrays.toString(individualRequirements) + ", individualWeights=" +
                Arrays.toString(individualWeights) + ", individualProperties=" +
                Arrays.toString(individualProperties) + ", evaluateFunctions=" +
                Arrays.toString(evaluateFunctions) + ", fitnessFunction='" + fitnessFunction +
                '\'' + ", excludedPairs=" + Arrays.toString(excludedPairs) + ", populationSize=" +
                populationSize + ", generation=" + generation + ", maxTime=" + maxTime +
                ", algorithm='" + algorithm + '\'' + ", distributedCores='" + distributedCores +
                '\'' + '}';
    }
}