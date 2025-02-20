package org.fit.ssapp.ss.smt.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.response.ComputerSpecs;
import org.fit.ssapp.ss.smt.Matches;

/**
 * Represents a solution to the Stable Matching Problem.
 * This class stores details about a single matching solution obtained from an algorithm.
 * Each solution contains:
 * Matching results (`matches`): The final set of matches produced by the algorithm.
 * Fitness value (`fitnessValue`): A score indicating the quality of the solution.
 * Runtime (`runtime`): Execution time (in seconds) taken to compute the solution.
 * Computer specifications (`computerSpecs`): Hardware details of the machine running the algorithm.
 * Algorithm name (`algorithm`): The name of the algorithm used to generate the solution.
 * Set satisfactions (setSatisfactions): An array containing satisfaction values for different sets.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingSolution {

  private Matches matches;
  private double fitnessValue;
  private double runtime;
  private ComputerSpecs computerSpecs;
  private String algorithm;
  private double[] setSatisfactions;

}
