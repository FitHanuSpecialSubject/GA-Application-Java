package org.fit.ssapp.ss.smt.implement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.IntStream;
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
import org.fit.ssapp.ss.smt.preference.PreferenceListWrapper;
import org.fit.ssapp.ss.smt.preference.impl.list.TripletPreferenceList;
import org.fit.ssapp.util.StringUtils;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.Permutation;

/**
 * TripletOTOProblem.
 */
@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TripletOTOProblem implements MatchingProblem {

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
   * check exists fitness function.
   *
   * @return true if exists
   */
  public boolean hasFitnessFunc() {
    return StringUtils.isEmptyOrNull(this.fitnessFunction);
  }


  /**
   * getMatchesSatisfactions.
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
    Set<Integer> matchedNode = new HashSet<>();
    Permutation castVar = (Permutation) var;
    int[] decodeVar = castVar.toArray();
    Queue<Integer> unMatchedNode = new LinkedList<>();

    for (int val : decodeVar) {
      unMatchedNode.add(val);
    }

    while (!unMatchedNode.isEmpty()) {
      int newNode = unMatchedNode.poll();

      if (matchedNode.contains(newNode)) {
        continue;
      }

      int currentSet = matchingData.getSetNoOf(newNode);
      int[] otherSets = getOtherSets(currentSet);

      TripletPreferenceList nodePreference = (TripletPreferenceList) preferenceLists.get(newNode);
      // including all the matched incoming pairs
      List<Integer> matchedGroup = new ArrayList<>();
      matchedGroup.add(newNode);
      // integrate through each of opposite sets
      for (int targetSet : otherSets) {
        int preferNodeOfTargetSet = matchWithTargetSet(newNode, targetSet, nodePreference, matches,
                unMatchedNode);

        // add to leftover if current individual unavailable to match with any preferNode
        if (preferNodeOfTargetSet == -1) {
          break;
        }
        matchedGroup.add(preferNodeOfTargetSet);
      }

      matches.addMatchForGroup(matchedGroup);
      matchedNode.addAll(matchedGroup);

    }

    // case when a newNode match with only 1 in 2 set
    for (int i = 0; i < matches.getSize(); i++) {
      Set<Integer> currentSet = matches.getSetOf(i);
      if (currentSet.size() == 1) {
        int element = currentSet.iterator().next();
        matches.removeMatchBi(i, element);
      }
    }

    return matches;

  }

  /**
   * find and match with a prefer node in the target set.
   * return the prefer node of target set

   * @param nodePreferences is the preferList of current node
   */
  private int matchWithTargetSet(int newNode, int targetSet,
                                 TripletPreferenceList nodePreferences,
                                 Matches matches,
                                 Queue<Integer> unmatchedNodes) {
    // -1 is not find yet
    int result = -1;

    int sizeOfTargetSet = matchingData.getSetNums().get(targetSet);

    int currentNewNodeSet = matchingData.getSetNoOf(newNode);
    int padding = calculatePadding(targetSet, currentNewNodeSet);
    int calPosition = calculatePosition(targetSet, currentNewNodeSet);
    nodePreferences.setPadding(padding);

    // integrate through preferList and find the preferNode
    for (int i = 0; i < sizeOfTargetSet; i++) {

      int preferNode = nodePreferences.getPositionByRank(UNUSED_VAL, calPosition + i);

      if (!matches.isFull(preferNode, matchingData.getCapacityOf(preferNode))) {
        //stop if successfully matched with preferNode
        result = preferNode;
        break;
      } else {
        if (breakPreviousMatch(newNode, preferNode, matches, unmatchedNodes)) {
          result = preferNode;
          break;
        }
      }
    }

    return result;

  }


  /**
   * whether break the previous match of the preferNode when preferNode already matched.
   * return boolean value when preferNode choose newNode or currentNode(old one)
   */

  private boolean breakPreviousMatch(int newNode, int preferNode,
                                     Matches matches, Queue<Integer> unmatchedNodes) {
    Integer[] individualMatches = matches.getSetOf(preferNode).toArray(new Integer[0]);
    // Iterate through existing matches
    for (int currentNode : individualMatches) {
      if (matchingData.getSetNoOf(currentNode) == matchingData.getSetNoOf(
              newNode)) {
        // Check if newNode is more preferred than currentNode
        if (preferenceLists.isPreferredOver(newNode, currentNode, preferNode)) {
          Collection<Integer> allMatched = matches.getMatchesAndTarget(preferNode);

          for (int matched : allMatched) {
            matches.disMatch(matched, allMatched);    // unmatched previous pairs
            if (matched != preferNode) {
              unmatchedNodes.add(matched);
            }
          }

          return true;
        }
      }
    }
    return false;
  }


  private int[] getOtherSets(int currentSet) {
    return IntStream.range(0, setNum)
            .filter(set -> set != currentSet)
            .toArray();
  }

  /**
   * calculate the padding for a set that stored in preferList of a  newNode.

   * @param targetSet         is the number of set that calculate padding to get
   * @param currentNewNodeSet is the current set can get with the current padding
   */
  private int calculatePadding(int targetSet, int currentNewNodeSet) {
    Map<Integer, Integer> setNums = matchingData.getSetNums();
    if (currentNewNodeSet == setNum - 1) {
      return 0;
    }
    if (currentNewNodeSet == 0) {
      return setNums.get(currentNewNodeSet);
    }

    // if smaller than newNode set, return 0 to get all name of previous set before current's
    if (targetSet < currentNewNodeSet) {
      return 0;
    }

    int paddingSize = 0;
    paddingSize += setNums.get(targetSet);
    return paddingSize;
  }


  /**
   * calculate the position of the preferNode in the preferList of a newNode.
   */
  private int calculatePosition(int targetSet, int currentNewNodeSet) {
    Map<Integer, Integer> setNums = matchingData.getSetNums();

    // if smaller than newNode set, return 0 to get all name of previous set before current's
    int paddingSize = 0;

    for (int i = 0; i < targetSet; i++) {
      if (i != currentNewNodeSet) {
        paddingSize += setNums.get(i);
      }
    }

    return paddingSize;
  }


  @Override
  public String getMatchingTypeName() {
    return "One To One To One";
  }

  /**
   * MOEA Framework Problem implements.
   */

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
