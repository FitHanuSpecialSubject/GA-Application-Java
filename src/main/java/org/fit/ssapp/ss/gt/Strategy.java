package org.fit.ssapp.ss.gt;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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


  @SuppressWarnings("unused")
  private double evaluateStringExpression(String expression) {
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName("JavaScript");
    try {
      Object result = engine.eval(expression);
      return Double.parseDouble(result.toString());
    } catch (ScriptException e) {
      throw new RuntimeException(e);
    }
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
