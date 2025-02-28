package org.fit.ssapp.ss.smt.implement;

import java.util.*;

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
import org.fit.ssapp.ss.smt.implement.var.CustomIntegerVariable;
import org.fit.ssapp.ss.smt.implement.var.CustomVariation;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.PreferenceListWrapper;
import org.fit.ssapp.util.StringUtils;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.Permutation;

/**
 * Represents a One-to-Many Stable Matching Problem.
 * This class models a matching problem where multiple individuals must be paired optimally
 * based on their preference lists. The goal is to find a stable
 * matching while optimizing a fitness function.
 * Many: Each participant can match with multiple partners
 * One: Each participant can only have one pair
 * Set 1: a, b, c, d
 * Set 2: x, y, z
 * Example matching for set 1 (One): a-x, b-y, c-z
 * Example matching for set 2 (Many): x-a, x-d, y-b, y-c
 *  * Correct: a-x (x-a, x-b, x-c), b-y (y-b, y-d)
 *  * Wrong: b-y (y-b) + b-x
 */
@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OTMProblem implements MatchingProblem {

  /**
   * problem name.
   */
  final String problemName;

  /**
   * problem size (number of individuals in matching problem).
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

  @Override
  public String getName() {
    return problemName;
  }

  @Override
  public int getNumberOfVariables() {
    return problemSize;
  }

  @Override
  public int getNumberOfObjectives() {
    return 1;
  }

  @Override
  public int getNumberOfConstraints() {
    return 0;
  }

  @Override
  public void evaluate(Solution solution) {
    int[] decodeVar = new int[problemSize];
    for (int i = 0; i < problemSize; i++) {
      CustomIntegerVariable var = (CustomIntegerVariable) solution.getVariable(i);
      decodeVar[i] = (int) Math.round(var.getValue());
    }
    Matches result = this.stableMatching(decodeVar);
    // Check Exclude Pairs
    int[][] excludedPairs = this.matchingData.getExcludedPairs();
    if (Objects.nonNull(excludedPairs)) {
      for (int[] excludedPair : excludedPairs) {
        if (result.getSetOf(excludedPair[0]).contains(excludedPair[1])) {
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
   * hasFitnessFunc.
   *
   * @return boolean
   */
  public boolean hasFitnessFunc() {
    return !this.fitnessFunction.equalsIgnoreCase("default") && !StringUtils.isEmptyOrNull(
            this.fitnessFunction);
  }

  @Override
  public Solution newSolution() {
    Solution solution = new Solution(problemSize, 1);

    int[] queue = new Permutation(problemSize).toArray();

    for (int i = 0; i < queue.length; i++) {
      CustomIntegerVariable var = new CustomIntegerVariable(0, problemSize-1);
      var.setValue(queue[i]);
      solution.setVariable(i, var);
    }

    return solution;
  }

  @Override
  public void close() {

  }

  @Override
  public String getMatchingTypeName() {
    return "One-to-Many Matching Problem";
  }

  @Override
  public MatchingData getMatchingData() {
    return matchingData;
  }

  @Override
  public Matches stableMatching(Variable var) {
    return null ;
  }

  /**
   *  refactor from permutation to array of integer
   * @param decodeVar int[]
   * @return Matches
   */
  public Matches stableMatching(int[] decodeVar) {
    Matches matches = new Matches(matchingData.getSize());
    Queue<Integer> queue = new LinkedList<>();
    for (int val : decodeVar) {
      queue.add(val);
    }
    while (!queue.isEmpty()) {
      int leftNode = queue.poll();
      if (matches.isMatched(leftNode)) {
        continue;
      }
      PreferenceList nodePreference = preferenceLists.get(leftNode);
      for (int i = 0; i < nodePreference.size(UNUSED_VAL); i++) {
        int rightNode = nodePreference.getPositionByRank(UNUSED_VAL, i);
        if (matches.isMatched(rightNode, leftNode)) {
          continue;
        }
        boolean rightIsFull = matches.isFull(rightNode, matchingData.getCapacityOf(rightNode));
        if (!rightIsFull) {
          matches.addMatchBi(leftNode, rightNode);
          break;
        } else {
          Set<Integer> currentMatches = matches.getSetOf(rightNode);
          int leastPreferredNode = preferenceLists.getLeastScoreNode(
                  UNUSED_VAL, rightNode, leftNode, currentMatches,
                  matchingData.getCapacityOf(rightNode)
          );
          if (leastPreferredNode != -1 && preferenceLists.isPreferredOver(leftNode,
                  leastPreferredNode, rightNode)) {
            matches.removeMatchBi(rightNode, leastPreferredNode);
            matches.addMatchBi(leftNode, rightNode);
            queue.add(leastPreferredNode);
            break;
          }
        }
      }
    }
    return matches;
  }

  @Override
  public double[] getMatchesSatisfactions(Matches matches) {
    return this.preferenceLists.getMatchesSatisfactions(matches, matchingData);
  }
}