package org.fit.ssapp.dto.response.functioncheck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Outcome of testing one function (payoff / evaluate / fitness).
 * <p>
 * {@code value} is what the production code computed. {@code referenceValue} is an independent
 * computation (variables bound by exp4j instead of string replacement); when both differ
 * {@code mismatch} is true, which points at a bug in the production evaluation.
 */
@Data
public class FunctionCheckResult {

  private String function;
  private boolean success;
  private boolean syntaxValid;
  private Double value;
  private Double referenceValue;
  private boolean mismatch;
  private String error;
  private List<String> requiredVariables = new ArrayList<>();
  private Map<String, Double> variablesUsed = new LinkedHashMap<>();
  private Map<String, Object> details = new LinkedHashMap<>();
  private List<String> notes = new ArrayList<>();
}
