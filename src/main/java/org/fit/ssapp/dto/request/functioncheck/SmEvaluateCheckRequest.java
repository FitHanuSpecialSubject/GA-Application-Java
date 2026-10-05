package org.fit.ssapp.dto.request.functioncheck;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;

/**
 * Request for checking a Stable Matching <b>evaluate function</b> (the function that scores how
 * much an individual likes another one, variables P{n}, W{n}, R{n}).
 * <p>
 * Dataset mode: send {@code problem} + {@code evaluatorIndex} + {@code evaluatedIndex} (0-based);
 * variable values are read from the data. Manual mode: send {@code variables}, e.g.
 * {@code {"P1": 3, "W1": 5, "R1": 1}}. With neither, only the syntax is validated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmEvaluateCheckRequest {

  /** The evaluate function to test. */
  private String function;

  /** Manual variable values (P1, W1, R1, ...). */
  private Map<String, Double> variables;

  /** Uploaded / typed problem data. Not validated with bean validation on purpose. */
  private StableMatchingProblemDto problem;

  /** Individual who is doing the evaluating (0-based). Decides which set's function is tested. */
  private Integer evaluatorIndex;

  /** Individual being evaluated (0-based), must belong to the other set. */
  private Integer evaluatedIndex;
}
