package org.fit.ssapp.ss.gt;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setStrategies(List<Strategy> strategies) {
    this.strategies = strategies;
  }

  public List<BigDecimal> getPayoffValues() {
    return payoffValues;
  }

  public void setPayoffValues(List<BigDecimal> payoffValues) {
    this.payoffValues = payoffValues;
  }

  public Strategy getStrategyAt(int index) {
    return strategies.get(index);
  }

  public void removeStrategiesAt(int index) {
    strategies.set(index, null);
  }

  public String getPayoffFunction() {
    return payoffFunction;
  }

  public void removeAllNull() {
    strategies.removeIf(Objects::isNull);
  }

  public int getDominantStrategyIndex() {

    List<Double> payoffs = strategies.stream()
        .map(Strategy::getPayoff)
        .collect(Collectors.toList());

    double maxPayoffValue = payoffs.stream()
        .max(Double::compareTo)
        .orElse(0D);

    // return index of the strategy having the max payOffValue
    return payoffs.indexOf(maxPayoffValue);
  }

  public List<Strategy> getStrategies() {
    return strategies;
  }

  public BigDecimal getPayoff() {
    return payoff;
  }

  public void setPrevStrategyIndex(int prevStrategyIndex) {
    this.prevStrategyIndex = prevStrategyIndex;
  }

  public int getPrevStrategyIndex() {
    return prevStrategyIndex;
  }


  public void setPayoffFunction(String payoffFunction) {
    this.payoffFunction = payoffFunction;
  }

  public void setPayoff(BigDecimal payoff) {
    this.payoff = payoff;
  }

  public double getPurePayoff() {

    // return sum of all payoffs
    return strategies.stream()
        .map(Strategy::getPayoff)
        .reduce(Double::sum)
        .orElse(0D);
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
