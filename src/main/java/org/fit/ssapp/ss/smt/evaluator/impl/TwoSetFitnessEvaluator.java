package org.fit.ssapp.ss.smt.evaluator.impl;

import static org.fit.ssapp.util.StringExpressionEvaluator.afterTokenLength;
import static org.fit.ssapp.util.StringExpressionEvaluator.convertToStringWithoutScientificNotation;
import static org.fit.ssapp.util.StringExpressionEvaluator.isNumericValue;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.FitnessEvaluator;
import org.fit.ssapp.util.EvaluatorUtils;

/**
 * Compatible with Two Set Matching Problems only.
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class TwoSetFitnessEvaluator implements FitnessEvaluator {

  private final MatchingData matchingData; // Matching data from input

  /**
   * Calculates the default fitness by summing all satisfaction values
   */
  @Override
  public double defaultFitnessEvaluation(double[] satisfactions) {
    return Arrays.stream(satisfactions).sum();
  }

  /**
   * Calculates fitness with a custom formula
   * 1. Replaces all custom functions (SIGMA, S(index), M) with numerical values
   * 2. Uses exp4j to evaluate the mathematical expression
   */
  @Override
  public double withFitnessFunctionEvaluation(double[] satisfactions, String fitnessFunction) {
    String processedExpression = replaceAllCustomFunctions(satisfactions, fitnessFunction);
    return new ExpressionBuilder(processedExpression)
            .build()
            .evaluate();
  }

  /**
   * Replaces all custom functions in the expression with numbers
   */
  private String replaceAllCustomFunctions(double[] satisfactions, String originalExpression) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < originalExpression.length(); i++) {
      char c = originalExpression.charAt(i);

      // Handles functions starting with 'S' (SIGMA or S(index))
      if (c == 'S') {
        // Handles SIGMA{expression}
        if (i + 5 <= originalExpression.length() && originalExpression.startsWith("SIGMA", i)) {
          i = replaceSigmaFunction(satisfactions, originalExpression, i, result);
          continue;
        }

        // Handles S(index) - calculates the sum of satisfaction for a set
        if (i + 3 < originalExpression.length()
                && originalExpression.charAt(i + 1) == '('
                && Character.isDigit(originalExpression.charAt(i + 2))
                && originalExpression.charAt(i + 3) == ')') {
          int setIndex = Character.getNumericValue(originalExpression.charAt(i + 2));
          double sum = calculateSetSum(satisfactions, setIndex);
          result.append(sum);
          i += 3; // Skips the (x) part
          continue;
        }
      }
      // Handles the M variable (reference to satisfaction value at a specific position)
      else if (c == 'M') {
        i = replaceMVariable(satisfactions, originalExpression, i, result);
        continue;
      }

      // Keeps regular characters that are not custom functions
      result.append(c);
    }

    return result.toString();
  }

  /**
   * Replaces the SIGMA{expression} function with a numerical value
   * @return the position after the last '}'
   */
  private int replaceSigmaFunction(double[] satisfactions, String expr, int startIdx, StringBuilder output) {
    int openBrace = expr.indexOf('{', startIdx);
    if (openBrace == -1) {
      throw new IllegalArgumentException("Missing '{' in the SIGMA function");
    }

    // Finds the corresponding '}'
    int closeBrace = findMatchingClosingBrace(expr, openBrace);
    String innerExpr = expr.substring(openBrace + 1, closeBrace);

    // Calculates the SIGMA value
    double sigmaValue = sigmaCalculate(satisfactions, innerExpr);
    output.append(convertToStringWithoutScientificNotation(sigmaValue));

    return closeBrace; // Returns the position after '}'
  }

  /**
   * Finds the closing '}' corresponding to the opening '{'
   */
  private int findMatchingClosingBrace(String expr, int openBracePos) {
    int balance = 1;
    for (int i = openBracePos + 1; i < expr.length(); i++) {
      char c = expr.charAt(i);
      if (c == '{') {
        balance++;
      } else if (c == '}') {
        balance--;
        if (balance == 0) {
          return i;
        }
      }
    }
    throw new IllegalArgumentException("No matching '}' found");
  }

  /**
   * Replaces the Mx variable with the satisfaction value at position x
   * @return the last processed position
   */
  private int replaceMVariable(double[] satisfactions, String expr, int startIdx, StringBuilder output) {
    // Finds all digits after M
    int numEnd = startIdx + 1;
    while (numEnd < expr.length() && Character.isDigit(expr.charAt(numEnd))) {
      numEnd++;
    }

    if (numEnd == startIdx + 1) {
      throw new IllegalArgumentException("Missing number after M");
    }

    int position = Integer.parseInt(expr.substring(startIdx + 1, numEnd));
    if (position < 1 || position > matchingData.getSize()) {
      throw new IllegalArgumentException("M position out of bounds: " + position);
    }

    output.append(satisfactions[position - 1]);
    return numEnd - 1; // Returns the last processed position
  }

  /**
   * Calculates the sum of satisfaction for a set
   */
  private double calculateSetSum(double[] satisfactions, int setIndex) {
    return Arrays.stream(getSatisfactoryOfASetByDefault(satisfactions, setIndex))
            .sum();
  }

  /**
   * Calculates the value for the SIGMA function
   */
  private double sigmaCalculate(double[] satisfactions, String expression) {
    double[] streamValue = null;
    String regex = null;

    // Finds the variables S1, S2 in the expression
    for (int i = 0; i < expression.length() - 1; i++) {
      char ch = expression.charAt(i);
      if (ch == 'S') {
        char set = expression.charAt(i + 1);
        regex = switch (set) {
          case '1' -> {
            streamValue = getSatisfactoryOfASetByDefault(satisfactions, 0);
            yield "S1";
          }
          case '2' -> {
            streamValue = getSatisfactoryOfASetByDefault(satisfactions, 1);
            yield "S2";
          }
          default -> throw new IllegalArgumentException(
                  "Invalid value after S: " + expression);
        };
      }
    }

    if (regex == null) {
      return 0;
    }

    // Creates a sub-expression and calculates it
    Expression exp = new ExpressionBuilder(expression)
            .variables(regex)
            .build();

    String finalRegex = regex;
    DoubleUnaryOperator calculator = x -> {
      exp.setVariable(finalRegex, x);
      return exp.evaluate();
    };

    return Arrays.stream(streamValue)
            .map(calculator)
            .sum();
  }

  /**
   * Gets the satisfaction values of a specific set
   */
  private double[] getSatisfactoryOfASetByDefault(double[] satisfactions, int set) {
    int setTotal = this.matchingData.getTotalIndividualOfSet(set);
    double[] result = new double[setTotal];
    for (int i = 0; i < matchingData.getSize(); i++) {
      if (Objects.equals(matchingData.getSetNoOf(i), set)) {
        setTotal--;
        result[i] = satisfactions[i];
      }
      if (setTotal == 0) {
        break;
      }
    }
    return result;
  }

  /**
   * Converts a number to a string without using scientific notation
   */
  private String convertToStringWithoutScientificNotation(double value) {
    if (value % 1 == 0) {
      return String.format("%.0f", value);
    }
    return String.valueOf(value);
  }

  /**
   * Helper method to determine the length of the number after the M character
   */
  private int afterTokenLength(String str, int start) {
    int length = 0;
    while (start + 1 + length < str.length()
            && Character.isDigit(str.charAt(start + 1 + length))) {
      length++;
    }
    return length;
  }
}
