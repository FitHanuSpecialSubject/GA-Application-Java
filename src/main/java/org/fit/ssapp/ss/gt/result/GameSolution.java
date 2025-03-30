package org.fit.ssapp.ss.gt.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.response.ComputerSpecs;

/**
 * Represents a standard game theory problem. This class holds the necessary information for game
 * theory calculations, including players, strategies, and payoff functions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSolution {

  private double fitnessValue;
  private List<Player> players;
  private String algorithm;
  private double runtime;
  private ComputerSpecs computerSpecs;

  /**
   * Represents a standard game theory problem. This class holds the necessary information for game
   * theory calculations, including players, strategies, and payoff functions.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Player {

    private int index;
    private int strategy;
    private double payoff;
  }
}
