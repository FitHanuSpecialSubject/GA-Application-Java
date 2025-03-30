package org.fit.ssapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class GameTheoryProblemDto extends ProblemDto {
  private double[][][] normalPlayers;
  private int[][] conflictSet;
  private boolean isMaximizing;
  private String defaultPayoffFunction;
  private String fitnessFunction;

  @Override
  public String toString() {
    return "GameTheoryProblemDto{" + ", normalPlayers="
        + normalPlayers + ", conflictSet=" + ", fitnessFunction='" + conflictSet + fitnessFunction
        + '\'' + ", defaultPayoffFunction='" + defaultPayoffFunction + '\'' + ", algorithm='"
        + algorithm + '\'' + ", isMaximizing=" + isMaximizing + ", distributedCores='"
        + distributedCores + '\'' + ", maxTime=" + maxTime + ", generation=" + generation
        + ", populationSize=" + populationSize + '}';
  }
}
