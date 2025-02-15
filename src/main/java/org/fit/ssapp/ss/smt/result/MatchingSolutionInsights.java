package org.fit.ssapp.ss.smt.result;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.response.ComputerSpecs;

/**
 * Represents insights collected from solving a Stable Matching Problem using different algorithms.
 * This class stores benchmarking results, including:
 * Fitness values (fitnessValues): A mapping of algorithm names to lists of computed fitness values.
 * Runtimes (runtimes): A mapping of algorithm names to lists of execution times (in seconds).
 * Computer specifications (computerSpecs)**: Hardware details of the system running the algorithms.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingSolutionInsights {

  Map<String, List<Double>> fitnessValues;
  Map<String, List<Double>> runtimes;
  private ComputerSpecs computerSpecs;

}
