package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fit.ssapp.util.StringExpressionEvaluator;

/**
 * Represents a Strategy with a name, properties, and a payoff value. Implements Serializable for
 * object persistence.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Strategy implements Serializable {

  private String name;
  @Getter
  private List<Double> properties = new ArrayList<>();
  @Setter
  @Getter
  private double payoff;

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