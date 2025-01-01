package com.example.SS2_Backend.dto.request;

import com.example.SS2_Backend.ss.gt.Conflict;
import com.example.SS2_Backend.ss.gt.NormalPlayer;
import com.example.SS2_Backend.ss.gt.SpecialPlayer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameTheoryProblemDTO implements ProblemRequestDto{
    private SpecialPlayer specialPlayer;
    private List<NormalPlayer> normalPlayers;
    private List<Conflict> conflictSet = new ArrayList<>();
    private String fitnessFunction;
    private String defaultPayoffFunction;
    private String algorithm;
    private boolean isMaximizing;
    private String distributedCores;
    private Integer maxTime;
    private Integer generation;
    private Integer populationSize;

    @Override
    public String toString() {
        return "GameTheoryProblemDTO{" +
                "specialPlayer=" + specialPlayer +
                ", number of normal players=" + normalPlayers.size() +
                ", conflictSet=" + conflictSet +
                ", fitnessFunction='" + fitnessFunction + '\'' +
                ", defaultPayoffFunction='" + defaultPayoffFunction + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", isMaximizing=" + isMaximizing +
                ", distributedCores='" + distributedCores + '\'' +
                ", maxTime=" + maxTime +
                ", generation=" + generation +
                ", populationSize=" + populationSize +
                '}';
    }
}
