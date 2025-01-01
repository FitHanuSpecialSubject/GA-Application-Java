package org.fit.ssapp.ss.gt.implement;

import static org.fit.ssapp.util.StringExpressionEvaluator.evaluateFitnessValue;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluatePayoffFunctionNoRelative;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.SpecialPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;

/**
 * @General_Formula: STEPS TO FIND OUT THE BEST RESPONSE/NASH EQUILIBRIUM 1)Set up the game with
 * `load()` method 2)Define each player's set of strategies with `loadNormalPlayersFromFile()`
 * 3)Find and eliminate conflict strategies with `eliminateDominantStrategies()` 4)Find the pure
 * strategy Nash Equilibrium of the game 5)Find the mixed strategy Nash Equilibrium of the game
 **/
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

public class StandardGameTheoryProblem implements GameTheoryProblem, Serializable {

  private SpecialPlayer specialPlayer;
  private List<NormalPlayer> normalPlayers;
  private List<NormalPlayer> oldNormalPlayers = new ArrayList<>(); // this is for problem with dynamic data
  private List<Conflict> conflictSet = new ArrayList<>();

  //Store average pure payoff differences
  private List<Double> playerAvgDiffs;
  private String fitnessFunction;
  private String defaultPayoffFunction;
  private boolean isMaximizing;
  int[] bestResponses = new int[4];


  public StandardGameTheoryProblem() {
  }

  public StandardGameTheoryProblem(String path, int startRow) throws IOException {
    super();

    if (Objects.equals(path, "")) {
      System.err.println("INVALID INPUT PATH FOUND: Unable to generate Game Theory Problem");
      System.exit(-1);
    }

    eliminateConflictStrategies();
  }

  /**
   * @usage To remove all conflict strategies of conflict set declared in .xlsx file
   * @modifies normalPlayers
   * @algorithm Loop through conflict set -----------> set all strategies matching with strategies
   * in conflict set to null -----------> remove all null strategies in normalPlayers Conflict set
   * format: Left Player, Left Player Strategy, Right Player, Right Player Strategy
   */
  private void eliminateConflictStrategies() {
      if (conflictSet == null) {
          return;
      }

    for (Conflict conflict : conflictSet) {
      NormalPlayer evaluatingLeftPlayer = normalPlayers.get(conflict.getLeftPlayer());
      NormalPlayer evaluatingRightPlayer = normalPlayers.get(conflict.getRightPlayer());
      int leftConflictStrat = conflict.getLeftPlayerStrategy();
      int rightConflictStrat = conflict.getRightPlayerStrategy();

      // IF STRATEGY BELONG TO SPECIAL PLAYER -> DON'T REMOVE
      // Set conflict strategy of right player to null
        if (evaluatingLeftPlayer.getStrategyAt(leftConflictStrat) != null
                && conflict.getLeftPlayer() > -1) {
            evaluatingLeftPlayer.removeStrategiesAt(leftConflictStrat);
        }

      // Set conflict strategy of right player to null
        if (evaluatingRightPlayer.getStrategyAt(rightConflictStrat) != null
                && conflict.getRightPlayer() > -1) {
            evaluatingRightPlayer.removeStrategiesAt(rightConflictStrat);
        }

    }
    //Completely remove all inappropriate strategies from Evaluating Strategies
      for (NormalPlayer player : normalPlayers) {
          player.removeAllNull();
      }
  }

  private List<Double> buildPayoffGlopses(List<NormalPlayer> players) {

    List<Double> playerAvgDiffs = new ArrayList<>();
    for (NormalPlayer player : players) {
      double playerAvgDiff = 0;
      for (NormalPlayer opponent : players) {
        playerAvgDiff += Math.abs(player.getPurePayoff() - opponent.getPurePayoff());
      }
      playerAvgDiff /= normalPlayers.size();
      playerAvgDiffs.add(playerAvgDiff);
    }
    return playerAvgDiffs;
  }

  /**
   * @usage To get the smallest average pure payoff difference among players
   * @algorithm 1) Loop through normalPlayers and calculate player average differences
   * ______________with formula |playerPayoff - opponentPayoff| / normalPlayers.size() ___________2)
   * Save result in a list ___________3) Assign result list to playerAvgDiffs property
   */
  private double computeNashEquilibrium() {
    double nash;
    List<Double> playerAvgDiffs = buildPayoffGlopses(normalPlayers);
    nash = Collections.min(playerAvgDiffs);

      if (specialPlayer != null) {
          nash = Math.abs(nash - specialPlayer.getPayoff());
      }
    this.playerAvgDiffs = playerAvgDiffs;

    return nash;
  }


  public boolean isMaximizing() {
    return isMaximizing;
  }

  public void setMaximizing(boolean maximizing) {
    isMaximizing = maximizing;
  }

  public List<NormalPlayer> getNormalPlayers() {
    return normalPlayers;
  }

  /**
   * @usage To get player index with the highest pure payoff
   * @algorithm 1) Save all pure payoff values to a list ___________2) Get max value in payoffs list
   * ___________3) Return max value index in list (Since pure payoff index == player index)
   */
  public int getDominantPlayerIndex() {
    List<Double> payoffs = new ArrayList<>();
    normalPlayers.forEach(player -> payoffs.add(player.getPurePayoff()));
    double max = Collections.max(payoffs);
    return payoffs.indexOf(max);
  }

  /**
   * @usage Get user with the best response strategy ----> The lower payoff average difference, the
   * more equilibrium strategy is
   */
  public int getBestResponse() {
    return playerAvgDiffs.indexOf(Collections.min(playerAvgDiffs));
  }

  public int[] getRemainAlliances() {
    int[] bestResponse = new int[normalPlayers.size()];
    Arrays.fill(bestResponse, 2);
    int bestPlayerIndex = getBestResponse();
    int bestStrategyIndex = normalPlayers.get(bestPlayerIndex).getDominantStrategyIndex();
    bestResponse[bestPlayerIndex] = bestStrategyIndex;

    if (bestStrategyIndex == normalPlayers.size() - 1) {
        for (double p : bestResponse) {
            p = bestStrategyIndex;
        }
    } else {
      for (int i = 0; i < normalPlayers.size(); ++i) {
        int upperBound = normalPlayers.size() - i;
          if (bestStrategyIndex == i) {
              bestResponses[i] =
                      playerAvgDiffs.indexOf(Collections.min(playerAvgDiffs)) / upperBound;
          } else {
              bestResponses[i] = 2;
          }
      }
    }
    return bestResponse;
  }

  /**
   * @usage To set up evaluator for MOEA model every single time Evaluation run
   */


  public String getDefaultPayoffFunction() {
    return defaultPayoffFunction;
  }

  public void setDefaultPayoffFunction(String defaultPayoffFunction) {
    this.defaultPayoffFunction = defaultPayoffFunction;
  }

  public int[] getBestResponses() {
    return bestResponses;
  }

  public void setBestResponses(int[] bestResponses) {
    this.bestResponses = bestResponses;
  }

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

  public SpecialPlayer getSpecialPlayer() {
    return specialPlayer;
  }

  public void setSpecialPlayer(SpecialPlayer specialPlayer) {
    this.specialPlayer = specialPlayer;
  }

  public void setNormalPlayers(List<NormalPlayer> normalPlayers) {
    this.normalPlayers = normalPlayers;

    for (NormalPlayer player : normalPlayers) {
      String payoffFunction = player.getPayoffFunction();
      if (payoffFunction == null) {
        payoffFunction = defaultPayoffFunction;
      }

      // if the payoff function is relative to other players, then it must be calculated in the evaluation
      if (payoffFunction.contains("P")) {
        continue;
      }

      // if the payoff function is relative to the player itself, then it can be calculated in the initialization
      List<BigDecimal> payoffValues = new ArrayList<>();
      for (int i = 0; i < player.getStrategies().size(); ++i) {
        BigDecimal payoffValue = evaluatePayoffFunctionNoRelative(player.getStrategies().get(i),
            payoffFunction);
        payoffValues.add(payoffValue);
      }
      player.setPayoffValues(payoffValues);

    }
  }

  public List<Conflict> getConflictSet() {
    return conflictSet;
  }

  public void setConflictSet(List<Conflict> conflictSet) {
    this.conflictSet = conflictSet;
  }

  public List<Double> getPlayerAvgDiffs() {
    return playerAvgDiffs;
  }

  public void setPlayerAvgDiffs(List<Double> playerAvgDiffs) {
    this.playerAvgDiffs = playerAvgDiffs;
  }

  public String getFitnessFunction() {
    return fitnessFunction;
  }

  public void setFitnessFunction(String fitnessFunction) {
    this.fitnessFunction = fitnessFunction;
  }

  public List<NormalPlayer> getOldNormalPlayers() {
    return oldNormalPlayers;
  }

  public void setOldNormalPlayers(List<NormalPlayer> oldNormalPlayers) {
    this.oldNormalPlayers = oldNormalPlayers;
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
//        System.out.println("Evaluating " + count++);
    double[] payoffs = new double[solution.getNumberOfVariables()];

    int[] chosenStrategyIndices = new int[solution.getNumberOfVariables()];
    // chosenStrategyIndices[0] is the strategy index that normalPlayers[0] has chosen

    for (int i = 0; i < normalPlayers.size(); i++) {
      BinaryIntegerVariable chosenStrategyIndex = (BinaryIntegerVariable) solution.getVariable(i);
      chosenStrategyIndices[i] = (chosenStrategyIndex.getValue());
    }

    // check if the solution violates any constraint
    for (int i = 0; i < conflictSet.size(); i++) {
      int leftPlayerIndex = conflictSet.get(i).getLeftPlayer();
      int rightPlayerIndex = conflictSet.get(i).getRightPlayer();
      int leftPlayerStrategy = conflictSet.get(i).getLeftPlayerStrategy();
      int rightPlayerStrategy = conflictSet.get(i).getRightPlayerStrategy();

      // if 2 player indices are the same, meaning they are the same player and this is an old player
      if (leftPlayerIndex == rightPlayerIndex && oldNormalPlayers.size() > i) {
        // this conflict is between 2 different strategies of the same player at 2 iterations, (for problem with dynamic data)
        int prevStrategyIndex = oldNormalPlayers.get(i).getPrevStrategyIndex();
        int currentStrategyIndex = chosenStrategyIndices[leftPlayerIndex];

        // if the prevStrategyIndex is one of 2 conflict strategies, and the currentStrategyIndex is the other one
        boolean violated =
            (prevStrategyIndex == leftPlayerStrategy && currentStrategyIndex == rightPlayerStrategy)
                ||
                (prevStrategyIndex == rightPlayerStrategy
                    && currentStrategyIndex == leftPlayerStrategy);

        if (violated) {
          //the player current strategy is conflict with his prev strategy in the previous iteration
          solution.setConstraint(i, -1); // this solution violates the constraints[i]
        }
      } else {
        // this conflict is between 2 strategies of the 2 players at the a iteration
        if (chosenStrategyIndices[leftPlayerIndex - 1] == leftPlayerStrategy &&
            chosenStrategyIndices[rightPlayerIndex - 1] == rightPlayerStrategy) {
          solution.setConstraint(i, -1); // this solution violates the constraints[i]
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

      BigDecimal chosenStrategyPayoff = new BigDecimal(0);
      if (payoffFunction.contains("P")) {
        // if the payoff function is relative to other players, then it must be calculated in the evaluation

        chosenStrategyPayoff
            = evaluatePayoffFunctionWithRelativeToOtherPlayers(chosenStrategy,
            payoffFunction,
            normalPlayers,
            chosenStrategyIndices);
      } else {
        // if the payoff function is relative to the player itself, then it can be calculated in the initialization
        chosenStrategyPayoff = normalPlayer.getPayoffValues().get(chosenStrategyIndices[i]);
      }

      chosenStrategy.setPayoff(chosenStrategyPayoff.doubleValue());
      payoffs[i] = chosenStrategyPayoff.doubleValue();
    }

    BigDecimal fitnessValue
        = evaluateFitnessValue(
        payoffs,
        fitnessFunction
    );

    if (isMaximizing) {
      fitnessValue = fitnessValue.negate(); // because the MOEA Framework only support minimization, for maximization problem, we need to negate the fitness value
    }

    solution.setObjective(0, fitnessValue.doubleValue());

  }

  // SOLUTION = VARIABLE -> OBJECTIVE || CONSTRAINT

  @Override
  public Solution newSolution() {

    // the variables[0] is the strategy index of each normalPlayers[0] choose
    // the variable 1 is the strategy index of each player1 choose
    // the variable 2 is the strategy index of each player2 choose
    // ..

    int numbeOfNP = normalPlayers.size();
    Solution solution = new Solution(numbeOfNP, 1, conflictSet.size());

    for (int i = 0; i < numbeOfNP; i++) {
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
