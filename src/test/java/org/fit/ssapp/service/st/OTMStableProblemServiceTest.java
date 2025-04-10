package org.fit.ssapp.service.st;
//package com.example.SS2_Backend.service;
//
//import com.example.SS2_Backend.dto.request.StableMatchingProblemDto;
//import com.example.SS2_Backend.dto.response.Response;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.util.Arrays;
//import java.util.Random;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ExtendWith(MockitoExtension.class)
//class OTMStableProblemServiceTest {
//
//    @Mock
//    private SimpMessagingTemplate mockSimpMessagingTemplate;
//
//    private StableMatchingOtmService otmStableMatchingSolverUnderTest;
//
//    @BeforeEach
//    void setUp() {
//        otmStableMatchingSolverUnderTest = new StableMatchingOtmService(mockSimpMessagingTemplate);
//    }
//
//    @Test
//    void testSolve() {
//        // Setup a two-set stable matching problem
//        final StableMatchingProblemDto request = new StableMatchingProblemDto();
//        request.setProblemName("Two-Set Stable Matching Optimization");
//
//        // Two sets (e.g., Students and Universities)
//        request.setNumberOfSets(2);
//
//        // Total number of individuals across both sets
//        request.setNumberOfIndividuals(1500);
//
//        // Assume 500 universities (set 0) and 1000 students (set 1)
//        int[] setIndices = new int[1500];
//        Arrays.fill(setIndices, 0, 500, 0);  // First 500 are universities
//        Arrays.fill(setIndices, 500, 1500, 1); // Next 1000 are students
//        request.setIndividualSetIndices(setIndices);
//
//        // Capacities for universities (how many students they can accept)
//        int[] capacities = new int[1500];
//        for (int i = 0; i < 500; i++) {
//            capacities[i] = 3 + (i % 3);  // Universities can take 3, 4, or 5 students
//        }
//        Arrays.fill(capacities, 500, 1500, 1);  // Students have individual capacity of 1
//        request.setIndividualCapacities(capacities);
//
//        // Properties could represent things like academic performance, research interests, etc.
//        request.setNumberOfProperty(2);
//
//        // Student requirements for universities
//        String[][] requirements = new String[1500][2];
//        for (int i = 0; i < 500; i++) {
//            requirements[i][0] = "ResearchCapacity";
//            requirements[i][1] = "FundingLevel";
//        }
//        for (int i = 500; i < 1500; i++) {
//            requirements[i][0] = "AcademicScore";
//            requirements[i][1] = "ResearchInterest";
//        }
//        request.setIndividualRequirements(requirements);
//
//        // Weights for evaluating match quality
//        double[][] weights = new double[1500][2];
//        for (int i = 0; i < 500; i++) {
//            weights[i][0] = 0.7;  // Universities care more about research
//            weights[i][1] = 0.3;  // Less about funding
//        }
//        for (int i = 500; i < 1500; i++) {
//            weights[i][0] = 0.6;  // Students care about academic performance
//            weights[i][1] = 0.4;  // And research interests
//        }
//        request.setIndividualWeights(weights);
//
//        // Actual property values
//        double[][] properties = new double[1500][2];
//        // Universities' properties
//        Random rand = new Random();
//        for (int i = 0; i < 500; i++) {
//            properties[i] = new double[]{7.0 + rand.nextDouble() * 3.0, 7.0 + rand.nextDouble() * 3.0};  // Random values between 7 and 10
//        }
//
//        // Students' properties (random but realistic academic scores and research interests)
//        for (int i = 500; i < 1500; i++) {
//            properties[i][0] = 5.0 + rand.nextDouble() * 4.0;  // Academic score 5-9
//            properties[i][1] = rand.nextDouble();  // Research interest 0-1
//        }
//        request.setIndividualProperties(properties);
//
//        // Two simple evaluation functions
//        request.setEvaluateFunctions(new String[]{
//                "P1 * W1",  // First property weighted importance
//                "P2 * W2"   // Second property weighted importance
//        });
//
//        // Fitness function that combines both evaluation functions
//        request.setFitnessFunction("(P1 * W1) + (P2 * W2)");
//
//        // Some excluded pairs (incompatible matches)
//        request.setExcludedPairs(new int[][]{
//                {0, 500},  // University 0 doesn't want student 500
//                {1, 700},  // University 1 doesn't want student 700
//                {2, 900}   // University 2 doesn't want student 900
//        });
//
//        // Genetic algorithm parameters
//        request.setPopulationSize(200);
//        request.setGeneration(50);
//        request.setMaxTime(3600);  // 1 hour
//
//        request.setAlgorithm("NSGAII");
//        request.setDistributedCores("4");
//
//        final ResponseEntity<Response> expectedResult = new ResponseEntity<>(Response.builder()
//                .status(0)
//                .message("Stable Matching Solution Found")
//                .data("Optimized University-Student Matching")
//                .build(), HttpStatus.OK);
//
//        // Run the test
//        final ResponseEntity<Response> result = otmStableMatchingSolverUnderTest.solve(request);
//
//        // Verify the results
//        assertThat(result).isEqualTo(expectedResult);
//    }
//
//}
