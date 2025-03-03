package org.fit.ssapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "validation")
@Data
public class ValidationConfig {

    @Value("${validation.population.max}")
    private int maxPopulation;

    @Value("${validation.generation.max}")
    private int maxGeneration;

    @Value("${validation.individuals.min}")
    private int minIndividuals;
}
