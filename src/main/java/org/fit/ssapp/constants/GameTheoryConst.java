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

  public static final String DEFAULT_PAYOFF_FUNC = "defalt";

  /**
   * FITNESS_VARIABLE_PREFIXES
   */
  public static final Set<String> FITNESS_VARIABLE_PREFIXES = Set.of("u");

}
