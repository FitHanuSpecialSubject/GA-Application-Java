package org.fit.ssapp.constants;

import java.util.Set;

/**
 * GT related constants.
 */
public class GameTheoryConst {

  /**
   * algorithms allowing
   */
  public static final String[] ALLOWED_INSIGHT_ALGORITHMS = {
      "NSGAII",
      "NSGAIII",
      "eMOEA",
      "PESA2",
      "VEGA",
      "OMOPSO",
      "SMPSO"};

  /**
   * PAYOFF_VARIABLE_PREFIXES
   */
  public static final Set<String> PAYOFF_VARIABLE_PREFIXES = Set.of("p");

  public static final String DEFAULT_PAYOFF_FUNC = "default";

  /**
   * FITNESS_VARIABLE_PREFIXES
   */
  public static final Set<String> FITNESS_VARIABLE_PREFIXES = Set.of("u");
  
  /**
   * Common aggregation functions for payoff and fitness calculations
   */
  public static final Set<String> AGGREGATION_FUNCTIONS = Set.of(
      "SUM", "AVERAGE", "MIN", "MAX", "PRODUCT", "MEDIAN", "RANGE"
  );

}
