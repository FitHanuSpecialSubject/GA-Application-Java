package org.fit.ssapp.ss.gt;

import java.util.List;
import org.moeaframework.core.Problem;

/**
 * Sets the default payoff function for the game.
 */
public interface GameTheoryProblem extends Problem {

  /**
   * Sets the fitness function for evaluating solutions in the game.
   */
  void setDefaultPayoffFunction(String payoffFunction);

  /**
   * Sets the special player in the game.
   */

  void setFitnessFunction(String fitnessFunction);

  /**
   * Sets the list of normal players in the game.
   */

  void setSpecialPlayer(SpecialPlayer specialPlayer);

  /**
   * Defines whether the game is maximizing or minimizing the payoff.
   */

  void setNormalPlayers(List<NormalPlayer> normalPlayers);

  /**
   * Gets the list of normal players in the game.
   */

  void setConflictSet(List<Conflict> conflictSet);

  /**
   * Sets the default payoff function for the game.
   */

  void setMaximizing(boolean isMaximizing);

  /**
   * Sets the default payoff function for the game.
   */

  List<NormalPlayer> getNormalPlayers();

}
