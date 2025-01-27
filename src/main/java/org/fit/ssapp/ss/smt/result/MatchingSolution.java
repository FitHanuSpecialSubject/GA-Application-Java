package org.fit.ssapp.ss.smt.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.response.ComputerSpecs;
import org.fit.ssapp.ss.smt.Matches;

/**
 *
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
