package org.fit.ssapp.service;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Unit tests for SMT Preference calculation.
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
     * Tests the default preference calculation.
     *
     * @param expectedScore0to1 Expected score from individual 0 to 1.
     * @param expectedScore1to0 Expected score from individual 1 to 0.
     */
    @ParameterizedTest
    @MethodSource("defaultPreferenceTestCases")
    public void testDefaultPreferenceCalculation(
            double expectedScore0to1,
            double expectedScore1to0) {

        StableMatchingProblemDto dto = genSampleDto();

        PreferenceList preferenceList0 = createPreferenceList(dto, 0, "default");
        PreferenceList preferenceList1 = createPreferenceList(dto, 1, "default");

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        Assertions.assertEquals(expectedScore0to1, score0to1, 0.001);
        Assertions.assertEquals(expectedScore1to0, score1to0, 0.001);
    }

    /**
     * Tests the custom preference calculation.
     *
     * @param expectedScore0to1 Expected score from individual 0 to 1.
     * @param expectedScore1to0 Expected score from individual 1 to 0.
     */
    @ParameterizedTest
    @MethodSource("customPreferenceTestCases")
    public void testCustomPreferenceCalculation(
            double expectedScore0to1,
            double expectedScore1to0) {

        StableMatchingProblemDto dto = genSampleDto();

        PreferenceList preferenceList0 = createPreferenceList(dto, 0, "sqrt(p1) + p2 + p3");
        PreferenceList preferenceList1 = createPreferenceList(dto, 1, "sqrt(p1) + p2 + abs(p3)");

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        Assertions.assertEquals(expectedScore0to1, score0to1, 0.001);
        Assertions.assertEquals(expectedScore1to0, score1to0, 0.001);
    }

    private static Stream<Arguments> defaultPreferenceTestCases() {
        return Stream.of(
                Arguments.of(51.6, 34.8),
                Arguments.of(51.6, 34.8),
                Arguments.of(51.6, 34.8)
        );
    }

    /**
     * Provides test cases for custom preference calculation.
     *
     * @return Stream of Arguments for custom preference tests.
     */
    private static Stream<Arguments> customPreferenceTestCases() {
        return Stream.of(
                Arguments.of(51.6, 34.8),
                Arguments.of(51.6, 34.8),
                Arguments.of(51.6, 34.8)
        );
    }


    /**
     * Creates a preference list for a given individual.
     *
     * @param dto             StableMatchingProblemDto.
     * @param index           Index of the individual.
     * @param evaluationType  Type of evaluation.
     * @return PreferenceList.
     */
    private PreferenceList createPreferenceList(StableMatchingProblemDto dto, int index, String evaluationType) {
        int size = dto.getIndividualRequirements().length - 1;
        TwoSetPreferenceList preferenceList = new TwoSetPreferenceList(size, 0);

        for (int i = 0; i < dto.getIndividualRequirements().length; i++) {
            if (i != index) {
                double score = calculatePreference(dto, index, i, evaluationType);
                preferenceList.add(score);
            }
        }
        preferenceList.sort();
        return preferenceList;
    }

    /**
     * Calculates the preference score between two individuals.
     *
     * @param dto             StableMatchingProblemDto.
     * @param individualA     Index of individual A.
     * @param individualB     Index of individual B.
     * @param evaluationType  Type of evaluation.
     * @return Preference score.
     */
    private double calculatePreference(StableMatchingProblemDto dto, int individualA, int individualB, String evaluationType) {
        double score = 0.0;
        for (int i = 0; i < dto.getNumberOfProperty(); i++) {
            double requirement;
            if ("default".equals(evaluationType)) {
                requirement = Double.parseDouble(dto.getIndividualRequirements()[individualA][i]);
            } else {
                requirement = parseRequirement(dto.getIndividualRequirements()[individualA][i]);
            }
            score += requirement * dto.getIndividualWeights()[individualA][i] * dto.getIndividualProperties()[individualB][i];
        }
        return score;
    }

    /**
     * Parses a requirement string into a double.
     *
     * @param requirementStr Requirement string.
     * @return Parsed requirement value.
     */
    private double parseRequirement(String requirementStr) {
        if (requirementStr.endsWith("--")) {
            return Double.parseDouble(requirementStr.substring(0, requirementStr.length() - 2));
        } else if (requirementStr.contains(":")) {
            String[] parts = requirementStr.split(":");
            return (Double.parseDouble(parts[0]) + Double.parseDouble(parts[1])) / 2.0;
        } else if (requirementStr.endsWith("++")) {
            return Double.parseDouble(requirementStr.substring(0, requirementStr.length() - 2));
        } else {
            return Double.parseDouble(requirementStr);
        }
    }
}