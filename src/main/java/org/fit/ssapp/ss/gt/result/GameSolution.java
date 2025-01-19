package org.fit.ssapp.ss.gt.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.response.ComputerSpecs;


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


  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Player {

    private String playerName;
    private String strategyName;
    private double payoff;
  }
}
