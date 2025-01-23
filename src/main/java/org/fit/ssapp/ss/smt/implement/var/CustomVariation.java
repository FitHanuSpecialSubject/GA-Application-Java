package org.fit.ssapp.ss.smt.implement.var;

import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class CustomVariation implements Variation {
    private final double mutationRate;

    public CustomVariation(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    @Override
    public String getName() {
        return "CustomVariation";
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Solution[] evolve(Solution[] solutions) {
        return new Solution[0];
    }
}
