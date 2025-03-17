package org.fit.ssapp.service;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
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
     * @param requirements      List of requirements.
     * @param properties        List of properties.
     * @param weights           List of weights.
     * @param expectedScore0to1 Expected score from individual 0 to 1.
     * @param expectedScore1to0 Expected score from individual 1 to 0.
     */
    @ParameterizedTest
    @MethodSource("defaultPreferenceTestCases")
    public void testDefaultPreferenceCalculation(
            List<Double> requirements,
            List<Double> properties,
            List<Double> weights,
            double expectedScore0to1,
            double expectedScore1to0) {

        SampleDataGenerator sampleData = new SampleDataGenerator(MatchingProblemType.OTO, 2, 2, 3);
        StableMatchingProblemDto dto = sampleData.generateDto();

        dto.setIndividualRequirements(new String[][]{
                requirements.stream().map(String::valueOf).toArray(String[]::new),
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
     * @param requirements      List of requirements as strings.
     * @param properties        List of properties.
     * @param weights           List of weights.
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
            double expectedScore1to0) {

        SampleDataGenerator sampleData = new SampleDataGenerator(MatchingProblemType.OTO, 2, 2, 3);
        StableMatchingProblemDto dto = sampleData.generateDto();

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

        PreferenceList preferenceList0 = createPreferenceList(dto, 0, "custom");
        PreferenceList preferenceList1 = createPreferenceList(dto, 1, "custom");

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        Assertions.assertEquals(expectedScore0to1, score0to1, 0.001);
        Assertions.assertEquals(expectedScore1to0, score1to0, 0.001);
    }

    private static Stream<Arguments> defaultPreferenceTestCases() {
        return Stream.of(
                Arguments.of(Arrays.asList(1.0, 2.0, 3.0), Arrays.asList(4.0, 5.0, 6.0), Arrays.asList(1.0, 2.0, 3.0), 36.0, 77.0),
                Arguments.of(Arrays.asList(4.0, 5.0, 6.0), Arrays.asList(7.0, 8.0, 9.0), Arrays.asList(4.0, 5.0, 6.0), 174.0, 122.0),
                Arguments.of(Arrays.asList(1.5, 2.5, 3.5), Arrays.asList(4.5, 5.5, 6.5), Arrays.asList(1.0, 2.0, 3.0), 43.0, 84.5)
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