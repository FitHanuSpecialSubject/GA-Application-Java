package org.fit.ssapp.dto.request.functioncheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * Request for checking a Stable Matching <b>fitness function</b> (variables M{n}, S(k),
 * SIGMA{...}) without running MOEA.
 * <p>
 * Dataset mode: send {@code problem}. A matching is produced by the real stable matching
 * algorithm from a {@code permutation} (proposal order, defaults to 0..n-1), then satisfactions
 * and fitness are computed. Manual mode: send {@code satisfactions} (and optionally
 * {@code setIndices}) directly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmFitnessCheckRequest {

  /** The fitness function to test. */
  private String function;

  /** Uploaded / typed problem data (dataset mode). */
  private StableMatchingProblemDto problem;

  /** Matching type used in dataset mode: MTM (default) or OTM. */
  private String matchingType;

  /** Proposal order (a permutation of 0..n-1) fed to the stable matching algorithm. */
  private int[] permutation;

  /** Manual satisfaction of every individual (manual mode). */
  private double[] satisfactions;

  /** Set index (0 / 1) of every individual in manual mode; defaults to a half/half split. */
  private int[] setIndices;
}
