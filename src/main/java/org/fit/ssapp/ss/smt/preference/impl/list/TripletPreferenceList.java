package org.fit.ssapp.ss.smt.preference.impl.list;

import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
/**
 * TripletPreferenceList - Manages preference rankings for triplet-based stable matching.
 */
@Slf4j
@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
public class TripletPreferenceList implements PreferenceList {

  // Scores are the preferences or priorities for the matching
  // (either from provider or consumer perspective).
  final double[] scores;
  // The positions correspond to the IDs of the individuals (either providers or consumers).
  final int[] positions;
  int current; // Tracks the current index in the list.
  int padding; // Used for index adjustments.

  /**
   * Constructs a **TripletPreferenceList** with a given size and padding.
   *
   * @param size    The total number of elements in the preference list.
   * @param padding The index offset applied for position adjustments.
   */
  public TripletPreferenceList(int size, int padding) {
    scores = new double[size];
    positions = new int[size];
    current = 0;
    this.padding = padding;
  }

  @Override
  public int size(int set) {
    return positions.length;
  }


  @Override
  public int getLeastNode(int set, int newNode, Set<Integer> currentNodes) {
    int leastNode = newNode - this.padding;
    for (int currentNode : currentNodes) {
      if (this.scores[leastNode] > this.scores[currentNode - this.padding]) {
        leastNode = currentNode - this.padding;
      }
    }
    return leastNode + this.padding;
  }

  @Override
  public int getLeastNode(int set, int newNode, int oldNode) {
    if (isScoreGreater(set, newNode, oldNode)) {
      return oldNode;
    } else {
      return newNode;
    }
  }

  public int getPositionByRank(int set, int rank) throws ArrayIndexOutOfBoundsException {
    try {
      return positions[rank] + this.padding;
    } catch (ArrayIndexOutOfBoundsException e) {
      log.error("Position {} not found:", rank, e);
      return -1;
    }
  }

  /**
   * addArray.
   *
   * @param scoreTMP    double[]
   * @param positionTMP int[]
   */
  public void addArray(double[] scoreTMP, int[] positionTMP) {
    for (int i = 0; i < scoreTMP.length; i++) {
      this.scores[current] = scoreTMP[i];
      this.positions[current] = positionTMP[i];
      this.current++;
    }
  }

  @Override
  public boolean isScoreGreater(int set, int node, int nodeToCompare) {
    return this.scores[node - this.padding] > this.scores[nodeToCompare - this.padding];
  }

  @Override
  public double getScore(int position) {  // preferNode :
    try {
      return scores[position - this.padding];
    } catch (ArrayIndexOutOfBoundsException e) {
      log.error("Position {} not found:", position, e);
      return 0;
    }
  }

}
