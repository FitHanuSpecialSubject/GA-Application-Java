package com.example.SS2_Backend.model.stableMatching.Requirement;

import lombok.Getter;

@Getter
public abstract class Requirement {

    private final int type;

    protected Requirement(int type) {
        this.type = type;
    }

    public abstract double getValueForFunction();

    public int getTargetValue() {
        return 0;
    }

    public double getBound() {
        return 0;
    }

    public String getExpression() {
        return null;
    }

    public double getUpperBound() {
        return 0;
    }

    public double getLowerBound() {
        return 0;
    }

    public abstract String toString();

}