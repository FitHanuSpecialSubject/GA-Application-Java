package org.fit.ssapp.ss.smt.preference.impl.list;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.ss.smt.preference.PreferenceList;

import java.util.*;

import static org.fit.ssapp.util.NumberUtils.formatDouble;

/**
 * Rewrite implementation of Two Sided Stable Matching Problem's Preference List that contains only
 * two sets. In this implementation, set parameters of interface methods is ignored
 */
@Getter
@Slf4j
public class TwoSetPreferListRewrite implements PreferenceList {
  /**
   * Key for nodeId and value for its score
   */
  private final Map<Integer, Double> scores;

  /**
   * TwoSetPreferListRewrite.
   *
   * @param size    int
   */
  public TwoSetPreferListRewrite(int size) {
    scores = new HashMap<>(size, 0.9f);
  }

  @Override
  public int size(int set) {
    return scores.size();
  }

  @Override
  public int getNumberOfOtherSets() {
    return 1;
  }

  /**
   * this method registers new competitor instance to the preference list.
   *
   * @param score score of the respective competitor
   *
   */
  public void add(int nodeId, double score) {
    scores.put(nodeId, score);
  }

  /**
   * Finds the least preferred node among the given candidates.
   *
   * @param set The set identifier
   * @param newNode The new node to be compared.
   * @param currentNodes The set of currently matched nodes.
   * @return The least preferred node.
   */
  @Override
  public int getLeastNode(int set, int newNode, Set<Integer> currentNodes) {
    Set<Integer> nodes = new HashSet<>(currentNodes);
    nodes.add(newNode);
    int leastNode = nodes.stream()
            .reduce((node1, node2) -> {
              double score1 = scores.getOrDefault(node1, 0.0);
              double score2 = scores.getOrDefault(node2, 0.0);
              return score1 <= score2 ? node1 : node2;
            })
            .orElse(newNode);

    return leastNode;
  }

  @Override
  public int getLeastNode(int set, int newNode, int oldNode) {
    if (isScoreGreater(set, newNode, oldNode)) {
      return oldNode;
    } else {
      return newNode;
    }
  }

  @Override
  public int getPositionByRank(int set, int rank) {
    return 0 ;
  }

  @Override
  public int getLastOption(int set) {
    return 0;
  }

  @Override
  public boolean isScoreGreater(int set, int proposeNode, int preferNodeCurrentNode) {
    return scores.getOrDefault(proposeNode, 0.0) > scores.getOrDefault(preferNodeCurrentNode, 0.0);
  }


  /**
   * getScore of a node ;
   *
   * @param nodeId that matched with
   */
  @Override
  public double getScore(int nodeId) {
    return scores.getOrDefault(nodeId,0.0);
  }


  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("{");
    Iterator<Map.Entry<Integer, Double>> iterator = scores.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<Integer, Double> entry = iterator.next();
      result.append("[")
              .append(entry.getKey())
              .append(" -> ")
              .append(formatDouble(entry.getValue()))
              .append("]");
      if (iterator.hasNext()) {
        result.append(", ");
      }
    }
    result.append("}");
    return result.toString();
  }

  public Set<Integer> getAllNodeId() {
    return Collections.unmodifiableSet(scores.keySet());
  }
}