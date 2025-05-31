package org.fit.ssapp.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Sets all fields to private and final by default using Lombok.
 */
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class PreferenceProviderUtils {

  /**
   * Converts a Map of string keys and sets of integers into a Set of strings. The resulting set
   * contains elements formatted as "key+value".
   *
   * @param varMap the map to be converted, where keys are strings and values are sets of integers
   * @return a set of concatenated string representations of key-value pairs
   */
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

  /**
   * Extracts variables from an evaluation function and maps them to their corresponding indices.
   * The valid variable prefixes are 'P', 'W', and 'R'.
   *
   * @param evaluateFunction the function containing variable expressions
   * @return a map where keys are variable prefixes ('P', 'W', 'R') and values are sets of indices
   * @throws IllegalArgumentException if the function contains an invalid expression
   */
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
          break;
        default:
          // DO NOTHING
      }
    }
    return variables;
  }

  private static Optional<Integer> getNextIndexToken(String evaluateFunction, int charPos) {
    int numberPos = charPos + 1;
    while (numberPos < evaluateFunction.length()
        &&
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

  /**
   * Dummy implementation for getting the next index token. Ensure to implement this method properly
   * based on the actual logic.
   *
   * @param evaluateFunction the function string being parsed
   * @return an Optional containing the next index token, if present
   */
  public static Set<String> getVariables(String evaluateFunction) {
    return convertMapToSet(filterVariable(evaluateFunction));
  }
}
