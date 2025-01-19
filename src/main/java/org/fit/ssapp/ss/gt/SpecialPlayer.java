package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialPlayer implements Serializable {

  /**
   * -- SETTER --
   */
  @Setter
  private int numberOfProperties;
  private final List<Double> properties = new ArrayList<>();
  private final List<Double> weights = new ArrayList<>();
  @Getter
  private double payoff;

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
