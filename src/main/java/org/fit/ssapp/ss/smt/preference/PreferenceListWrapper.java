package org.fit.ssapp.ss.smt.preference;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingData;

/**
 * Wrapper class provides methods to interact with big list of preference list
 */
public class PreferenceListWrapper {

  /**
   * preference lists
   */
  private final List<PreferenceList> lists;

  public PreferenceListWrapper(List<PreferenceList> lists) {
    this.lists = lists;
  }

  /**
   * lấy thằng học dốt nhất
   *
   * @param set                set tổ (tổ 1, tổ 2, tổ 3, ...)
   * @param preferNode         thằng đánh giá (chấm điểm)
   * @param proposeNode        thằng mới đi học
   * @param setOfPreferNode    các thằng đang trong tổ
   * @param preferNodeCapacity sĩ số tổ
   * @return thằng học dốt nhất
   */
  public int getLeastScoreNode(int set,
      int preferNode,
      int proposeNode,
      Set<Integer> setOfPreferNode,
      int preferNodeCapacity) {
      if (setOfPreferNode.isEmpty()) {
          return -1;
      }

    PreferenceList prefOfSelectorNode = this.lists.get(preferNode);
    // Lớp có một thằng
    if (Objects.equals(preferNodeCapacity, 1)) {
      int currentNode = setOfPreferNode.iterator().next();
      if (isPreferredOver(proposeNode, currentNode, preferNode)) {
        return currentNode;
      } else {
        return proposeNode;
      }
    } else {
      return prefOfSelectorNode.getLeastNode(set, proposeNode, setOfPreferNode);
    }
  }

  /**
   * get preference list
   *
   * @param idx position of individual
   * @return Preference list
   */
  public PreferenceList get(int idx) {
    return lists.get(idx);
  }

  // Stable Matching Algorithm Component: isPreferredOver
  public boolean isPreferredOver(int proposeNode, int preferNodeCurrentNode, int preferNode) {
    PreferenceList preferenceOfSelectorNode = lists.get(preferNode);
    return preferenceOfSelectorNode.isScoreGreater(StableMatchingConst.UNUSED_VALUE,
        proposeNode,
        preferNodeCurrentNode);
  }

  /**
   * Lấy bảng điểm của cả lớp get all satisfaction based on matches
   *
   * @param matches matching result
   * @return satisfactions
   */
  public double[] getMatchesSatisfactions(Matches matches, MatchingData matchingData) {
    int problemSize = matchingData.getSize();
    double[] satisfactions = new double[problemSize];

    for (int i = 0; i < problemSize; i++) {
      double setScore = 0.0;
      PreferenceList ofInd = lists.get(i);
      Set<Integer> setMatches = matches.getSetOf(i);
      for (int node : setMatches) {
        setScore += ofInd.getScore(node);
      }
      satisfactions[i] = setScore;
    }
    return satisfactions;
  }

  /**
   * get last option of target
   *
   * @param target position of individual
   * @return last option
   */
  public int getLastChoiceOf(int set, int target) {
    PreferenceList pref = lists.get(target);
    return pref.getPositionByRank(set, pref.size(set) - 1);
  }

  public double getPreferenceScore(int target, int option) {
    return lists.get(target).getScore(option);
  }

}
