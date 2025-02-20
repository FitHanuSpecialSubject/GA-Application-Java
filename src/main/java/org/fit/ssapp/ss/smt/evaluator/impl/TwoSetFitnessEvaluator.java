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

  private final MatchingData matchingData;

  @Override
  public double defaultFitnessEvaluation(double[] satisfactions) {
    return Arrays.stream(satisfactions).sum();
  }

  @Override
  public double withFitnessFunctionEvaluation(double[] satisfactions, String fitnessFunction) {

    StringBuilder tmpSB = new StringBuilder();
    for (int c = 0; c < fitnessFunction.length(); c++) {
      char ch = fitnessFunction.charAt(c);
      if (ch == 'S') {
//        if (Objects.equals(fitnessFunction.substring(c, c + 5), "SIGMA")) {
//          if (fitnessFunction.charAt(c + 5) != '{') {
//            System.err.println("Missing '{'");
//            System.err.println(fitnessFunction);
//            throw new RuntimeException("Missing '{' after Sigma function");
//          } else {
//            int expressionStartIndex = c + 6;
//            int expressionLength = EvaluatorUtils
//                .getSigmaFunctionExpressionLength(fitnessFunction, expressionStartIndex);
//            String expression = fitnessFunction.substring(expressionStartIndex,
//                expressionStartIndex + expressionLength);
//            double val = this.sigmaCalculate(satisfactions, expression);
//            tmpSB.append(convertToStringWithoutScientificNotation(val));
//            c += expressionLength + 3;
//          }
//        }

        /* Sửa lại đoạn này (Đoạn gốc đã được comment out để tiện cho việc review)
        * Đang có vấn đề trong việc xử lý hàm SIGMA
        * VD: Range [5, 10) out of bounds for length 7 (Trong StableMatchingSolverTest)
        * Các phần xử lý cho *else* khác sẽ được tìm cách tốt để xử lý lỗi sau
        * */
        if (c + 5 <= fitnessFunction.length() && Objects.equals(fitnessFunction.substring(c, c + 5), "SIGMA")) {
          if (c + 5 < fitnessFunction.length() && fitnessFunction.charAt(c + 5) != '{') {
            // TODO: Error handling here
          } else if (c + 6 < fitnessFunction.length()) {
            // Thử xem có lấy được expressionStartIndex không
            int expressionStartIndex = c + 6;
            int expressionLength = EvaluatorUtils.getSigmaFunctionExpressionLength(fitnessFunction, expressionStartIndex);

            if (expressionStartIndex + expressionLength <= fitnessFunction.length()) {
              // If này kiểm tra xem có sử dụng/lấy được Expression hay không
              String expression = fitnessFunction.substring(expressionStartIndex, expressionStartIndex + expressionLength);
              double val = this.sigmaCalculate(satisfactions, expression);
              tmpSB.append(convertToStringWithoutScientificNotation(val));
              c += expressionLength + 3;
            } else {
              // TODO: Error handling here
            }
          } else {
            // TODO: Error handling here
          }
        }
        // Check for F(index) pattern
        if (c + 3 < fitnessFunction.length() && fitnessFunction.charAt(c + 1) == '('
                &&
                fitnessFunction.charAt(c + 3) == ')') {
          if (isNumericValue(fitnessFunction.charAt(c + 2))) {
            int set = Character.getNumericValue(fitnessFunction.charAt(c + 2));
            //Calculate SUM
            tmpSB.append(convertToStringWithoutScientificNotation(DoubleStream
                    .of(getSatisfactoryOfASetByDefault(satisfactions, set))
                    .sum()));
          }
        }
        c += 3;
      } else if (ch == 'M') {
        int ssLength = afterTokenLength(fitnessFunction, c);
        int positionOfM = Integer.parseInt(fitnessFunction.substring(c + 1,
                c + 1 + ssLength));
        if (positionOfM < 0 || positionOfM > matchingData.getSize()) {
          throw new IllegalArgumentException(
                  "invalid position after variable M: " + positionOfM);
        }
        double valueOfM = satisfactions[positionOfM - 1];
        tmpSB.append(valueOfM);
        c += ssLength;
      } else {
        //No occurrence of W/w/P/w
        tmpSB.append(ch);
      }
    }
    System.out.println(tmpSB);
    return new ExpressionBuilder(tmpSB.toString())
            .build()
            .evaluate();
  }

  private double sigmaCalculate(double[] satisfactions, String expression) {
    double[] streamValue = null;
    String regex = null;
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

  private double[] getSatisfactoryOfASetByDefault(double[] satisfactions, int set) {
    int setTotal = this.matchingData.getTotalIndividualOfSet(set);
    double[] result = new double[setTotal];
    for (int i = 0; i < matchingData.getSize(); i++) {
      if (Objects.equals(matchingData.getSetNoOf(i), set)) {
        setTotal--;
        result[i] = satisfactions[i];
      }
      if (Objects.equals(setTotal, 0)) {
        break;
      }
    }
    return result;
  }
}
