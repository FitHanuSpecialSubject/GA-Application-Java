package org.fit.ssapp.ss.gt;

import java.io.Serializable;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SuppressWarnings({"checkstyle:MissingJavadocType", "checkstyle:SummaryJavadoc"})
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conflict implements Serializable {

  private int leftPlayer;
  private int rightPlayer;
  private int leftPlayerStrategy;
  private int rightPlayerStrategy;

  /**
   *
   */
  @SuppressWarnings("unused")
  public Conflict(String conflict) {
    System.out.println("conflict = " + conflict);
    String[] conflictSet = conflict.split(",");
    System.out.println("Arrays.toString(conflictSet) = " + Arrays.toString(conflictSet));
    if (conflictSet.length != 4) {
      System.err.print("Invalid Conflict input data format");
      return;
    }
    leftPlayer = Integer.parseInt(conflictSet[0]) - 1;
    leftPlayerStrategy = Integer.parseInt(conflictSet[1]) - 1;
    rightPlayer = Integer.parseInt(conflictSet[2]) - 1;
    rightPlayerStrategy = Integer.parseInt(conflictSet[3]) - 1;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public String toString() {
    return String.format("Player: %s, Strategy: %s, Player: %s, Strategy: %s", leftPlayer + 1,
        leftPlayerStrategy + 1, rightPlayer + 1, rightPlayerStrategy + 1);
  }
}
