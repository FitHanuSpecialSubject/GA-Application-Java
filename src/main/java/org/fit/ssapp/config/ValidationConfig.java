package org.fit.ssapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "validation.stable-matching")
@Data
public class ValidationConfig {
  @Value("${validation.stable-matching.population.max}")
  private int maxPopulation;

  @Value("${validation.stable-matching.generation.max}")
  private int maxGeneration;

  @Value("${validation.stable-matching.individuals.min}")
  private int minIndividualCount;

  @Value("${validation.stable-matching.run-count-per-algorithm.min}")
  private int minRunCountPerAlgorithm;

  @Value("${validation.stable-matching.run-count-per-algorithm.max}")
  private int maxRunCountPerAlgorithm;

}
