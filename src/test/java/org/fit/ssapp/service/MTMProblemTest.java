package org.fit.ssapp.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fit.ssapp.constants.MatchingConst;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.implement.MTMProblem;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moeaframework.core.Solution;

public class MTMProblemTest {

  StableMatchingProblemDto stableMatchingProblemDto;
  SampleDataGenerator sampleData;
  int numberOfIndividuals1;
  int numberOfIndividuals2;
  int numberOfProperties;

  @BeforeEach
  public void setUp() {
    numberOfIndividuals1 = 20;
    numberOfIndividuals2 = 200;
    numberOfProperties = 5; // Initialize numberOfProperties
    sampleData = new SampleDataGenerator(MatchingProblemType.MTM, numberOfIndividuals1,
        numberOfIndividuals2, numberOfProperties);
  }

  @Test
  public void testNodeCapacity() {
    stableMatchingProblemDto = sampleData.generateDto();
    MTMProblem problem = StableMatchingProblemMapper.toMTM(stableMatchingProblemDto);

    // Create a Solution to test and get Matches from the Solution
    Solution solution = problem.newSolution();
    problem.evaluate(solution);
    Matches matches = (Matches) solution.getAttribute(MatchingConst.MATCHES_KEY);
    for (int i = 0; i < stableMatchingProblemDto.getNumberOfIndividuals(); i++) {
      // Getting individual
      int capacity = problem.getMatchingData().getCapacityOf(i);
      int matchedCount = matches.getSetOf(i).size();
      assertTrue(matchedCount <= capacity, "Node " + i + " has exceeded its capacity");
    }
  }
}