package org.fit.ssapp.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class PreferenceProviderUtils {

  public static Set<String> convertMapToSet(Map<String, Set<Integer>> varMap) {
    Set<String> resultSet = new HashSet<>();
    for (Map.Entry<String, Set<Integer>> entry : varMap.entrySet()) {
      String variable = entry.getKey();
      for (Integer value : entry.getValue()) {
        resultSet.add(variable + value.toString());
      }
    }
    return resultSet;
  }

  public static Map<String, Set<Integer>> filterVariable(String evaluateFunction) {
    Map<String, Set<Integer>> variables = new HashMap<>();
    for (int c = 0; c < evaluateFunction.length(); c++) {
      char ch = evaluateFunction.charAt(c);
      switch (ch) {
        case 'P':
        case 'W':
        case 'R':
          String prefix = String.valueOf(ch);
          Optional<Integer> nextIdx = getNextIndexToken(evaluateFunction, c);
          if (nextIdx.isPresent()) {
            int idx = nextIdx.get();
            variables.compute(prefix, (key, value) -> {
              if (value == null) {
                Set<Integer> set = new HashSet<>();
                set.add(idx);
                return set;
              } else {
                value.add(idx);
                return value;
              }
            });
          } else {
            throw new IllegalArgumentException("Invalid expression after: " + prefix);
          }
      }
    }
    return variables;
  }

  private static Optional<Integer> getNextIndexToken(String evaluateFunction, int charPos) {
    int numberPos = charPos + 1;
    while (numberPos < evaluateFunction.length() &&
        Character.isDigit(evaluateFunction.charAt(numberPos))) {
      numberPos++;
    }
    if (numberPos == charPos + 1) {
      return Optional.empty();
    }
    String subString = evaluateFunction.substring(charPos + 1, numberPos);
    int idx = Integer.parseInt(subString);
    return Optional.of(idx);
  }

  public static Set<String> getVariables(String evaluateFunction) {
    return convertMapToSet(filterVariable(evaluateFunction));
  }
}
