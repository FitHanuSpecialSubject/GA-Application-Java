package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fit.ssapp.util.StringExpressionEvaluator;

/**
 * Represents a game player with various attributes. a @Setter annotation automatically generates
 * setter methods.
 */
@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NormalPlayer implements Serializable {

  private List<Strategy> strategies;
  private List<BigDecimal> payoffValues;
  private int prevStrategyIndex = -1; // this is for the problem with dynamic data
  private String payoffFunction;

  /**
   * Retrieves the strategy at the specified index.
   */
  public Strategy getStrategyAt(int index) {
    return strategies.get(index);
  }

  /**
   * Removes the strategy at the specified index by setting it to null.
   *
   * @param index The index of the strategy to remove.
   * @throws IndexOutOfBoundsException if the index is out of range.
   */
  public void removeStrategiesAt(int index) {
    strategies.set(index, null);
  }


  /**
   * Generates a string representation of the strategies and their payoffs.
   *
   * @return A formatted string containing the list of strategies and their payoffs.
   */
  public String toString() {
    StringBuilder np = new StringBuilder();
    for (Strategy s : strategies) {
      if (s == null) {
        continue;
      }
      np.append("\nStrategy ").append(strategies.indexOf(s) + 1).append(":\t");
      np.append(s).append("\nPayoff: ").append(s.getPayoff());
    }
    return np.toString();
  }
}