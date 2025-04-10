package org.fit.ssapp.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.SpecialPlayer;
import org.fit.ssapp.dto.validator.ValidFitnessFunction;
import org.fit.ssapp.dto.validator.ValidFitnessFunctionGT;
import org.fit.ssapp.dto.validator.ValidPayoffFunction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class GameTheoryProblemDto implements ProblemRequestDto {
  private SpecialPlayer specialPlayer;
  
  @Valid
  @NotEmpty(message = "Normal players list cannot be empty")
  private List<NormalPlayer> normalPlayers;
  
  private List<Conflict> conflictSet = new ArrayList<>();
  
  @NotNull(message = "Fitness function cannot be null")

  //@ValidFitnessFunctionGT
  private String fitnessFunction;
  
  @ValidPayoffFunction
  @NotNull(message = "Default payoff function cannot be null")
  private String defaultPayoffFunction;
  
  @NotNull(message = "Algorithm cannot be null")
  private String algorithm;
  
  
  private boolean isMaximizing;
  private String distributedCores;
  private Integer maxTime;
  private Integer generation;
  private Integer populationSize;

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
