package org.fit.ssapp.ss.smt.implement;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.MatchingProblem;
import org.fit.ssapp.ss.smt.evaluator.FitnessEvaluator;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.PreferenceListWrapper;
import org.fit.ssapp.util.StringUtils;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.Permutation;

/**
 * Represents a Many-to-Many Stable Matching Problem.
 * This class models a matching problem where multiple individuals must be paired optimally
 * based on their preference lists. The goal is to find a stable
 * matching while optimizing a fitness function.
 * Many: Each participant can match with multiple partners
 * One: Each participant can only have one pair
 * Example for many to many match:
 * Set 1: a, b, c, d
 * Set 2: x, y, z
 * Available match: x-a, x-b, x-c, a-x, a-y, b-z, ...
 * Wrong when an individual pairs with one also in the same Set
 */
@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MTMProblem implements MatchingProblem {

  /**
   * problem name.
   */
  final String problemName;

  /**
   * problem size (number of individuals in matching problem.
   */
  final int problemSize;

  /**
   * number of set in matching problem.
   */
  final int setNum;

  /**
   * Matching data.
   */
  final MatchingData matchingData;

  /**
   * preference list.
   */
  final PreferenceListWrapper preferenceLists;

  /**
   * problem fitness function.
   */
  final String fitnessFunction;

  /**
   * fitness evaluator.
   */
  final FitnessEvaluator fitnessEvaluator;

  /**
   * will not be used.
   */
  final int UNUSED_VAL = StableMatchingConst.UNUSED_VALUE;


  /**
   * generate new solution.
   *
   * @return Solution contains Variable(s)
   */
  @Override
  public Solution newSolution() {
    Solution solution = new Solution(1, 1);
    Permutation permutationVar = new Permutation(problemSize);
    solution.setVariable(0, permutationVar);
    return solution;
  }

  /**
   * evaluate function for matching problem.
   *
   * @param solution Solution contains Variable(s)
   */
  @Override
  public void evaluate(Solution solution) {
    Matches result = this.stableMatching(solution.getVariable(0));
    // Check Exclude Pairs
    int[][] excludedPairs = this.matchingData.getExcludedPairs();
    if (Objects.nonNull(excludedPairs)) {
      for (int[] excludedPair : excludedPairs) {
        if (result.isMatched(excludedPair[0], excludedPair[1])) {
          solution.setObjective(0, Double.MAX_VALUE);
          return;
        }
      }
    }
    double[] satisfactions = this.preferenceLists.getMatchesSatisfactions(result, matchingData);
    double fitnessScore;
    if (this.hasFitnessFunc()) {
      fitnessScore = fitnessEvaluator
          .withFitnessFunctionEvaluation(satisfactions, this.fitnessFunction);
    } else {
      fitnessScore = fitnessEvaluator.defaultFitnessEvaluation(satisfactions);
    }
    solution.setAttribute(StableMatchingConst.MATCHES_KEY, result);
    solution.setObjective(0, -fitnessScore);
  }


  /**
   * check exists fitness function.
   *
   * @return true if exists
   */
  public boolean hasFitnessFunc() {
    return !StringUtils.isEmptyOrNull(this.fitnessFunction);
  }

  /**
   * calculate matches satisfaction.
   *
   * @return double[]
   */
  public double[] getMatchesSatisfactions(Matches matches) {
    return this.preferenceLists.getMatchesSatisfactions(matches, matchingData);
  }

  /**
   * stableMatching.
   *
   * @return Matches
   */
  @Override
  public Matches stableMatching(Variable var) {
    Matches matches = new Matches(matchingData.getSize());
    int[] decodeVar = EncodingUtils.getPermutation(var);
    Queue<Integer> queue = new LinkedList<>();

    for (int val : decodeVar) {
      queue.add(val);
    }

    while (!queue.isEmpty()) {
      int leftNode = queue.poll();
      if (matches.isMatched(leftNode)) {
        continue;
      }

      //Get preference list of proposing node
      PreferenceList nodePreference = preferenceLists.get(leftNode);

      //Loop through LeftNode's preference list to find a Match
      for (int i = 0; i < nodePreference.size(UNUSED_VAL); i++) {
        int rightNode = nodePreference.getPositionByRank(UNUSED_VAL, i);

        if (matches.isMatched(rightNode, leftNode)) {
          continue;
        }

        boolean rightIsFull = matches.isFull(rightNode, this.matchingData.getCapacityOf(rightNode));

        if (!rightIsFull) {
          matches.addMatchBi(leftNode, rightNode);
          break;
        }

        // The node that rightNode has the least preference considering
        // its currents matches and leftNode
        int rightLoser = preferenceLists.getLeastScoreNode(
                UNUSED_VAL,
                rightNode,
                leftNode,
                matches.getSetOf(rightNode),
                matchingData.getCapacityOf(rightNode));

        // rightNode likes its current matches more than leftNode
        if (rightLoser == leftNode) {
          // if leftNode like rightNode the least
          if (preferenceLists.getLastChoiceOf(UNUSED_VAL, leftNode) == rightNode) {
            break;
          }
        } else {
          matches.removeMatchBi(rightNode, rightLoser);
          matches.addMatchBi(leftNode, rightNode);
          queue.add(rightLoser);
          break;
        }
      }
    }

    return matches;
  }

  @Override
  public String getMatchingTypeName() {
    return "Many to Many";
  }

  @Override
  public String getName() {
    return this.problemName;
  }

  @Override
  public int getNumberOfConstraints() {
    return 0;
  }

  @Override
  public int getNumberOfObjectives() {
    return 1;
  }

  @Override
  public int getNumberOfVariables() {
    return 1;
  }

  @Override
  public void close() {
  }
}
