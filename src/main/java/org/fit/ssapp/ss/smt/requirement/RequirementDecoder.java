package org.fit.ssapp.ss.smt.requirement;

import static org.fit.ssapp.util.NumberUtils.isDouble;
import static org.fit.ssapp.util.NumberUtils.isInteger;
import static org.fit.ssapp.util.StringUtils.findFirstNonNumericCharIndex;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fit.ssapp.ss.smt.requirement.impl.OneBound;
import org.fit.ssapp.ss.smt.requirement.impl.ScaleTarget;
import org.fit.ssapp.ss.smt.requirement.impl.TwoBound;

/**
 * Decoder for requirement.
 * TODO: Hơi bị thừa logic do bê từ code cũ sang
 * TODO: tối giản hóa logic map
 */
public class RequirementDecoder {

  private static final boolean INCREMENT = true;
  private static final boolean DECREMENT = false;

  /**
   * Decode String[][] of requirements in request to Requirement[][].
   *
   * @param requirements string representations of requirements
   * @return Requirement[][]
   */
  public static Requirement[][] decode(String[][] requirements) {
    return Arrays.stream(requirements)
            .map(row -> Arrays.stream(row)
                    .map(RequirementDecoder::decodeInputRequirement)
                    .map(RequirementDecoder::toRequirement)
                    .toArray(Requirement[]::new))
            .toArray(Requirement[][]::new);
  }

  private static Requirement toRequirement(String[] array) {
    try {
      if (Objects.equals(array[1], "++")) {
        return new OneBound(Double.parseDouble(array[0]), INCREMENT);
      } else if (Objects.equals(array[1], "--")) {
        return new OneBound(Double.parseDouble(array[0]), DECREMENT);
      } else if (Objects.equals(array[1], null)) {
        if (isInteger(array[0])) {
          return new ScaleTarget(Integer.parseInt(array[0]));
        } else if (isDouble(array[0])) {
          return new OneBound(Double.parseDouble(array[0]), INCREMENT);
        } else {
          return new OneBound(0.0, INCREMENT);
        }
      } else {
        double value1 = Double.parseDouble(array[0]);
        double value2 = Double.parseDouble(array[1]);
        return new TwoBound(value1, value2);
      }
    } catch (NumberFormatException e) {
      return new OneBound(0.0, INCREMENT);
    }
  }

  private static String[] decodeInputRequirement(String item) {
    item = item.trim();
    String[] result = new String[2];

    Pattern intPattern = Pattern.compile("^-?\\d+$");
    Pattern doublePattern = Pattern.compile("^-?\\d+\\.\\d+$");
    Pattern operatorPattern = Pattern.compile("^(-?\\d+(?:\\.\\d+)?)(:|\\+\\+|--)(.*)$");

    Matcher integerMatcher = intPattern.matcher(item);
    Matcher doubleMatcher = doublePattern.matcher(item);
    Matcher operatorMatcher = operatorPattern.matcher(item);
    if (integerMatcher.matches()) {
      try {
        int num = Integer.parseInt(item);
        result[0] = item;
        result[1] = (num >= 0 && num <= 10) ? null : "++";
      } catch (NumberFormatException e) {
        result[0] = "-1";
        result[1] = "++";
      }
    } else if (doubleMatcher.matches()) {
      try {
        Double.parseDouble(item);
        result[0] = item;
        result[1] = null;
      } catch (NumberFormatException e) {
        result[0] = "-3";
        result[1] = null;
      }
    } else if (operatorMatcher.matches()) {
      result[0] = operatorMatcher.group(1);
      String operator = operatorMatcher.group(2);
      if (operator.equals(":")) {
        result[1] = operatorMatcher.group(3).trim();
      } else {
        result[1] = operator;
      }
    } else {
      result[0] = "-2";
      result[1] = "++";
    }
    return result;
  }

}
