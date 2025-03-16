package org.fit.ssapp.service;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SMTPreferenceTest {
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


    @ParameterizedTest
    @CsvSource({
            "1, 2, 3, 4, 5, 6, 1, 2, 3, 36.0, 77.0", // req1, req2, req3, prop1, prop2, prop3, weight1, weight2, weight3, expectedScore0to1, expectedScore1to0
            "4, 5, 6, 7, 8, 9, 4, 5, 6, 174.0, 122.0",
            "1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 1, 2, 3, 43.0, 84.5"
    })
    public void testDefaultPreferenceCalculation(
            double req1, double req2, double req3,
            double prop1, double prop2, double prop3,
            double weight1, double weight2, double weight3,
            double expectedScore0to1, double expectedScore1to0) {

        SampleDataGenerator sampleData = new SampleDataGenerator(MatchingProblemType.OTO, 2, 2, 3);
        StableMatchingProblemDto dto = sampleData.generateDto();

        dto.setIndividualRequirements(new String[][]{
                {String.valueOf(req1), String.valueOf(req2), String.valueOf(req3)},
                {"1", "1", "1"},
                {"1", "1", "1"},
                {"1", "1", "1"}
        });
        dto.setIndividualProperties(new double[][]{
                {prop1, prop2, prop3},
                {1, 2, 3},
                {1, 2, 3},
                {1, 2, 3}
        });
        dto.setIndividualWeights(new double[][]{
                {weight1, weight2, weight3},
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

    @ParameterizedTest
    @CsvSource({
            "1--, 2:3, 3++, 4, 5, 6, 1, 2, 3, 38.0, 77.0", // req1, req2, req3, prop1, prop2, prop3, weight1, weight2, weight3, expectedScore0to1, expectedScore1to0
            "4, 5, 6, 7, 8, 9, 4, 5, 6, 174.0, 122.0",
            "1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 1, 2, 3, 43.0, 84.5"
    })
    public void testCustomPreferenceCalculation(
            String req1, String req2, String req3,
            double prop1, double prop2, double prop3,
            double weight1, double weight2, double weight3,
            double expectedScore0to1, double expectedScore1to0) {

        SampleDataGenerator sampleData = new SampleDataGenerator(MatchingProblemType.OTO, 2, 2, 3);
        StableMatchingProblemDto dto = sampleData.generateDto();

        dto.setIndividualRequirements(new String[][]{
                {req1, req2, req3},
                {"1", "1", "1"},
                {"1", "1", "1"},
                {"1", "1", "1"}
        });
        dto.setIndividualProperties(new double[][]{
                {prop1, prop2, prop3},
                {1, 2, 3},
                {1, 2, 3},
                {1, 2, 3}
        });
        dto.setIndividualWeights(new double[][]{
                {weight1, weight2, weight3},
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