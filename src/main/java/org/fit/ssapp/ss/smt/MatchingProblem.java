package org.fit.ssapp.ss.smt;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Variable;

/**
 * base class for MatchingProblem
 */
public interface MatchingProblem extends Problem {

  /**
   * Get problem name
   */
  String getName();

  /**
   * Get Matching type name
   */
  String getMatchingTypeName();

  /**
   * Get problem's matching data
   *
   * @return matching data
   */
  MatchingData getMatchingData();

  /**
   * Main matching logic for Stable Matching Problem Types
   *
   * @param var Variable
   * @return Matches
   */
  Matches stableMatching(Variable var);

  /**
   * Get all satisfactions of matches result
   *
   * @param matches Matches
   * @return satisfactions
   */
  double[] getMatchesSatisfactions(Matches matches);

}
