package org.fit.ssapp.util;

import java.util.Set;
import org.fit.ssapp.ss.smt.Matches;

public class Testing {
  private final Matches matches;
  private final int[] capacities;

  public Testing(Matches matches, int[] capacities) {
    this.capacities = capacities;
    this.matches = matches;
  }

  public boolean hasDuplicate() {
    int[] checkArr = capacities;
    int sz = matches.size();
    for (int i = 0; i < sz; i++) {
      Set<Integer> matchSet = matches.getSetOf(i);
      for (int elm : matchSet) {
        checkArr[elm]--;
          if (checkArr[elm] < 0) {
              return true;
          }
      }
    }
    Set<Integer> lefts = matches.getLeftOvers();
    for (int elm : lefts) {
      checkArr[elm]--;
        if (checkArr[elm] < 0) {
            return true;
        }
    }
    return false;
  }

  public static void main(String[] args) {
    int[] check = {3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    Matches matches = new Matches(12);
    matches.addMatch(0, 4);
    matches.addMatch(0, 5);
    matches.addMatch(0, 6);

    matches.addMatch(1, 3);
    matches.addMatch(1, 10);
    matches.addMatch(1, 8);

    matches.addMatch(2, 7);
    matches.addMatch(2, 11);

    Testing testing = new Testing(matches, check);
    System.out.println("Has duplicates: " + testing.hasDuplicate());
  }

}
