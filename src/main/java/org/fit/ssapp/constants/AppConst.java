package org.fit.ssapp.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppConst {

  public final static String[] SUPPORTED_ALGOS = {
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

  // based on net.objecthunter.exp4j version 0.4.8 supported math functions
  public final static Set<String> BUILTIN_FUNCTION_NAMES = new HashSet<>(Arrays.asList(
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

  public static final List<String> PSO_BASED_ALGOS = Arrays.asList(
      "OMOPSO", "SMPSO");

  public static final String DEFAULT_FUNC = "default";
  public static final Set<String> APP_CUSTOM_FUNCTIONS = Set.of("sigma");
  public static final String E_OPEN = "{";
  public static final String E_CLOSE = "}";

  public static final String DATA_DIR = ".data";
  public static final String DATA_EXT = "ser";
  public static final String LOG_DIR = ".log";
  public static final String CSV_EXT = "csv";
  public static final String TSV_EXT = "tsv";

}
