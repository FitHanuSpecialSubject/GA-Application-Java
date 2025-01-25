package org.fit.ssapp.ss.gt;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

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


  private double evaluateStringExpression(String expression) {
    ExpressionBuilder builder = new ExpressionBuilder(expression);
    Expression expr = builder.build();

//    for (int i = 0; i < properties.size(); i++) {
//      builder.variable("p" + (i + 1));
//    }

    for (int i = 0; i < properties.size(); i++) {
      expr.setVariable("p" + (i + 1), properties.get(i));
    }

    // Evaluate the expression
    ValidationResult validationResult = expr.validate();
    if (validationResult.isValid()) {
      return expr.evaluate();
    } else {
      throw new RuntimeException("Invalid payoff expression: " + validationResult.getErrors().toString());
    }
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
