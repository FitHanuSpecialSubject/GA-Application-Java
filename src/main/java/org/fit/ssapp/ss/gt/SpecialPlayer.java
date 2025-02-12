package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Represents a Special Player with properties, weights, and a payoff. Implements Serializable for
 * object persistence.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialPlayer implements Serializable {

  @Setter
  private int numberOfProperties;
  private final List<Double> properties = new ArrayList<>();
  private final List<Double> weights = new ArrayList<>();
  @Getter
  private double payoff;

  /**
   * Returns a formatted string representation of the Special Player.
   *
   * @return A string describing the player's properties, weights, and payoff.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SPECIAL PLAYER: \nProperties: \n");
    for (Double x : properties) {
      sb.append(x).append("\t");
    }
    sb.append("\nWeight:\n");
    for (Double x : weights) {
      sb.append(x).append("\t");
    }
    return sb + "\nPayoff: " + payoff;
  }
}
