package com.example.SS2_Backend.model.stableMatching;

import com.example.SS2_Backend.model.stableMatching.Requirement.OneBoundRequirement;
import com.example.SS2_Backend.model.stableMatching.Requirement.Requirement;
import com.example.SS2_Backend.model.stableMatching.Requirement.ScaleTargetRequirement;
import com.example.SS2_Backend.model.stableMatching.Requirement.TwoBoundRequirement;
import lombok.Getter;

import java.util.Objects;

import static com.example.SS2_Backend.util.NumberUtils.isDouble;
import static com.example.SS2_Backend.util.NumberUtils.isInteger;

@Getter
public class PropertyRequirement {
    private static final boolean INCREMENT = true;
    private static final boolean DECREMENT = false;
    private final Requirement requirement;

    public PropertyRequirement(String[] inputRequirement) {
        this.requirement = setRequirement(inputRequirement);
    }

    public static Requirement setRequirement(String[] array) {
        try {
            if (Objects.equals(array[1], "++")) {
                return new OneBoundRequirement(Double.parseDouble(array[0]), INCREMENT);
            } else if (Objects.equals(array[1], "--")) {
                return new OneBoundRequirement(Double.parseDouble(array[0]), DECREMENT);
            } else if (Objects.equals(array[1], null)) {
                if (isInteger(array[0])) {
                    return new ScaleTargetRequirement(Integer.parseInt(array[0]));
                } else if (isDouble(array[0])) {
                    return new OneBoundRequirement(Double.parseDouble(array[0]), INCREMENT);
                } else {
                    return new OneBoundRequirement(0.0, INCREMENT);
                }
            } else {
                double value1 = Double.parseDouble(array[0]);
                double value2 = Double.parseDouble(array[1]);
                return new TwoBoundRequirement(value1, value2);
            }
        } catch (NumberFormatException e) {
            return new OneBoundRequirement(0.0, INCREMENT);
        }
    }

    @Override
    public String toString() {
        return "Requirement: " + requirement;
    }
}
