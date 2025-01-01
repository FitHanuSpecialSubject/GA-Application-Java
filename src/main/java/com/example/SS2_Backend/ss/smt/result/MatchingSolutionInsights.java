package com.example.SS2_Backend.ss.smt.result;

import com.example.SS2_Backend.dto.response.ComputerSpecs;
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
public class MatchingSolutionInsights {

    Map<String, List<Double>> fitnessValues;
    Map<String, List<Double>> runtimes;
    private ComputerSpecs computerSpecs;

}
