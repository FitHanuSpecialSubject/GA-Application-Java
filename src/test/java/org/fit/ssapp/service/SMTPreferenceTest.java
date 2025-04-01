package org.fit.ssapp.service;

import java.util.stream.Stream;

import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.provider.TwoSetPreferenceProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for SMT Preference calculation using TwoSetPreferenceProvider.
 */
public class SMTPreferenceTest {

    /**
     * Generates a sample StableMatchingProblemDto for testing.
     *
     * @return A sample StableMatchingProblemDto.
     */
    public StableMatchingProblemDto genSampleDto() {
        StableMatchingProblemDto dto = new StableMatchingProblemDto();
        dto.setProblemName("Stable Matching Problem");
        dto.setNumberOfSets(2);
        dto.setNumberOfProperty(3);
        dto.setNumberOfIndividuals(3);
        dto.setIndividualSetIndices(new int[]{0, 1, 0});
        dto.setIndividualCapacities(new int[]{1, 1, 1});
        dto.setIndividualRequirements(new String[][]{
            {"1", "1.1", "1"},
            {"1", "1.1", "1.1"},
            {"1", "1", "2"}
        });
        dto.setIndividualWeights(new double[][]{
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
        });
        dto.setIndividualProperties(new double[][]{
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
        });
        dto.setEvaluateFunctions(new String[]{
            "default",
            "default"
        });
        dto.setFitnessFunction("default");
        dto.setPopulationSize(500);
        dto.setGeneration(50);
        dto.setMaxTime(3600);
        dto.setAlgorithm("NSGAII");
        dto.setDistributedCores("4");

        return dto;
    }

    /**
     * Tests the default preference calculation using TwoSetPreferenceProvider.
     *
     * @param expectedScore0to1 Expected score from individual 0 to 1.
     * @param expectedScore1to0 Expected score from individual 1 to 0.
     */
    // @ParameterizedTest
    @MethodSource("defaultPreferenceTestCases")
    public void testDefaultPreferenceCalculation(
            String[] requirements,
            double[] properties,
            double[] weights,
            double expected
    ) {

        StableMatchingProblemDto dto = genSampleDto();
        dto.setIndividualRequirements(new String[][]{
            requirements,
            requirements,
            requirements,
        });
        dto.setIndividualProperties(new double[][]{
            properties,
            properties,
            properties,
        });
        dto.setIndividualWeights(new double[][]{
            weights,
            weights,
            weights
        });

        MatchingData matchingData = StableMatchingProblemMapper
          .toMTM(dto)
          .getMatchingData();
        TwoSetPreferenceProvider provider = new TwoSetPreferenceProvider(matchingData, new String[]{"", ""});

        PreferenceList preferenceList0 = provider.getPreferenceListByFunction(0);
        PreferenceList preferenceList1 = provider.getPreferenceListByFunction(1);

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        Assertions.assertEquals(expected, score0to1, 0.001);
        Assertions.assertEquals(expected, score1to0, 0.001);
    }

    /**
     * Tests the custom preference calculation using TwoSetPreferenceProvider.
     *
     * @param expectedScore0to1 Expected score from individual 0 to 1.
     * @param expected1 Expected score from individual 1 to 0.
     * @param expected2 Expected score from individual 0 to 1.
     */
    // @ParameterizedTest
    @MethodSource("customPreferenceTestCases")
    public void testCustomPreferenceCalculation(
      String function,
            String[] requirements,
            double[] properties,
            double[] weights,
            double expected1,
            double expected2
    ) {

        StableMatchingProblemDto dto = genSampleDto();
        dto.setIndividualRequirements(new String[][]{
            requirements,
            requirements,
            requirements,
        });
        dto.setIndividualProperties(new double[][]{
            properties,
            properties,
            properties,
        });
        dto.setIndividualWeights(new double[][]{
            weights,
            weights,
            weights,
        });

        MatchingData matchingData = StableMatchingProblemMapper.toMTM(dto).getMatchingData();
        TwoSetPreferenceProvider provider = new TwoSetPreferenceProvider(
          matchingData,
          new String[]{function, function}
        );

        PreferenceList preferenceList0 = provider.getPreferenceListByFunction(0);
        PreferenceList preferenceList1 = provider.getPreferenceListByFunction(1);

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        Assertions.assertEquals(expected1, score0to1, 0.001);
        Assertions.assertEquals(expected2, score1to0, 0.001);
    }

    private static Stream<Arguments> defaultPreferenceTestCases() {
        return Stream.of(
                Arguments.of(
                        new String[]{"1--", "2:3", "3++"},
                        new double[]{4.0, 5.0, 6.0}, // property
                        new double[]{1.0, 2.0, 3.0}, // weight
                        6
                ),
                Arguments.of(
                        new String[]{"4", "5", "6"},
                        new double[]{7.0, 8.0, 9.0}, // property
                        new double[]{4.0, 5.0, 6.0}, // weight
                        25.5
                ),
                
                Arguments.of(
                        new String[]{"1:3", "5:10", "100:200"},
                        new double[]{100, 100, 150.0}, // property
                        new double[]{4.0, 5.0, 6.0}, // weight
                        12
                ),
                Arguments.of(
                        new String[]{"4:5", "5:7", "1:6"},
                        new double[]{7.0, 8.0, 9.0}, // property
                        new double[]{4.0, 5.0, 6.0}, // weight
                        0
                ),
                Arguments.of(
                        new String[]{"1.5", "2.5", "3.5"},
                        new double[]{11, 20, 30}, // property
                        new double[]{1.0, 2.0, 3.0}, // weight
                        0
                )
        );
    }

    /**
     * Provides test cases for custom preference calculation.
     *
     * @return Stream of Arguments for custom preference tests.
     */
    private static Stream<Arguments> customPreferenceTestCases() {
        return Stream.of(
          // ceil
//          Arguments.of(
//            "ceil(R2) + (15 / 2) * W3",
//            new String[] {"1--", "2:3", "3++"},
//            new double[] { 4.0, 5.0, 6.0 }, // property
//            new double[] { 1.0, 2.0, 3.0 }, // weight
//            6, 25.5
//          ),
//
//          // floor
//          Arguments.of(
//            "floor(R2) + 15^2",
//            new String[] {"4", "5", "6"},
//            new double[] {7.0, 8.0, 9.0}, // property
//            new double[] {4.0, 5.0, 6.0}, // weight
//            25.5, 230
//          ),
//
//          // cbrt
//          Arguments.of(
//            "cbrt(3) + R1 * W2 + R3",
//            new String[] {"4", "5", "6"},
//            new double[] {7.0, 8.0, 9.0}, // property
//            new double[] {4.0, 5.0, 6.0}, // weight
//            25.5, 27.442249570307407
//          ),
//
//          // abs
//          Arguments.of(
//            "abs(R1 - R2) + 1",
//            new String[] {"4", "5", "6"},
//            new double[] {7.0, 8.0, 9.0}, // property
//            new double[] {4.0, 5.0, 6.0}, // weight
//            25.5, 2
//          ),
//
//          // sqrt
//          Arguments.of(
//            "sqrt(4) + 17",
//            new String[] {"4", "5", "6"},
//            new double[] {7.0, 8.0, 9.0}, // property
//            new double[] {4.0, 5.0, 6.0}, // weight
//            25.5, 19
//          ),

          // log
          Arguments.of(
            "12^2 + log(R1) * P1 + log2(W2) + P3",
            new String[] {"1.5", "2.5", "3.5"},
            new double[] { 4.5, 5.5, 6.5 }, // property
            new double[] { 1.0, 2.0, 3.0 }, // weight
                  0 , 151.5
          )
  );
    }
}
