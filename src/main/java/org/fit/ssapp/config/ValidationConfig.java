package org.fit.ssapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

    @Value("${validation.population.max}")
    private int maxPopulation;

    @Value("${validation.generation.max}")
    private int maxGeneration;

    @Value("${validation.individuals.min}")
    private int minIndividuals;

    public int getMaxPopulation() {
        return maxPopulation;
    }

    public int getMaxGeneration() {
        return maxGeneration;
    }

    public int getMinIndividuals() {
        return minIndividuals;
    }
}

