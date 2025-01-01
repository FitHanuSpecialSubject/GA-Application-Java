package com.example.SS2_Backend.service;

import com.example.SS2_Backend.constants.MatchingConst;
import com.example.SS2_Backend.dto.mapper.StableMatchingProblemMapper;
import com.example.SS2_Backend.dto.request.StableMatchingProblemDTO;
import com.example.SS2_Backend.ss.smt.Matches;
import com.example.SS2_Backend.ss.smt.implement.MTMProblem;
import com.example.SS2_Backend.util.MatchingProblemType;
import com.example.SS2_Backend.util.SampleDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moeaframework.core.Solution;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MTMProblemTest {
    StableMatchingProblemDTO stableMatchingProblemDTO;
    SampleDataGenerator sampleData;
    int numberOfIndividuals1;
    int numberOfIndividuals2;
    int numberOfProperties;

    @BeforeEach
    public void setUp() {
        numberOfIndividuals1 = 20;
        numberOfIndividuals2 = 200;
        numberOfProperties = 5; // Initialize numberOfProperties
        sampleData = new SampleDataGenerator(MatchingProblemType.MTM, numberOfIndividuals1, numberOfIndividuals2, numberOfProperties);
    }

    @Test
    public void testNodeCapacity() {
        stableMatchingProblemDTO = sampleData.generateDto();
        MTMProblem problem = StableMatchingProblemMapper.toMTM(stableMatchingProblemDTO);

        // Create a Solution to test and get Matches from the Solution
        Solution solution = problem.newSolution();
        problem.evaluate(solution);
        Matches matches = (Matches) solution.getAttribute(MatchingConst.MATCHES_KEY);
        for (int i = 0; i < stableMatchingProblemDTO.getNumberOfIndividuals(); i++) {
            // Getting individual
            int capacity = problem.getMatchingData().getCapacityOf(i);
            int matchedCount = matches.getSetOf(i).size();
            assertTrue(matchedCount <= capacity, "Node " + i + " has exceeded its capacity");
        }
    }
}