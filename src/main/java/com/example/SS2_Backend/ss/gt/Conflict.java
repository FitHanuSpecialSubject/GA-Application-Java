package com.example.SS2_Backend.ss.gt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conflict implements Serializable {
    private int leftPlayer;
    private int rightPlayer;
    private int leftPlayerStrategy;
    private int rightPlayerStrategy;

    public Conflict(String conflict) {
        System.out.println("conflict = " + conflict);;
        String[] conflictSet = conflict.split(",");
        System.out.println("Arrays.toString(conflictSet) = " + Arrays.toString(conflictSet));
        if (conflictSet.length != 4) {
            System.err.print("Invalid Conflict input data format, it should be: 'left player, left player strategy, right player, right player strategy'");
            return;
        }
        leftPlayer = Integer.parseInt(conflictSet[0]) - 1;
        leftPlayerStrategy = Integer.parseInt(conflictSet[1]) - 1;
        rightPlayer = Integer.parseInt(conflictSet[2]) - 1;
        rightPlayerStrategy = Integer.parseInt(conflictSet[3]) - 1;
    }

    public int getLeftPlayer() {
        return leftPlayer;
    }

    public int getRightPlayer() {
        return rightPlayer;
    }

    public int getLeftPlayerStrategy() {
        return leftPlayerStrategy;
    }

    public int getRightPlayerStrategy() {
        return rightPlayerStrategy;
    }

    public String toString() {
        return String.format("Player: %s, Strategy: %s, Player: %s, Strategy: %s", leftPlayer + 1, leftPlayerStrategy + 1, rightPlayer + 1, rightPlayerStrategy + 1);
    }
}
