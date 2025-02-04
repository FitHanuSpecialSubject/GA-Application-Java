package org.fit.ssapp.ss.gt.result;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a standard game theory problem. This class holds the necessary information for game
 * theory calculations, including players, strategies, and payoff functions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSolutionInsights {

  Map<String, List<Double>> fitnessValues;
  Map<String, List<Double>> runtimes;


}
