package com.example.SS2_Backend.ss.gt.result;

import com.example.SS2_Backend.dto.response.ComputerSpecs;
import com.example.SS2_Backend.util.ComputerSpecsUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSolutionInsights {
    Map<String, List<Double>> fitnessValues;
    Map<String, List<Double>> runtimes;
    private ComputerSpecs computerSpecs;

    public ComputerSpecs getComputerSpecs() {
        return ComputerSpecsUtil.getComputerSpecs();
    }
}
