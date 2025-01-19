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

  public Strategy getStrategyAt(int index) {
    return strategies.get(index);
  }

  public void removeStrategiesAt(int index) {
    strategies.set(index, null);
  }

  public void removeAllNull() {
    strategies.removeIf(Objects::isNull);
  }

  public int getDominantStrategyIndex() {

    List<Double> payoffs = strategies.stream().map(Strategy::getPayoff).toList();

    double maxPayoffValue = payoffs.stream().max(Double::compareTo).orElse(0D);

    // return index of the strategy having the max payOffValue
    return payoffs.indexOf(maxPayoffValue);
  }

  public String toString() {
    StringBuilder NP = new StringBuilder();
    for (Strategy s : strategies) {
      if (s == null) {
        continue;
      }
      NP.append("\nStrategy ").append(strategies.indexOf(s) + 1).append(":\t");
      NP.append(s).append("\nPayoff: ").append(s.getPayoff());
    }
    return NP.toString();
  }
}
