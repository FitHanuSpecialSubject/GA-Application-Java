package org.fit.ssapp.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.SpecialPlayer;
import org.fit.ssapp.dto.validator.ValidFitnessFunction;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

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
  
  @NotNull(message = "normalPlayers is required")
  @NotEmpty(message = "normalPlayers cannot be empty")
  private List<NormalPlayer> normalPlayers;
  
  private List<Conflict> conflictSet = new ArrayList<>();
  
  @ValidFitnessFunction
  private String fitnessFunction;
  
  private String defaultPayoffFunction;
  private String algorithm;
  private boolean isMaximizing;
  private String distributedCores;
  
  @Min(value = 1, message = "maxTime must be at least 1")
  private Integer maxTime;
  
  @Min(value = 1, message = "generation must be at least 1")
  private Integer generation;
  
  @Min(value = 1, message = "populationSize must be at least 1")
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
