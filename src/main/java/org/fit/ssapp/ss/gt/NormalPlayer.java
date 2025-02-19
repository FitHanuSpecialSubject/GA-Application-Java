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

  private String name;
  private List<Strategy> strategies;
  private List<BigDecimal> payoffValues;
  private int prevStrategyIndex = -1; // this is for the problem with dynamic data
  private String payoffFunction;
  private BigDecimal payoff;

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
   * Removes all null values from the strategy list.
   */
  public void removeAllNull() {
    strategies.removeIf(Objects::isNull);
  }

  /**
   * Finds and returns the index of the dominant strategy, which is the strategy with the highest
   * payoff value.
   *
   * @return The index of the dominant strategy, or -1 if no strategies exist.
   */
  public int getDominantStrategyIndex() {
    List<Double> payoffs = strategies.stream()
        .map(Strategy::getPayoff)
        .toList();

    double maxPayoffValue = payoffs.stream()
            .max(Double::compareTo)
            .orElse(0D);

    // return index of the strategy having the max payOffValue
    return payoffs.indexOf(maxPayoffValue);
  }


  public void evaluatePayoff(List<NormalPlayer> normalPlayers, int[] chosenStrategyIndices) {
    if (payoffFunction != null && !payoffFunction.isBlank()) {
      this.payoff = StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(this.getStrategyAt(chosenStrategyIndices[this.strategies.indexOf(this)]), payoffFunction, normalPlayers, chosenStrategyIndices);
    } else {
      // Default behavior: sum all properties of the strategy
      this.payoff = StringExpressionEvaluator.calculateDefault(this.getStrategyAt(chosenStrategyIndices[this.strategies.indexOf(this)]).getProperties(), null);
    }
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