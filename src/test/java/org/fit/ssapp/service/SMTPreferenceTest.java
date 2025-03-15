package org.fit.ssapp.service;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.preference.PreferenceList;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testDefaultPreferenceCalculation() {
        StableMatchingProblemDto dto = genSampleDto();
        PreferenceList preferenceList0 = createPreferenceList(dto, 0, "default");
        PreferenceList preferenceList1 = createPreferenceList(dto, 1, "default");

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        assertTrue(score0to1 > 0);
        assertTrue(score1to0 > 0);
    }

    @Test
    public void testCustomPreferenceCalculation() {
        SampleDataGenerator sampleData = new SampleDataGenerator(MatchingProblemType.OTO, 2, 2, 3);
        StableMatchingProblemDto dto = sampleData.generateDto();
        dto.setIndividualRequirements(new String[][]{
                {"1--", "2:3", "3++"},
                {"4", "5", "6"},
                {"1", "1", "2"},
                {"1", "1", "2"}
        });
        PreferenceList preferenceList0 = createPreferenceList(dto, 0, "custom");
        PreferenceList preferenceList1 = createPreferenceList(dto, 1, "custom");

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        assertTrue(score0to1 > 0);
        assertTrue(score1to0 > 0);
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