package org.fit.ssapp.util;

import java.util.Objects;

/**
 * Utility class for common string operations.
 */
public class StringUtils {

  /**
   * Checks if a string is null or empty.
   *
   * @param str the input string.
   * @return {@code true} if the string is null or empty, otherwise {@code false}.
   */
  public static boolean isEmptyOrNull(String str) {
    return Objects.isNull(str) || Objects.equals(str, "");
  }

  /**
   * Creates a string filled with the specified character for the given length.
   *
   * @param character the character to repeat.
   * @param length    the number of times the character should repeat.
   * @return a string consisting of the repeated character.
   * @throws IllegalArgumentException if length is negative.
   */
  public static String fillWithChar(char character, int length) {
    String format = "%" + length + "s";
    return String.format(format, "").replace(' ', character);
  }

  /**
   * Finds the index of the first non-numeric character in a string.
   *
   * @param str the input string to search.
   */
  public static int findFirstNonNumericCharIndex(String str) {
    str = str.trim();
    int index = 0;
    while (index < str.length()
        &&
        (Character.isDigit(str.charAt(index)) || str.charAt(index) == '.')) {
      index++;
    }
    if (index < str.length()) {
      return index;
    } else {
      return -1;
    }
  }

  /**
   * build message from formated message
   * ------------------------------------------
   * ex: source =  "{} must be greater than {}"
   * getMsg(source, "run count", 5);
   * output: "run count must be greater than 5"
   *
   * @param template template message
   * @param params toString() able ... params
   * @return message
   */
  public static String getMsg(String template, Object... params) {
    for (Object param : params) {
      template = template.replaceFirst("\\{\\}", param.toString());
    }
    return template;
  }

}
