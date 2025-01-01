package org.fit.ssapp.ss.gt.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.response.ComputerSpecs;
import org.fit.ssapp.util.ComputerSpecsUtil;

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

  public ComputerSpecs getComputerSpecs() {
    return ComputerSpecsUtil.getComputerSpecs();
  }

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
