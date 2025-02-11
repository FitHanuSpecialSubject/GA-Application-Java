package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fit.ssapp.util.StringExpressionEvaluator;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Strategy implements Serializable {

  private String name;
  private List<Double> properties = new ArrayList<>();
  private double payoff;

  public List<Double> getProperties() {
    return properties;
  }

  public double getPayoff() {
    return payoff;
  }

  public void setPayoff(double payoff) {
    this.payoff = payoff;
  }

  public double evaluateStringExpression(String expression, List<NormalPlayer> normalPlayers, int[] chosenStrategyIndices) {
    return StringExpressionEvaluator.evaluatePayoffFunctionWithRelativeToOtherPlayers(this, expression, normalPlayers, chosenStrategyIndices).doubleValue();
  }

  public void addProperty(double property) {
    properties.add(property);
  }

  @Override
  public String toString() {
    StringBuilder props = new StringBuilder("[ ");
    for (double prop : properties) {
      props.append(prop).append(", ");
    }
    return props.substring(0, props.length() - 2) + " ]";
  }
}