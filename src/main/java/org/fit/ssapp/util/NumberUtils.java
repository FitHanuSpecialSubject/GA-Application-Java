package org.fit.ssapp.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

/**
 * Utility class for number-related operations.
 */
public class NumberUtils {

  /**
   * Checks if a given string can be parsed as an integer.
   *
   * @param str the string to check
   * @return true if the string is a valid integer, false otherwise
   */
  public static boolean isInteger(String str) {
    try {
      // Attempt to parse the String as an integer
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      // The String is not a valid integer
      return false;
    }
  }


  /**
   * Checks if a given string can be parsed as a double.
   *
   * @param str the string to check
   * @return true if the string is a valid double, false otherwise
   */
  public static boolean isDouble(String str) {
    if (!str.contains(",") && !str.contains(".")) {
      return false;
    }
    try {
      // Attempt to parse the String as an integer
      Double.parseDouble(str);
      return true;
    } catch (NumberFormatException e) {
      // The String is not a valid integer
      return false;
    }
  }

  /**
   * Formats a double value to two decimal places.
   *
   * @param val the double value to format
   * @return the formatted double value
   */
  public static Double formatDouble(double val) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    DecimalFormat df = new DecimalFormat("#.##", symbols);
    String formattedValue = df.format(val);
    return Double.parseDouble(formattedValue);
  }

  /**
   * Converts a RealVariable object to an integer by taking the floor of its value.
   *
   * @param variable the RealVariable to convert
   * @return the integer representation of the variable
   */
  public static int toInteger(RealVariable variable) {
    double rawValue = EncodingUtils.getReal(variable);
    return (int) Math.floor(rawValue);
  }

}
