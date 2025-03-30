package org.fit.ssapp.ss.gt.implement;

import static org.fit.ssapp.util.StringExpressionEvaluator.evaluateFitnessValue;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluatePayoffFunctionNoRelative;
import static org.fit.ssapp.util.StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.service.GameTheoryService;
import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.GameTheoryProblem;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.SpecialPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.fit.ssapp.ss.gt.result.GameSolution;
import org.fit.ssapp.util.NumberUtils;
import org.fit.ssapp.util.ProblemUtils;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

/**
 * Represents a standard game theory problem. This class holds the necessary information for game
 * theory calculations, including players, strategies, and payoff functions.
 */
@Data
@NoArgsConstructor
public class PsoCompatibleGameTheoryProblem implements GameTheoryProblem, Serializable {
  private SpecialPlayer specialPlayer;
  private List<NormalPlayer> normalPlayers;
  private List<NormalPlayer> oldNormalPlayers = new ArrayList<>();
  private List<Conflict> conflictSet = new ArrayList<>();
  
  /** Stores average pure payoff differences between strategies */
  private List<Double> playerAvgDiffs;
  
  private String fitnessFunction;
  private String defaultPayoffFunction;
  private boolean isMaximizing;

  /**
   * Returns a string representation of this game theory problem.
   * Includes details of each normal player and their strategies.
   *
   * @return A string containing formatted details of all normal players
   */
  @Override
  public String toString() {
    StringBuilder gameString = new StringBuilder();
    for (NormalPlayer normalPlayer : normalPlayers) {
      gameString
          .append("Normal player: ")
          .append(normalPlayers.indexOf(normalPlayer) + 1)
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
   * Sets the list of normal players and calculates their payoff values. If a player's payoff
   * function is null, it is replaced with the default. If the payoff function contains "P", it is
   * skipped.
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
        BigDecimal payoffValue = evaluatePayoffFunctionNoRelative(player
            .getStrategies()
            .get(i), payoffFunction);
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

  // SOLUTION = VARIABLE -> OBJECTIVE || CONSTRAINT

  @Override
  public void evaluate(Solution solution) {

    double[] payoffs = new double[solution.getNumberOfVariables()];

    int[] chosenStrategyIndices = new int[solution.getNumberOfVariables()];
    // chosenStrategyIndices[0] is the strategy index that normalPlayers[0] has chosen

    for (int i = 0; i < normalPlayers.size(); i++) {
      int chosenStrategyIndex = NumberUtils.toInteger((RealVariable) solution.getVariable(i));
      chosenStrategyIndices[i] = (chosenStrategyIndex);
    }

    for (int i = 0; i < conflictSet.size(); i++) {
      int leftPlayerIndex = conflictSet.get(i).getLeftPlayer();
      int rightPlayerIndex = conflictSet.get(i).getRightPlayer();
      int leftPlayerStrategy = conflictSet.get(i).getLeftPlayerStrategy();
      int rightPlayerStrategy = conflictSet.get(i).getRightPlayerStrategy();

      if (leftPlayerIndex == rightPlayerIndex && oldNormalPlayers.size() > i) {

        int prevStrategyIndex = oldNormalPlayers.get(i).getPrevStrategyIndex();
        int currentStrategyIndex = chosenStrategyIndices[leftPlayerIndex];

        boolean violated = (prevStrategyIndex == leftPlayerStrategy
            &&
            currentStrategyIndex == rightPlayerStrategy)
            ||
            (prevStrategyIndex == rightPlayerStrategy
                &&
                currentStrategyIndex == leftPlayerStrategy);

        if (violated) {
          //the player current strategy is conflict with his prev strategy in the previous iteration
          solution.setConstraint(i, -1); // this solution violates the constraints[i]
        }
      } else {
        if (chosenStrategyIndices[leftPlayerIndex - 1] == leftPlayerStrategy
            &&
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

      BigDecimal chosenStrategyPayoff;
      if (payoffFunction.contains("P")) {

        chosenStrategyPayoff = evaluatePayoffFunctionWithRelativeToOtherPlayers(
            chosenStrategy,
            payoffFunction,
            normalPlayers,
            chosenStrategyIndices);
      } else {
        chosenStrategyPayoff = normalPlayer
            .getPayoffValues()
            .get(chosenStrategyIndices[i]);
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

  @Override
  public Solution newSolution() {

    int numberOfNp = normalPlayers.size();
    Solution solution = new Solution(numberOfNp, 1, conflictSet.size());

    for (int i = 0; i < numberOfNp; i++) {
      NormalPlayer player = normalPlayers.get(i);
      RealVariable variable = new RealVariable(0, player.getStrategies().size() - 0.01);
      solution.setVariable(i, variable);
    }

    return solution;
  }

  @Override
  public void close() {
  }

  /**
   * The main method to execute the PSO-based game theory problem. It reads the problem from a file,
   * runs the OMOPSO algorithm, and prints the solution along with execution time.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    PsoCompatibleGameTheoryProblem problem = (PsoCompatibleGameTheoryProblem)
        ProblemUtils.readProblemFromFile(".data/gt_data_1.ser");
    if (Objects.isNull(problem)) {
      return;
    }
    long startTime = System.currentTimeMillis();
    NondominatedPopulation result = new Executor()
        .withProblem(problem)
        .withAlgorithm("OMOPSO")
        .withMaxEvaluations(100)
        .withProperty("populationSize", 1000)
        .distributeOnAllCores()
        .run();
    long endTime = System.currentTimeMillis();
    double runtime = ((double) (endTime - startTime) / 1000);
    runtime = Math.round(runtime * 100.0) / 100.0;
    System.out.println("Runtime: " + runtime);

    GameSolution solution = GameTheoryService.formatSolution(problem, result);
    System.out.println(solution);
  }

}
