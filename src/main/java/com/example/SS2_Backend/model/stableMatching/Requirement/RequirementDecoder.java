package com.example.SS2_Backend.model.stableMatching.Requirement;

import static com.example.SS2_Backend.util.NumberUtils.isDouble;
import static com.example.SS2_Backend.util.NumberUtils.isInteger;
import static com.example.SS2_Backend.util.StringUtils.findFirstNonNumericCharIndex;

/**
 * Decoder for requirement
 */
public class RequirementDecoder {

    public static String[] decodeInputRequirement(String item) {
        item = item.trim();
        String[] result = new String[2];
        int index = findFirstNonNumericCharIndex(item);
        if (index == -1) {
            if (isInteger(item)) {
                try {
                    int a = Integer.parseInt(item);
                    result[0] = item;
                    if (a >= 0 && a <= 10) {
                        result[1] = null;
                    } else {
                        result[1] = "++";
                    }
                } catch (NumberFormatException e) {
                    System.out.println("error index - 1");
                    result[0] = "-1";
                    result[1] = "++";
                }
            } else if (isDouble(item)) {
                result[0] = "-2";
                result[1] = null;
            } else {
                result[0] = "-3";
                result[1] = null;
            }
        } else {
            if (item.contains(":")) {
                String[] parts = item.split(":");
                result[0] = parts[0].trim();
                result[1] = parts[1].trim();
            } else if (item.contains("++")) {
                String[] parts = item.split("\\+\\+");
                result[0] = parts[0].trim();
                result[1] = "++";
            } else if (item.contains("--")) {
                String[] parts = item.split("--");
                result[0] = parts[0].trim();
                result[1] = "--";
            } else {
                result[0] = "-2";
                result[1] = "++";
            }
        }
        return result;
    }

}
