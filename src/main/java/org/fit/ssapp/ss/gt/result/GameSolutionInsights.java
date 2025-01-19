package org.fit.ssapp.ss.gt.result;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.response.ComputerSpecs;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSolutionInsights {

  Map<String, List<Double>> fitnessValues;
  Map<String, List<Double>> runtimes;
  private ComputerSpecs computerSpecs;
}
