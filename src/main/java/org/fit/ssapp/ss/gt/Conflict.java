package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a conflict between two players in a game theory context, where each player has a
 * strategy associated with their action. This class is used to store the conflict data and provide
 * methods for displaying and handling conflict information.
 */

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conflict implements Serializable {

  private int leftPlayer;
  private int rightPlayer;
  private int leftPlayerStrategy;
  private int rightPlayerStrategy;


  /**
   * Returns a string representation of the Conflict object, showing the players and their
   * strategies. The returned string follows the format: "Player: leftPlayer, Strategy:
   * leftPlayerStrategy, Player: rightPlayer, Strategy: rightPlayerStrategy"
   *
   * @return A formatted string displaying the players and their strategies.
   */
  public String toString() {
    return String.format("Player: %s, Strategy: %s, Player: %s, Strategy: %s", leftPlayer + 1,
        leftPlayerStrategy + 1, rightPlayer + 1, rightPlayerStrategy + 1);
  }
}
