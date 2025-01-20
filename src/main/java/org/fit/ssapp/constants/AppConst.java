package org.fit.ssapp.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * App related constants.
 */
public class AppConst {

  /**
   * Algorithm available for the system
   */
  public static final String[] SUPPORTED_ALGOS = {
      "AGE-MOEA-II",
      "AMOSA",
      "CMA-ES",
      "DBEA",
      "DE",
      "eMOEA",
      "eNSGAII",
      "ES",
      "GA",
      "GDE3",
      "IBEA",
      "MOEAD",
      "MSOPS",
      "NSGAII",
      "NSGAIII",
      "OMOPSO",
      "PAES",
      "PESA2",
      "RSO",
      "RVEA",
      "SA",
      "SMPSO",
      "SMSEMOA",
      "SPEA2",
      "UNSGAIII",
      "VEGA"
  };

  /**
   * based on net.objecthunter.exp4j version 0.4.8 supported math functions
   */
  public static final Set<String> BUILTIN_FUNCTION_NAMES = new HashSet<>(Arrays.asList(
      "sin",
      "cos",
      "tan",
      "cot",
      "asin",
      "acos",
      "atan",
      "sinh",
      "cosh",
      "tanh",
      "abs",
      "log",
      "log10",
      "log2",
      "log1p",
      "ceil",
      "floor",
      "sqrt",
      "cbrt",
      "pow",
      "exp",
      "expm1",
      "signum"
  ));

  /**
   * PSO_BASED_ALGOS
   */
  public static final List<String> PSO_BASED_ALGOS = Arrays.asList(
          "OMOPSO", "SMPSO");

  /**
   * default function of system
   */
  public static final String DEFAULT_FUNC = "default";

  /**
   * APP_CUSTOM_FUNCTIONS
   */
  public static final Set<String> APP_CUSTOM_FUNCTIONS = Set.of("sigma");

  /**
   * character for the open tag
   */
  public static final String E_OPEN = "{";

  /**
   * character for the close tag
   */
  public static final String E_CLOSE = "}";

  /**
   * DATA_DIR
   */
  public static final String DATA_DIR = ".data";

  /**
   * DATA_EXT
   */
  public static final String DATA_EXT = "ser";

  /**
   * LOG_DIR
   */
  public static final String LOG_DIR = ".log";

  /**
   * CSV_EXT
   */
  public static final String CSV_EXT = "csv";

  /**
   * TSV_EXT
   */
  public static final String TSV_EXT = "tsv";

}
