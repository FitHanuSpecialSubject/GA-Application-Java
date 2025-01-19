package org.fit.ssapp.ss.smt.implement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.moeaframework.core.variable.Permutation;

@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OTOProblem implements MatchingProblem {

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

  public boolean hasFitnessFunc() {
    return StringUtils.isEmptyOrNull(this.fitnessFunction);
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
    return "One-to-One Matching";
  }

  @Override
  public MatchingData getMatchingData() {
    return matchingData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Matches stableMatching(Variable var) {
    int[] order = ((Permutation) var).toArray();
    Queue<Integer> singleQueue = Arrays.stream(order).boxed()
        .collect(Collectors.toCollection(LinkedList::new));
    Matches matches = new Matches(getProblemSize());

    while (!singleQueue.isEmpty()) {
      int a = singleQueue.poll();

      PreferenceList aPreference = getPreferenceLists().get(a);
      int prefLen = aPreference.size(0);
      boolean foundMatch = false;

      for (int i = 0; i < prefLen; i++) {
        int b = aPreference.getPositionByRank(UNUSED_VAL, i);

        // If already matched to each other, skip
          if (matches.isMatched(a, b)) {
              break;
          }

        if (!matches.isMatched(b)) {
          // Case 1: b is unmatched
          matches.addMatchBi(a, b);
          break;
        } else {
          // Case 2: b is already matched
          // Find b's current partner(s)
          Set<Integer> bPartners = matches.getSetOf(b);

          // If b prefers an over any current partner
          for (int bPartner : bPartners) {
            if (bLikeAMore(a, b, bPartner)) {
              singleQueue.add(bPartner);
              matches.removeMatchBi(b, bPartner);
              matches.addMatchBi(a, b);
              foundMatch = true;
              break;
            }
          }

            if (foundMatch) {
                break;
            }
        }
      }
    }

    return matches;
  }

  @Override
  public double[] getMatchesSatisfactions(Matches matches) {
    return new double[0];
  }

  private boolean bLikeAMore(int a, int b, int c) {
    return getPreferenceLists().get(b).isScoreGreater(0, a, c);
  }

}
