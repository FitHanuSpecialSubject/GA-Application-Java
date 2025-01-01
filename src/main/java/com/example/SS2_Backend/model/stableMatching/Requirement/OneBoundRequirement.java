package com.example.SS2_Backend.model.stableMatching.Requirement;

import lombok.Getter;

import static com.example.SS2_Backend.util.NumberUtils.formatDouble;

@Getter
public class OneBoundRequirement extends Requirement {

    @Getter
    private final double bound;
    private final boolean expression;

    public OneBoundRequirement(double bound, boolean expression) {
        super(1);
        this.bound = bound;
        this.expression = expression;
    }

    private String expressionToString(boolean expression) {
        return expression ? "++" : "--";
    }

    @Override
    public double getValueForFunction() {
        return bound;
    }

    public String toString() {
        return "[" + formatDouble(bound) + ", " + expressionToString(expression) + "]";
    }

}
