package org.fit.ssapp.dto.request;

/**
 * Base request dto class for this application.
 */
public interface ProblemRequestDto {

  /**
   * Get code name of one of MOEA supported Genetic Algorithms.
   *
   * @return algorithm code name.
   */
  String getAlgorithm();

  int getGeneration();

  int getPopulationSize();
}
