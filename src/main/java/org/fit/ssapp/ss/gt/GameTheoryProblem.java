package org.fit.ssapp.ss.gt;

import java.util.List;
import org.moeaframework.core.Problem;

public interface GameTheoryProblem extends Problem {

  void setDefaultPayoffFunction(String payoffFunction);

  void setFitnessFunction(String fitnessFunction);

  void setSpecialPlayer(SpecialPlayer specialPlayer);

  void setNormalPlayers(List<NormalPlayer> normalPlayers);

  void setConflictSet(List<Conflict> conflictSet);

  void setMaximizing(boolean isMaximizing);

  List<NormalPlayer> getNormalPlayers();

}
