package org.fit.ssapp.service;

import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.provider.TwoSetPreferenceProvider;
import org.fit.ssapp.ss.smt.requirement.Requirement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
        dto.setIndividualSetIndices(new int[]{1, 1, 0});
        dto.setIndividualCapacities(new int[]{1, 2, 1});
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
    @ParameterizedTest
    @MethodSource("defaultPreferenceTestCases")
    public void testDefaultPreferenceCalculation(
            List<String> requirements,
            List<Double> properties,
            List<Double> weights,
            double expectedScore0to1,
            double expectedScore1to0
    ) {

        StableMatchingProblemDto dto = genSampleDto();
        dto.setIndividualRequirements(new String[][]{
                requirements.toArray(new String[0]),
                {"1", "1", "1"},
                {"1", "1", "1"},
                {"1", "1", "1"}
        });
        dto.setIndividualProperties(new double[][]{
                properties.stream().mapToDouble(Double::doubleValue).toArray(),
                {1, 2, 3},
                {1, 2, 3},
                {1, 2, 3}
        });
        dto.setIndividualWeights(new double[][]{
                weights.stream().mapToDouble(Double::doubleValue).toArray(),
                {4, 5, 6},
                {1, 2, 3},
                {1, 2, 3}
        });

        MatchingData matchingData = StableMatchingProblemMapper.toMTM(dto).getMatchingData();
        TwoSetPreferenceProvider provider = new TwoSetPreferenceProvider(matchingData, new String[]{"default", "default"});

        PreferenceList preferenceList0 = provider.getPreferenceListByFunction(0);
        PreferenceList preferenceList1 = provider.getPreferenceListByFunction(1);

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        Assertions.assertEquals(expectedScore0to1, score0to1, 0.001);
        Assertions.assertEquals(expectedScore1to0, score1to0, 0.001);
    }

    /**
     * Tests the custom preference calculation using TwoSetPreferenceProvider.
     *
     * @param expectedScore0to1 Expected score from individual 0 to 1.
     * @param expectedScore1to0 Expected score from individual 1 to 0.
     */
    @ParameterizedTest
    @MethodSource("customPreferenceTestCases")
    public void testCustomPreferenceCalculation(
            List<String> requirements,
            List<Double> properties,
            List<Double> weights,
            double expectedScore0to1,
            double expectedScore1to0
    ) {

        StableMatchingProblemDto dto = genSampleDto();
        dto.setIndividualRequirements(new String[][]{
                requirements.toArray(new String[0]),
                {"1", "1", "1"},
                {"1", "1", "1"},
                {"1", "1", "1"}
        });
        dto.setIndividualProperties(new double[][]{
                properties.stream().mapToDouble(Double::doubleValue).toArray(),
                {1, 2, 3},
                {1, 2, 3},
                {1, 2, 3}
        });
        dto.setIndividualWeights(new double[][]{
                weights.stream().mapToDouble(Double::doubleValue).toArray(),
                {4, 5, 6},
                {1, 2, 3},
                {1, 2, 3}
        });

        MatchingData matchingData = StableMatchingProblemMapper.toMTM(dto).getMatchingData();
        TwoSetPreferenceProvider provider = new TwoSetPreferenceProvider(matchingData, new String[]{"sqrt(R1) + R2 + R3", "sqrt(R1) + R2 + abs(R3)"});

        PreferenceList preferenceList0 = provider.getPreferenceListByFunction(0);
        PreferenceList preferenceList1 = provider.getPreferenceListByFunction(1);

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        Assertions.assertEquals(expectedScore0to1, score0to1, 0.001);
        Assertions.assertEquals(expectedScore1to0, score1to0, 0.001);
    }


    private static Stream<Arguments> defaultPreferenceTestCases() {
        return Stream.of(
                Arguments.of(Arrays.asList("1.0", "2.0", "3.0"), Arrays.asList(4.0, 5.0, 6.0), Arrays.asList(1.0, 2.0, 3.0), 36.0, 77.0),
                Arguments.of(Arrays.asList("4.0", "5.0", "6.0"), Arrays.asList(7.0, 8.0, 9.0), Arrays.asList(4.0, 5.0, 6.0), 174.0, 122.0),
                Arguments.of(Arrays.asList("1.5", "2.5", "3.5"), Arrays.asList(4.5, 5.5, 6.5), Arrays.asList(1.0, 2.0, 3.0), 43.0, 84.5)
        );
    }

    /**
     * Provides test cases for custom preference calculation.
     *
     * @return Stream of Arguments for custom preference tests.
     */
    private static Stream<Arguments> customPreferenceTestCases() {
        return Stream.of(
                Arguments.of(Arrays.asList("1--", "2:3", "3++"), Arrays.asList(4.0, 5.0, 6.0), Arrays.asList(1.0, 2.0, 3.0), 38.0, 77.0),
                Arguments.of(Arrays.asList("4", "5", "6"), Arrays.asList(7.0, 8.0, 9.0), Arrays.asList(4.0, 5.0, 6.0), 174.0, 122.0),
                Arguments.of(Arrays.asList("1.5", "2.5", "3.5"), Arrays.asList(4.5, 5.5, 6.5), Arrays.asList(1.0, 2.0, 3.0), 43.0, 84.5)
        );
    }
}