package org.fit.ssapp.ss.smt.preference.impl.list;

import static org.fit.ssapp.util.NumberUtils.formatDouble;

import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.ss.smt.preference.PreferenceList;

/**
 * Old implementation of Two Sided Stable Matching Problem's Preference List that contains only two
 * sets. In this implementation, set parameters of interface methods is ignored
 */
@Getter
@Slf4j
public class TwoSetPreferenceList implements PreferenceList {

  private final double[] scores;
  private final int[] positions;
  private int current;
  private final int padding;

  /**
   * TwoSetPreferenceList
   *
   * @param size    int
   * @param padding int
   */
  public TwoSetPreferenceList(int size, int padding) {
    scores = new double[size];
    positions = new int[size];
    current = 0;
    this.padding = padding;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(int set) {
    // ignore set param
    return positions.length;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOtherSets() {
    return 0;
  }


  /**
   * @param score score of the respective competitor
   *              <p>
   *              this method registers new competitor instance to the preference list
   */
  public void add(double score) {
    this.scores[current] = score;
    this.positions[current] = current;
    this.current++;
  }

  /**
   * {@inheritDoc}
   */
  public int getLeastNode(int set, int newNode, Set<Integer> currentNodes) {
    int leastNode = newNode - this.padding;
    for (int currentNode : currentNodes) {
      if (this.scores[leastNode] > this.scores[currentNode - this.padding]) {
        leastNode = currentNode - this.padding;
      }
    }
    return leastNode + this.padding;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getLeastNode(int set, int newNode, int oldNode) {
    if (isScoreGreater(set, newNode, oldNode)) {
      return oldNode;
    } else {
      return newNode;
    }
  }


  /**
   * {@inheritDoc}
   */
  public boolean isScoreGreater(int set, int node, int nodeToCompare) {
    return this.scores[node - this.padding] > this.scores[nodeToCompare - this.padding];
  }

  /**
   * <i>THIS METHOD ONLY VALUABLE AFTER @method sortByValueDescending IS INVOKED </i>
   *
   * @param rank position (rank best <-- 0, 1, 2, 3, ... --> worst) on the preference list
   * @return unique identifier of the competitor instance that holds the respective position on the
   * list
   */
  public int getPositionByRank(int set, int rank) throws ArrayIndexOutOfBoundsException {
    try {
      return positions[rank] + this.padding;
    } catch (ArrayIndexOutOfBoundsException e) {
      log.error("Position {} not found:", rank, e);
      return -1;
    }
  }

  /**
   * {@inheritDoc}
   */
  public int getLastOption(int set) {
    return this.getPositionByRank(set, this.positions.length - 1);
  }


  public void sort() {
    sortDescendingByScores();
  }

  public void sortDescendingByScores() {
    double[] cloneScores = scores.clone(); //copy to new array
    int size = cloneScores.length;

    // Build min heap
    for (int i = size / 2 - 1; i >= 0; i--) {
      heapify(cloneScores, size, i);
    }

    // Extract elements from heap one by one
    for (int i = size - 1; i > 0; i--) {
      // Move current root to end
      double temp = cloneScores[0];
      int tempPos = positions[0];

      cloneScores[0] = cloneScores[i];
      positions[0] = positions[i];

      cloneScores[i] = temp;
      positions[i] = tempPos;

      // Call min heapify on the reduced heap
      heapify(cloneScores, i, 0);
    }
  }

  void heapify(double[] array, int heapSize, int rootIndex) {
    int smallestIndex = getSmallestIndex(array, heapSize, rootIndex);

    // If smallest is not root
    if (smallestIndex != rootIndex) {
      double swap = array[rootIndex];
      int posSwap = positions[rootIndex];

      array[rootIndex] = array[smallestIndex];
      positions[rootIndex] = positions[smallestIndex];

      array[smallestIndex] = swap;
      positions[smallestIndex] = posSwap;

      // Recursively heapify the affected sub-tree
      heapify(array, heapSize, smallestIndex);
    }
  }

  private static int getSmallestIndex(double[] array, int heapSize, int rootIndex) {
    int smallestIndex = rootIndex; // Initialize smallest as root
    int leftChildIndex = 2 * rootIndex + 1; // left = 2*rootIndex + 1
    int rightChildIndex = 2 * rootIndex + 2; // right = 2*rootIndex + 2

    // If left child is smaller than root
    if (leftChildIndex < heapSize && array[leftChildIndex] < array[smallestIndex]) {
      smallestIndex = leftChildIndex;
    }

    // If right child is smaller than smallest so far
    if (rightChildIndex < heapSize && array[rightChildIndex] < array[smallestIndex]) {
      smallestIndex = rightChildIndex;
    }
    return smallestIndex;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("{");
    for (int i = 0; i < scores.length; i++) {
      int pos = positions[i];
      result
              .append("[")
              .append(pos)
              .append(" -> ")
              .append(formatDouble(scores[pos]))
              .append("]");
      if (i < scores.length - 1) {
        result.append(", ");
      }
    }
    result.append("}");
    return result.toString();
  }

  /**
   * {@inheritDoc}
   */
  public double getScore(int position) {
    try {
      return scores[position - this.padding];
    } catch (ArrayIndexOutOfBoundsException e) {
      log.error("Position {} not found:", position, e);
      return 0;
    }
  }

}
