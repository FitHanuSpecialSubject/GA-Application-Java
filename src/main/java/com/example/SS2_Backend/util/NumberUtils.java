package com.example.SS2_Backend.util;

import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtils {

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

    public static boolean isDouble(String str) {
        if (!str.contains(",") || !str.contains(".")) {
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

    public static Double formatDouble(double val) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.##", symbols);
        String formattedValue = df.format(val);
        return Double.parseDouble(formattedValue);
    }

    /**
     * floor real variable, parse int & return
     *
     * @param variable Framework shi
     * @return int
     */
    public static int toInteger(RealVariable variable) {
        double rawValue = EncodingUtils.getReal(variable);
        return (int) Math.floor(rawValue);
    }

}
