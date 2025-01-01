package com.example.SS2_Backend.ss.smt.requirement;

import com.example.SS2_Backend.ss.smt.requirement.impl.OneBound;
import com.example.SS2_Backend.ss.smt.requirement.impl.TwoBound;
import com.example.SS2_Backend.ss.smt.requirement.impl.ScaleTarget;

import java.util.Arrays;
import java.util.Objects;

import static com.example.SS2_Backend.util.NumberUtils.isDouble;
import static com.example.SS2_Backend.util.NumberUtils.isInteger;
import static com.example.SS2_Backend.util.StringUtils.findFirstNonNumericCharIndex;

/**
 * Decoder for requirement
 * TODO: Hơi bị thừa logic do bê từ code cũ sang
 * TODO: tối giản hóa logic map
 */
public class RequirementDecoder {

    private static final boolean INCREMENT = true;
    private static final boolean DECREMENT = false;

    /**
     * Decode String[][] of requirements in request to Requirement[][]
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
