package org.fit.ssapp.dto.request.functioncheck;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.ss.gt.NormalPlayer;

/**
 * Request for checking Game Theory payoff / fitness functions without running MOEA.
 * <p>
 * Two ways to provide input:
 * <ul>
 *   <li><b>Dataset mode</b>: send {@code normalPlayers} (parsed from the uploaded excel or typed
 *   manually) plus {@code chosenStrategyIndices} (the "strategy profile" = which strategy each
 *   player picks, 0-based). Payoffs are computed from the payoff functions, then the fitness
 *   function is computed from those payoffs.</li>
 *   <li><b>Payoff-only mode</b>: send {@code payoffs} directly to test just the fitness
 *   function (no players needed).</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GtFunctionCheckRequest {

  /** Players with their strategies. Optional when {@code payoffs} is given. */
  private List<NormalPlayer> normalPlayers;

  /** Chosen strategy index (0-based) per player. Defaults to 0 for every player. */
  private int[] chosenStrategyIndices;

  /** Payoff function used by players that do not define their own. */
  private String defaultPayoffFunction;

  /** Fitness function (variables u1, u2, ... = payoff of player 1, 2, ...). */
  private String fitnessFunction;

  /** If true the objective handed to MOEA is the negated fitness (see GT problem classes). */
  private boolean maximizing;

  /** Manual payoffs, used when {@code normalPlayers} is absent. */
  private double[] payoffs;
}
