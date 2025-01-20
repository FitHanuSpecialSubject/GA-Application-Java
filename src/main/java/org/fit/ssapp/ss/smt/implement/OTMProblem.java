package org.fit.ssapp.ss.smt.implement;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
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
 * OTMProblem
 */
@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OTMProblem implements MatchingProblem {

  /**
   * problem name
   */
  final String problemName;

  /**
   * problem size (number of individuals in matching problem
   */
  final int problemSize;

  /**
   * number of set in matching problem
   */
  final int setNum;

  /**
   * Matching data
   */
  final MatchingData matchingData;

  /**
   * preference list
   */
  final PreferenceListWrapper preferenceLists;

  /**
   * problem fitness function
   */
  final String fitnessFunction;

  /**
   * fitness evaluator
   */
  final FitnessEvaluator fitnessEvaluator;

  /**
   * will not be used
   */
  final int UNUSED_VAL = StableMatchingConst.UNUSED_VALUE;

  @Override
  public String getName() {
    return problemName;
  }

  @Override
  public int getNumberOfVariables() {
    return 1;
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
    Matches result = this.stableMatching(solution.getVariable(0));
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
   * @return boolean
   */
  public boolean hasFitnessFunc() {
    return !this.fitnessFunction.equalsIgnoreCase("default") && !StringUtils.isEmptyOrNull(
            this.fitnessFunction);
  }

  @Override
  public Solution newSolution() {
    Solution solution = new Solution(1, 1);
    Permutation permutationVar = new Permutation(problemSize);
    solution.setVariable(0, permutationVar);
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
                  UNUSED_VAL, rightNode, leftNode, currentMatches, matchingData.getCapacityOf(rightNode)
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