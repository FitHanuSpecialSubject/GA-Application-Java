package org.fit.ssapp.constants;

import java.util.Set;

/**
 * SMT related constants.
 */
public class StableMatchingConst {

  public static final String MATCHES_KEY = "matches";
  public static final int UNUSED_VALUE = 0;
  public static final String[] ALLOWED_INSIGHT_ALGORITHMS = {"NSGAII", "NSGAIII", "eMOEA", "PESA2",
      "VEGA", "IBEA"};
  public static final String DEFAULT_EVALUATE_FUNC = "default";
  public static final String DEFAULT_FITNESS_FUNC = "default";
  public static final Set<Character> EVAL_VARIABLE_PREFIXES = Set.of('p', 'w', 'r');

  /**
   * Ordinal requirement types.
   */
  public interface ReqTypes {

    int SCALE_TARGET = 0;
    int ONE_BOUND = 1;
    int TWO_BOUND = 2;
    int TIME_SLOT = 3;

  }
}
