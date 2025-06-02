package org.fit.ssapp.constants;

import java.util.Set;

/**
 * SMT related constants.
 */
public class StableMatchingConst {

  /**
   * MATCHES_KEY.
   */
  public static final String MATCHES_KEY = "matches";
  /**
   * UNUSED_VALUE.
   */
  public static final int UNUSED_VALUE = 0;
  /**
   * ALLOWED_INSIGHT_ALGORITHMS.
   */
  public static final String[] ALLOWED_INSIGHT_ALGORITHMS = {"NSGAII", "NSGAIII", "eMOEA", "PESA2", "VEGA", "IBEA"};
  /**
   * DEFAULT_EVALUATE_FUNC.
   */
  public static final String DEFAULT_EVALUATE_FUNC = "default";
  /**
   * DEFAULT_FITNESS_FUNC.
   */
  public static final String DEFAULT_FITNESS_FUNC = "default";
  /**
   * EVAL_VARIABLE_PREFIXES.
   */
  public static final Set<Character> EVAL_VARIABLE_PREFIXES = Set.of('p', 'w', 'r');
  /**
   * DEFAULT_RUN_COUNT_PER_ALGO
   */
  public static final int DEFAULT_RUN_COUNT_PER_ALGO = 10;
  /**
   * Ordinal requirement types.
   */
  public interface ReqTypes {
    /**
     * SCALE_TARGET.
     */
    int SCALE_TARGET = 0;
    /**
     * ONE_BOUND.
     */
    int ONE_BOUND = 1;
    /**
     * TWO_BOUND.
     */
    int TWO_BOUND = 2;
    /**
     * TIME_SLOT.
     */
    int TIME_SLOT = 3;

  }
}
