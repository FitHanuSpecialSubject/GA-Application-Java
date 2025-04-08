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

  private final MatchingData matchingData; // Dữ liệu matching từ input

  /**
   * Tính fitness mặc định bằng tổng tất cả các satisfaction values
   */
  @Override
  public double defaultFitnessEvaluation(double[] satisfactions) {
    return Arrays.stream(satisfactions).sum();
  }

  /**
   * Tính fitness với công thức tùy chỉnh
   * 1. Thay thế tất cả các hàm custom (SIGMA, S(index), M) bằng giá trị số
   * 2. Dùng exp4j để tính toán biểu thức toán học thuần túy
   */
  @Override
  public double withFitnessFunctionEvaluation(double[] satisfactions, String fitnessFunction) {
    String processedExpression = replaceAllCustomFunctions(satisfactions, fitnessFunction);
    return new ExpressionBuilder(processedExpression)
            .build()
            .evaluate();
  }

  /**
   * Thay thế tất cả các hàm custom trong biểu thức bằng giá trị số
   */
  private String replaceAllCustomFunctions(double[] satisfactions, String originalExpression) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < originalExpression.length(); i++) {
      char c = originalExpression.charAt(i);

      // Xử lý các hàm bắt đầu bằng chữ 'S' (SIGMA hoặc S(index))
      if (c == 'S') {
        // Xử lý SIGMA{expression}
        if (i + 5 <= originalExpression.length() && originalExpression.startsWith("SIGMA", i)) {
          i = replaceSigmaFunction(satisfactions, originalExpression, i, result);
          continue;
        }

        // Xử lý S(index) - tính tổng satisfaction của 1 set
        if (i + 3 < originalExpression.length()
                && originalExpression.charAt(i + 1) == '('
                && Character.isDigit(originalExpression.charAt(i + 2))
                && originalExpression.charAt(i + 3) == ')') {
          int setIndex = Character.getNumericValue(originalExpression.charAt(i + 2));
          double sum = calculateSetSum(satisfactions, setIndex);
          result.append(sum);
          i += 3; // Bỏ qua phần (x)
          continue;
        }
      }
      // Xử lý biến M (reference tới satisfaction value tại vị trí cụ thể)
      else if (c == 'M') {
        i = replaceMVariable(satisfactions, originalExpression, i, result);
        continue;
      }

      // Giữ lại các ký tự thông thường không phải hàm custom
      result.append(c);
    }

    return result.toString();
  }

  /**
   * Thay thế hàm SIGMA{expression} bằng giá trị số
   * @return vị trí sau dấu '}' cuối cùng
   */
  private int replaceSigmaFunction(double[] satisfactions, String expr, int startIdx, StringBuilder output) {
    int openBrace = expr.indexOf('{', startIdx);
    if (openBrace == -1) {
      throw new IllegalArgumentException("Thiếu dấu '{' trong hàm SIGMA");
    }

    // Tìm dấu '}' tương ứng
    int closeBrace = findMatchingClosingBrace(expr, openBrace);
    String innerExpr = expr.substring(openBrace + 1, closeBrace);

    // Tính giá trị SIGMA
    double sigmaValue = sigmaCalculate(satisfactions, innerExpr);
    output.append(convertToStringWithoutScientificNotation(sigmaValue));

    return closeBrace; // Trả về vị trí sau dấu '}'
  }

  /**
   * Tìm dấu '}' đóng tương ứng với '{' mở
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
    throw new IllegalArgumentException("Không tìm thấy dấu '}' đóng tương ứng");
  }

  /**
   * Thay thế biến Mx bằng giá trị satisfaction tại vị trí x
   * @return vị trí cuối cùng đã xử lý
   */
  private int replaceMVariable(double[] satisfactions, String expr, int startIdx, StringBuilder output) {
    // Tìm tất cả chữ số sau M
    int numEnd = startIdx + 1;
    while (numEnd < expr.length() && Character.isDigit(expr.charAt(numEnd))) {
      numEnd++;
    }

    if (numEnd == startIdx + 1) {
      throw new IllegalArgumentException("Thiếu số sau M");
    }

    int position = Integer.parseInt(expr.substring(startIdx + 1, numEnd));
    if (position < 1 || position > matchingData.getSize()) {
      throw new IllegalArgumentException("Vị trí M ngoài phạm vi: " + position);
    }

    output.append(satisfactions[position - 1]);
    return numEnd - 1; // Trả về vị trí cuối cùng đã xử lý
  }

  /**
   * Tính tổng satisfaction của một set
   */
  private double calculateSetSum(double[] satisfactions, int setIndex) {
    return Arrays.stream(getSatisfactoryOfASetByDefault(satisfactions, setIndex))
            .sum();
  }

  /**
   * Tính toán giá trị cho hàm SIGMA
   */
  private double sigmaCalculate(double[] satisfactions, String expression) {
    double[] streamValue = null;
    String regex = null;

    // Tìm các biến S1, S2 trong expression
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
                  "Giá trị không hợp lệ sau S: " + expression);
        };
      }
    }

    if (regex == null) {
      return 0;
    }

    // Tạo biểu thức con và tính toán
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
   * Lấy satisfaction values của một set cụ thể
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
   * Chuyển số sang string không dùng scientific notation
   */
  private String convertToStringWithoutScientificNotation(double value) {
    if (value % 1 == 0) {
      return String.format("%.0f", value);
    }
    return String.valueOf(value);
  }

  /**
   * Helper method xác định độ dài số sau ký tự M
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
