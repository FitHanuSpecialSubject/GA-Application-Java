package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialPlayer implements Serializable {

  private int numberOfProperties;
  private final List<Double> properties = new ArrayList<>();
  private final List<Double> weights = new ArrayList<>();
  private double payoff;


  /**
   * @return properties size
   */
  public int getNumberOfProperties() {
    return numberOfProperties;
  }

  /**
   * @modifies properties
   * @effects add more property into properties
   */
  public void addProperty(double property) {
    properties.add(property);
  }

  /**
   * @modifies weight
   * @effects add more weight into weights
   */
  public void addWeight(double weight) {
    weights.add(weight);
  }

  /**
   * @modifies properties
   * @effects set number of Properties
   */
  public void setNumberOfProperties(int numberOfProperties) {
    this.numberOfProperties = numberOfProperties;
  }

  public void setPayoff() {
    double newPayoff = 0;
    for (int i = 0; i < properties.size(); ++i) {
      newPayoff += properties.get(i) * weights.get(i);
    }
    this.payoff = newPayoff;
  }

  public double getPayoff() {
    return payoff;
  }

  public String toString() {
    StringBuilder SP = new StringBuilder("SPECIAL PLAYER: \nProperties: \n");
    for (Double x : properties) {
      SP.append(x).append("\t");
    }
    SP.append("\nWeight:\n");
    for (Double x : weights) {
      SP.append(x).append("\t");
    }
    return SP + "\nPayoff: " + payoff;
  }
}
