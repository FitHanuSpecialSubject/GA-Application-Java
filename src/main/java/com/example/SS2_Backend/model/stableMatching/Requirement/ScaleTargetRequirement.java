package com.example.SS2_Backend.model.stableMatching.Requirement;

import lombok.Getter;

@Getter
public class ScaleTargetRequirement extends Requirement {

    private final int targetValue;

    public ScaleTargetRequirement(int targetValue) {
        super(0);
        this.targetValue = targetValue;
    }

    @Override
    public double getValueForFunction() {
        return targetValue;
    }

    @Override
    public String toString() {
        return "[" + this.targetValue + "]";
    }

}
