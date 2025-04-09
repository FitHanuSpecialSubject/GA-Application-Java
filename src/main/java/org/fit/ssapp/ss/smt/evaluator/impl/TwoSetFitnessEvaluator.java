package org.fit.ssapp.ss.smt.evaluator.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.FitnessEvaluator;

/**
 * Evaluates fitness functions for Two-Set Matching Problems.
 * Handles custom functions (SIGMA, S(index), M variables) by replacing them
 * with numerical values before final evaluation using exp4j.
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class TwoSetFitnessEvaluator implements FitnessEvaluator {

  private final MatchingData matchingData; // Matching data from input

  // Regex pattern for SIGMA{expression} functions
  private static final Pattern SIGMA_PATTERN = Pattern.compile("SIGMA\\{([^}]+)\\}");

  // Regex pattern for S(index) set references
  private static final Pattern S_INDEX_PATTERN = Pattern.compile("S\\((\\d)\\)");

  // Regex pattern for M(position) variables
  private static final Pattern M_VAR_PATTERN = Pattern.compile("M(\\d+)");

  /**
   * Calculates the default fitness by summing all satisfaction values
   */
  @Override
  public double defaultFitnessEvaluation(double[] satisfactions) {
    return Arrays.stream(satisfactions).sum();
  }

  /**
   * Evaluates custom fitness function by:
   * 1. Replacing all custom functions with their numerical values
   * 2. Evaluating the resulting mathematical expression
   */
  @Override
  public double withFitnessFunctionEvaluation(double[] satisfactions, String fitnessFunction) {
    String processedExpression = processCustomFunctions(satisfactions, fitnessFunction);

    try {
      Expression expression = new ExpressionBuilder(processedExpression).build();

      ValidationResult validation = expression.validate(false);
      if (!validation.isValid()) {
        throw new IllegalArgumentException(
                "Invalid expression: '" + processedExpression + "'. "
                        + "Validation errors: " + validation.getErrors()
                        + " Original expression: '" + fitnessFunction + "'"
        );
      }
      return expression.evaluate();

    } catch (IllegalArgumentException | ArithmeticException e) {
      throw new IllegalArgumentException(
              "Evaluation failed for expression: '" + processedExpression
                     + "'. Original: '" + fitnessFunction + "'. Error: " + e.getMessage(),
              e
      );
    }
  }

  /**
   * Processes all custom functions in the expression by replacing them with values
   */
  private String processCustomFunctions(double[] satisfactions, String expression) {
    // Processing order matters - handle most complex functions first
    String processed = replaceSigmaFunctions(satisfactions, expression);
    processed = replaceSIndexFunctions(satisfactions, processed);
    processed = replaceMVariables(satisfactions, processed);
    return processed;
  }

  /**
   * Replaces all SIGMA{expression} occurrences with their calculated values
   */
  private String replaceSigmaFunctions(double[] satisfactions, String expression) {
    Matcher matcher = SIGMA_PATTERN.matcher(expression);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      // Extract the inner expression between { and }
      String innerExpr = matcher.group(1);

      // Calculate the sigma value
      double value = sigmaCalculate(satisfactions, innerExpr);

      // Replace with calculated value (handling scientific notation)
      matcher.appendReplacement(sb, Matcher.quoteReplacement(
              convertToStringWithoutScientificNotation(value)));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  /**
   * Replaces all S(index) occurrences with their set sums
   */
  private String replaceSIndexFunctions(double[] satisfactions, String expression) {
    Matcher matcher = S_INDEX_PATTERN.matcher(expression);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      int setIndex = Integer.parseInt(matcher.group(1));
      double sum = calculateSetSum(satisfactions, setIndex);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(
              convertToStringWithoutScientificNotation(sum)));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  /**
   * Replaces all M(position) variables with satisfaction values
   */
  private String replaceMVariables(double[] satisfactions, String expression) {
    Matcher matcher = M_VAR_PATTERN.matcher(expression);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      int position = Integer.parseInt(matcher.group(1));

      // Validate position bounds
      if (position < 1 || position > matchingData.getSize()) {
        throw new IllegalArgumentException(
                "M position out of range [1-" + matchingData.getSize() + "]: " + position);
      }

      double value = satisfactions[position - 1];
      matcher.appendReplacement(sb, Matcher.quoteReplacement(
              convertToStringWithoutScientificNotation(value)));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  /**
   * Calculates the value of a SIGMA function expression
   */
  private double sigmaCalculate(double[] satisfactions, String expression) {
    double[] streamValue = null;
    String regex = null;

    // Identify which set (S1/S2) is referenced in the expression
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
                  "Invalid set reference after S: " + expression);
        };
      }
    }

    if (regex == null) {
      return 0; // No valid set reference found
    }

    // Build and evaluate the sub-expression
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
   * Calculates the sum of satisfaction values for a specific set
   */
  private double calculateSetSum(double[] satisfactions, int setIndex) {
    return Arrays.stream(getSatisfactoryOfASetByDefault(satisfactions, setIndex))
            .sum();
  }

  /**
   * Extracts satisfaction values for a specific set
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
   * Converts double to String without scientific notation
   */
  private String convertToStringWithoutScientificNotation(double value) {
    return String.valueOf(value); // Decimal values as-is
  }
}