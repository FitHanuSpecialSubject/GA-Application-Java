package org.fit.ssapp.ss.smt.implement;

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
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.fit.ssapp.util.SolutionUtils;
import org.fit.ssapp.util.StringUtils;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.RealVariable;

import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * The idea is N:problem size going to be the number of dimensions in PSO
 */
@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PsoCompatMtmProblem implements MatchingProblem {

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
    Solution solution = new Solution(this.problemSize, 1);
    for (int i = 0; i < this.problemSize; i++) {
      RealVariable var = new RealVariable(-1, 1);
      solution.setVariable(i, var);
    }
    return solution;
  }

  /**
   * evaluate function for matching problem.
   *
   * @param solution Solution contains Variable(s)
   */
  @Override
  public void evaluate(Solution solution) {
    Matches result = this.stableMatching(solution);
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
      fitnessScore = fitnessEvaluator.withFitnessFunctionEvaluation(satisfactions,
          this.fitnessFunction);
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
    return null;
  }

  /**
   * stableMatching.
   *
   * @return Matches
   */
  public Matches stableMatching(Solution solution) {
    Queue<Integer> queue = SolutionUtils.getSortedIds(solution, true);

    Matches matches = new Matches(matchingData.getSize());

    while (!queue.isEmpty()) {
      int leftNode = queue.poll();
      if (matches.isMatched(leftNode)) {
        continue;
      }

      TwoSetPreferenceList nodePreference = (TwoSetPreferenceList) preferenceLists.get(leftNode);
      for (int rightNode : nodePreference.getScores().keySet()) {

        Set<Integer> currentMatchesOfLeftNode = matches.getSetOf(leftNode);
        Set<Integer> currentMatchesOfRightNode = matches.getSetOf(rightNode);

        boolean leftIsFull = matches.isFull(leftNode, matchingData.getCapacityOf(leftNode));
        boolean rightIsFull = matches.isFull(rightNode, matchingData.getCapacityOf(rightNode));

        // if both left and right are not null: match
        if (!leftIsFull && !rightIsFull) {
          matches.addMatchBi(leftNode, rightNode);
          continue;
        }

        // if left is full and left does not like current right more: continue
        if (leftIsFull) {
          int leastPreferredLeftMatch = nodePreference.getLeastNode(UNUSED_VAL, rightNode, currentMatchesOfLeftNode);
          if (leastPreferredLeftMatch == rightNode) {
            continue;
          }
        }

        // if right is full and right does not like left more: continue
        if (rightIsFull) {
          TwoSetPreferenceList rightNodePreference = (TwoSetPreferenceList) preferenceLists.get(rightNode);
          int leastPreferredRightMatch = rightNodePreference.getLeastNode(UNUSED_VAL, leftNode, currentMatchesOfRightNode);
          if (leastPreferredRightMatch == leftNode) {
            continue;
          }
        }

        //  left prefer new right more: match
        if (leftIsFull) {
          int leastPreferredLeftMatch = nodePreference.getLeastNode(UNUSED_VAL, rightNode, currentMatchesOfLeftNode);
          matches.removeMatchBi(leftNode, leastPreferredLeftMatch);
          matches.addMatchBi(leftNode, rightNode);
          queue.add(leastPreferredLeftMatch);
        }

        //  right prefer new left more: match
        if (rightIsFull) {
          TwoSetPreferenceList rightNodePreference = (TwoSetPreferenceList) preferenceLists.get(rightNode);
          int leastPreferredRightMatch = rightNodePreference.getLeastNode(UNUSED_VAL, leftNode, currentMatchesOfRightNode);
          matches.removeMatchBi(rightNode, leastPreferredRightMatch);
          matches.addMatchBi(leftNode, rightNode);
          queue.add(leastPreferredRightMatch);
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
    return problemSize;
  }

  @Override
  public void close() {
  }

}
