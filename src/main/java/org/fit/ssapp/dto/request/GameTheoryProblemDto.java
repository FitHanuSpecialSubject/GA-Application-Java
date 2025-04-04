package org.fit.ssapp.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.SpecialPlayer;

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
  private List<NormalPlayer> normalPlayers;
  private List<Conflict> conflictSet = new ArrayList<>();
  private String fitnessFunction;
  private String defaultPayoffFunction;
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
