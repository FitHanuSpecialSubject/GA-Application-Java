package org.fit.ssapp.dto.response.functioncheck;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Response of a Game Theory function check: per-player payoff results, then the fitness.
 */
@Data
public class GtFunctionCheckResponse {

  /** Strategy profile used (0-based index per player). Null in payoff-only mode. */
  private int[] chosenStrategyIndices;

  /** One entry per player, in order. Empty in payoff-only mode. */
  private List<FunctionCheckResult> players = new ArrayList<>();

  /** Payoffs fed to the fitness function. */
  private List<Double> payoffs = new ArrayList<>();

  private FunctionCheckResult fitness;

  /** Value handed to MOEA: fitness, negated when maximizing. Null if fitness failed. */
  private Double objective;
}
