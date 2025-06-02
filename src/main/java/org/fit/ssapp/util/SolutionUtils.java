package org.fit.ssapp.util;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

public class SolutionUtils {

  public static Queue<Integer> getSortedIds(Solution solution, boolean isAscending) {
    if (solution == null || solution.getNumberOfVariables() == 0) {
      return new LinkedList<>();
    }
    int n = solution.getNumberOfVariables();
    LinkedList<Integer> sortedIndices = new LinkedList<>();
    for (int i = 0; i < n; i++) {
      insertInSortedOrder(sortedIndices, i, solution, isAscending);
    }
    return sortedIndices;
  }

  private static void insertInSortedOrder(LinkedList<Integer> sortedIndices,
      int index,
      Solution solution,
      boolean isAscending) {
    double score = EncodingUtils.getReal(solution.getVariable(index));
    ListIterator<Integer> it = sortedIndices.listIterator();

    while (it.hasNext()) {
      double sc = EncodingUtils.getReal(solution.getVariable(it.next()));
      if (isAscending) {
        if (sc > score) {
          it.previous();
          break;
        }
      } else {
        if (sc < score) {
          it.previous();
          break;
        }
      }

    }
    it.add(index);
  }

}
