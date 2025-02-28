package org.fit.ssapp.util;

import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.fit.ssapp.constants.AppConst;
import org.fit.ssapp.constants.StableMatchingConst;

/**
 * Utility class for evaluating mathematical expressions related to satisfaction levels and
 * functions. This class provides methods for extracting variables, validating functions, and
 * calculating sigma values.
 */
public class EvaluatorUtils {

  /**
   * Calculates the length of the expression inside a sigma function. This function searches for the
   * content between the start index and the closing bracket "{}".
   *
   * @param function   the function string containing the sigma expression
   * @param startIndex the index where the sigma expression starts (index of "{}")
   * @return the length of the expression inside the sigma function
   */
  public static int getSigmaFunctionExpressionLength(String function, int startIndex) {
    int num = 0;
    for (int i = startIndex; i < function.charAt(i); i++) {
      char ch = function.charAt(i);
      if (ch == '}') {
        return num;
      } else {
        num++;
      }
    }
    return num;
  }


  /**
   * Calculates the satisfaction levels of a set based on the satisfaction levels of individuals.
   * This function splits the satisfaction levels based on the set and the number of individuals in
   * that set.
   *
   * @param satisfactions            the array containing the satisfaction levels of all
   *                                 individuals
   * @param expression               the set index (0 or 1)
   * @param numberOfIndividuals      the total number of individuals
   * @param numberOfIndividualOfSet0 the number of individuals in set 0
   */
  public static double sigmaCalculate(double[] satisfactions, String expression,
      int numberOfIndividuals, int numberOfIndividualOfSet0) {
    System.out.println("sigma calculating...");
    double[] streamValue = null;
    String regex = null;
    for (int i = 0; i < expression.length() - 1; i++) {
      char ch = expression.charAt(i);
      if (ch == 'S') {
        char set = expression.charAt(i + 1);
        regex = switch (set) {
          case '1' -> {
            streamValue = getSatisfactoryOfaSetByDefault(satisfactions, 0, numberOfIndividuals,
                numberOfIndividualOfSet0);
            yield "S1";
          }
          case '2' -> {
            streamValue = getSatisfactoryOfaSetByDefault(satisfactions, 1, numberOfIndividuals,
                numberOfIndividualOfSet0);
            yield "S2";
          }
          default -> throw new IllegalArgumentException(
              "Illegal value after S regex in sigma calculation: " + expression);
        };
      }
    }
    if (regex == null) {
      return 0;
    }
    Expression exp = new ExpressionBuilder(expression)
        .variables(regex)
        .build();
    String finalRegex = regex;
    DoubleUnaryOperator calculator = x -> {
      exp.setVariable(finalRegex, x);
      return exp.evaluate();
    };
    return DoubleStream
        .of(streamValue)
        .map(calculator)
        .sum();
  }

  /**
   * Calculates sigma value based on satisfaction level and input expression.
   *
   * @param satisfactions            Array containing satisfaction levels of individuals.
   * @param set                      Tổng số cá nhân.
   * @param numberOfIndividualOfSet0 Set of individual numbers 0.
   * @return The sum of the sigma values is calculated.
   */
  public static double[] getSatisfactoryOfaSetByDefault(double[] satisfactions, int set,
      int numberOfIndividual, int numberOfIndividualOfSet0) {
    double[] setSatisfactions;
    if (set == 0) {
      setSatisfactions = new double[numberOfIndividualOfSet0];
      System.arraycopy(satisfactions, 0, setSatisfactions, 0, numberOfIndividualOfSet0);
    } else {
      setSatisfactions = new double[numberOfIndividual - numberOfIndividualOfSet0];
      if (numberOfIndividual - numberOfIndividualOfSet0 >= 0) {
        int idx = 0;
        for (int i = numberOfIndividualOfSet0; i < numberOfIndividual; i++) {
          setSatisfactions[idx] = satisfactions[i];
          idx++;
        }
      }
    }
    return setSatisfactions;
  }


  /**
   * Validates a given fitness function by trimming whitespace and checking if it is empty or
   * matches the default fitness function. If it is empty or matches the default, it returns an
   * empty string.
   *
   * @param func the fitness function string to validate
   * @return an empty string if the function is empty or matches the default fitness function;
   *        otherwise, returns the function string
   */
  public static String getValidEvaluationFunction(String func) {
    func = func.trim();
    if (func.equalsIgnoreCase(StableMatchingConst.DEFAULT_EVALUATE_FUNC)) {
      return "";
    }
    return func;
  }

  /**
   * Checks if the function string is equal to the default function and returns an empty string if
   * true.
   *
   * @param func the function string to check
   * @return an empty string if the function matches the default; otherwise, returns the function
   *        string
   */
  public static String getIfDefaultFunction(String func) {
    if (AppConst.DEFAULT_FUNC.equalsIgnoreCase(func)) {
      return "";
    }
    return func;
  }


  /**
   * Validates a given fitness function by trimming whitespace and checking if it is empty or
   * matches the default fitness function. If it is empty or matches the default, it returns an
   * empty string.
   *
   * @param func the fitness function string to validate
   * @return an empty string if the function is empty or matches the default fitness function;
   *      otherwise, returns the function string
   */
  public static String getValidFitnessFunction(String func) {
    func = func.trim();
    if (StringUtils.isEmptyOrNull(func)
        ||
        func.equalsIgnoreCase(StableMatchingConst.DEFAULT_FITNESS_FUNC)) {
      return "";
    }
    return func;
  }


  /**
   * Main method to demonstrate the usage of expressions with variables. This method builds and
   * validates expressions using the exp4j library.
   *
   * @param args command line arguments (not used)
   */
  public static void main(String[] args) {
    String[] vars = new String[]{
        "u", "u12", "u21", "u202"
    };
    String[] functions = new String[]{
        "u202 + 1 + 2",
        "(u12 + 1) * 2",
        "abs(u12 - u21) / 2"
    };
    for (String func : functions) {
      Expression e = new ExpressionBuilder(func)
          .variables(vars)
          .build();
      Set<String> extractedVars = PreferenceProviderUtils.getVariables(func);
      for (String var : extractedVars) {
        e.setVariable(var, 1d);
      }
      ValidationResult res = e.validate();
      printValidateRes(res);
    }

  }

  private static void printValidateRes(ValidationResult valRes) {
    System.out.println(
        "Validation Result: " + valRes.isValid() + ", errors: " + valRes.getErrors());
  }
}
