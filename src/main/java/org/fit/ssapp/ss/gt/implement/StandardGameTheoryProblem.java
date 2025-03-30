package org.fit.ssapp.ss.gt.implement;

import static org.fit.ssapp.util.StringExpressionEvaluator.evaluateFitnessValue;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluatePayoffFunctionNoRelative;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;

//--------------------------------------------------------------------------
/* *
 * WARNING: DO NOT CHANGE ORDER OF StandardGameTheoryProblem CONSTRUCTOR
 * PRECAUTION: THIS WOULD ONLY SOLVE PROBLEM FOR COORDINATING PROBLEMS

 *  We will try to solve to equations of type : ( find p and q )
        a*p=b
        c*q=d
        ( How we got this a,b,c and d coefficient ? )
        for example :
                      q                  1-q
                      C                   D
             ---------------------------------------------
    p      A        (3,-3)       |        (-2,2)
             --------------------------------------------
    1-p    B        (-1,1)       |         (0,0)
             ---------------------------------------------
        If we apply the definition of mixed strategy Nash Equilibrium we will get :
        For Player 1 :
        -3p + 1(1-p) = 2p + 0(1-p)
        For Player 2 :
        3q + (-2)(1-q) = -1q + 0(1-q)
        So our goal is to solve this linear system !
        Let's make it like the format in the start of this example :
        (-3-1-2+0)p = -1 + 0
        (3+2+1+0)q  = +2 + 0
        Cool we have now our a,b,c and d coefficient ! let's do our thing :
        p = 1/6
        q = 1/3
*/

/**
 * This class represents a standard game theory problem. It implements the {@link GameTheoryProblem}
 * interface and supports serialization.
 */
@Getter
@Setter
public class StandardGameTheoryProblem implements GameTheoryProblem, Serializable {
  private List<NormalPlayer> normalPlayers;
  private List<NormalPlayer> oldNormalPlayers = new ArrayList<>();
  private List<Conflict> conflictSet = new ArrayList<>();

  //Store average pure payoff differences
  private List<Double> playerAvgDiffs;
  private String fitnessFunction;
  private String defaultPayoffFunction;
  private boolean isMaximizing;

  /**
   * Default constructor for StandardGameTheoryProblem. Initializes an empty game theory problem.
   */
  public StandardGameTheoryProblem() {
  }

  public boolean isMaximizing() {
    return isMaximizing;
  }

  public void setMaximizing(boolean maximizing) {
    isMaximizing = maximizing;
  }


  public int getBestResponse() {
    return playerAvgDiffs.indexOf(Collections.min(playerAvgDiffs));
  }

  /**
   * Returns a string representation of the game theory problem. It includes details of all normal
   * players.
   *
   * @return A formatted string containing details of normal players.
   */
  public String toString() {
    StringBuilder gameString = new StringBuilder();
    for (NormalPlayer normalPlayer : normalPlayers) {
      gameString.append("Normal player: ").append(normalPlayers.indexOf(normalPlayer) + 1)
          .append(normalPlayer);
      gameString.append("\n----------------\n");
    }
    return gameString.toString();
  }

  @Override
  public String getName() {
    return "Standard Game Theory Problem";
  }

  @Override
  public int getNumberOfVariables() {
    return normalPlayers.size();
  }

  /**
   * Sets the list of normal players and initializes their payoff values. If a player's payoff
   * function is null, it will be assigned the default payoff function. If the payoff function is
   * relative to other players (contains "P"), it will be skipped. Otherwise, payoff values will be
   * calculated and assigned.
   *
   * @param normalPlayers List of normal players to be set.
   */
  public void setNormalPlayers(List<NormalPlayer> normalPlayers) {
    this.normalPlayers = normalPlayers;
    for (NormalPlayer player : normalPlayers) {
      String payoffFunction = player.getPayoffFunction();
      if (payoffFunction == null) {
        payoffFunction = defaultPayoffFunction;
      }
      if (payoffFunction.contains("P")) {
        continue;
      }
      List<BigDecimal> payoffValues = new ArrayList<>();
      for (int i = 0; i < player.getStrategies().size(); ++i) {
        BigDecimal payoffValue = evaluatePayoffFunctionNoRelative(player.getStrategies().get(i),
            payoffFunction);
        payoffValues.add(payoffValue);
      }
      player.setPayoffValues(payoffValues);

    }
  }

  @Override
  public int getNumberOfObjectives() {
    return 1;
  }

  @Override
  public int getNumberOfConstraints() {
    return conflictSet.size();
  }

  @Override
  public void evaluate(Solution solution) {
    double[] payoffs = new double[solution.getNumberOfVariables()];

    int[] chosenStrategyIndices = new int[solution.getNumberOfVariables()];
    // chosenStrategyIndices[0] is the strategy index that normalPlayers[0] has chosen

    for (int i = 0; i < normalPlayers.size(); i++) {
      BinaryIntegerVariable chosenStrategyIndex = (BinaryIntegerVariable) solution.getVariable(i);
      chosenStrategyIndices[i] = chosenStrategyIndex.getValue();
    }

    // check if the solution violates any constraint
    for (int i = 0; i < conflictSet.size(); i++) {
      int leftPlayerIndex = conflictSet.get(i).getLeftPlayer();
      int rightPlayerIndex = conflictSet.get(i).getRightPlayer();
      int leftPlayerStrategy = conflictSet.get(i).getLeftPlayerStrategy();
      int rightPlayerStrategy = conflictSet.get(i).getRightPlayerStrategy();

      if (leftPlayerIndex == rightPlayerIndex && oldNormalPlayers.size() > i) {

        int prevStrategyIndex = oldNormalPlayers.get(i).getPrevStrategyIndex();
        int currentStrategyIndex = chosenStrategyIndices[leftPlayerIndex];

        boolean violated =
                (prevStrategyIndex == leftPlayerStrategy && currentStrategyIndex == rightPlayerStrategy)
                        ||
                        (prevStrategyIndex == rightPlayerStrategy
                                && currentStrategyIndex == leftPlayerStrategy);

        if (violated) {

          solution.setConstraint(i, -1);
        }
      } else {
        // this conflict is between 2 strategies of the 2 players at the iteration
        if (chosenStrategyIndices[leftPlayerIndex - 1] == leftPlayerStrategy
            &&
            chosenStrategyIndices[rightPlayerIndex - 1] == rightPlayerStrategy) {
          solution.setConstraint(i, -1);
        }
      }
    }

    // calculate the payoff of the strategy each player has chosen
    for (int i = 0; i < normalPlayers.size(); i++) {
      NormalPlayer normalPlayer = normalPlayers.get(i);
      Strategy chosenStrategy = normalPlayer.getStrategyAt(chosenStrategyIndices[i]);

      String payoffFunction = normalPlayer.getPayoffFunction();
      // if the player does not have his own payoff function, use the default one
      if (payoffFunction == null) {
        payoffFunction = defaultPayoffFunction;
      }

      BigDecimal chosenStrategyPayoff;
      if (payoffFunction.contains("P")) {

        chosenStrategyPayoff
                = evaluatePayoffFunctionWithRelativeToOtherPlayers(chosenStrategy,
                payoffFunction,
                normalPlayers,
                chosenStrategyIndices);
      } else {
        chosenStrategyPayoff = normalPlayer.getPayoffValues().get(chosenStrategyIndices[i]);
      }

      chosenStrategy.setPayoff(chosenStrategyPayoff.doubleValue());
      payoffs[i] = chosenStrategyPayoff.doubleValue();
    }

    BigDecimal fitnessValue = evaluateFitnessValue(payoffs, fitnessFunction);

    if (isMaximizing) {
      fitnessValue = fitnessValue.negate();
    }

    solution.setObjective(0, fitnessValue.doubleValue());
  }

  // SOLUTION = VARIABLE -> OBJECTIVE || CONSTRAINT

  @Override
  public Solution newSolution() {

    // the variables[0] is the strategy index of each normalPlayers[0] choose
    // the variable 1 is the strategy index of each player1 choose
    // the variable 2 is the strategy index of each player2 choose
    // .

    int numberOfNp = normalPlayers.size();
    Solution solution = new Solution(numberOfNp, 1, conflictSet.size());

    for (int i = 0; i < numberOfNp; i++) {
      NormalPlayer player = normalPlayers.get(i);
      BinaryIntegerVariable variable = new BinaryIntegerVariable(0,
          player.getStrategies().size() - 1);

      solution.setVariable(i, variable);
    }

    return solution;
  }


  @Override
  public void close() {
  }
}
