package org.fit.ssapp.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.fit.ssapp.dto.validator.ValidFitnessFunctionGT;
import org.fit.ssapp.dto.validator.ValidStrategyStructure;
import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.SpecialPlayer;
import org.fit.ssapp.dto.validator.ValidPayoffFunction;
import org.fit.ssapp.dto.validator.ValidThreshold;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * dto class for GT problem request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidStrategyStructure
@ValidFitnessFunctionGT
@ValidPayoffFunction
@ValidThreshold
public class GameTheoryProblemDto implements ProblemRequestDto {
  private SpecialPlayer specialPlayer;

  @Valid
  @NotEmpty(message = "Normal players list cannot be empty")
  @NotNull(message = "Normal players are required")
  @Size(min = 1, message = "At least one normal player is required")
  private List<NormalPlayer> normalPlayers;

  private List<Conflict> conflictSet = new ArrayList<>();

  @NotNull(message = "Fitness function cannot be null")
  private String fitnessFunction;

  @NotNull(message = "Default payoff function is required")
  @NotBlank(message = "Default payoff function is required")
  private String defaultPayoffFunction;

  @NotNull(message = "Algorithm is required")
  @NotBlank(message = "Algorithm cannot be empty")
  private String algorithm;


  private boolean isMaximizing;
  private String distributedCores;

  @NotNull(message = "Max time is required")
  @Min(value = 1, message = "Max time must be greater than 0")
  private Integer maxTime;

  @NotNull(message = "Generation is required")
  @Min(value = 1, message = "Generation must be greater than 0")
  private int generation;

  @NotNull(message = "Population size is required")
  @Min(value = 1, message = "Population size must be greater than 0")
  private int populationSize;

  @Override
  public String toString() {
    return "GameTheoryProblemDto{" + "specialPlayer=" + specialPlayer + ", normalPlayers="
        + normalPlayers + ", conflictSet=" + ", fitnessFunction='" + conflictSet + fitnessFunction
        + '\'' + ", defaultPayoffFunction='" + defaultPayoffFunction + '\'' + ", algorithm='"
        + algorithm + '\'' + ", isMaximizing=" + isMaximizing + ", distributedCores='"
        + distributedCores + '\'' + ", maxTime=" + maxTime + ", generation=" + generation
        + ", populationSize=" + populationSize + '}';
  }
}