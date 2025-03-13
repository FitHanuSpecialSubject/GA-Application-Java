package org.fit.ssapp.service;
import org.fit.ssapp.dto.mapper.StableMatchingProblemMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.preference.*;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

public class SMTPreferenceTest {

    @Test
    public void testDefaultPreferenceList() {
        StableMatchingProblemDto dto = createSampleDto();
        PreferenceList preferenceList0 = createDefaultPreferenceList(dto, 0);
        PreferenceList preferenceList1 = createDefaultPreferenceList(dto, 1);

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        assertEquals(10.0 + 22.0 + 36.0, score0to1);
        assertEquals(40.0 + 70.0 + 90.0, score1to0);
    }

    @Test
    public void testCustomPreferenceList() {
        StableMatchingProblemDto dto = createCustomSampleDto();
        PreferenceList preferenceList0 = createCustomPreferenceList(dto, 0);
        PreferenceList preferenceList1 = createCustomPreferenceList(dto, 1);

        double score0to1 = preferenceList0.getScore(1);
        double score1to0 = preferenceList1.getScore(0);

        assertEquals(10.0 + 23.0 + 36.0, score0to1);
        assertEquals(40.0 + 70.0 + 90.0, score1to0);
    }

    private PreferenceList createDefaultPreferenceList(StableMatchingProblemDto dto, int index) {
        int size = dto.getIndividualRequirements().length - 1; // Assuming 2 individuals
        TwoSetPreferenceList preferenceList = new TwoSetPreferenceList(size, 0); // Padding is 0 for simplicity

        for (int i = 0; i < dto.getIndividualRequirements().length; i++) {
            if (i != index) {
                double score = calculateDefaultPreference(dto, index, i);
                preferenceList.add(score);
            }
        }
        preferenceList.sort();
        return preferenceList;
    }

    private PreferenceList createCustomPreferenceList(StableMatchingProblemDto dto, int index) {
        int size = dto.getIndividualRequirements().length - 1;
        TwoSetPreferenceList preferenceList = new TwoSetPreferenceList(size, 0);

        for (int i = 0; i < dto.getIndividualRequirements().length; i++) {
            if (i != index) {
                double score = calculateCustomPreference(dto, index, i);
                preferenceList.add(score);
            }
        }
        preferenceList.sort();
        return preferenceList;
    }

    private double calculateDefaultPreference(StableMatchingProblemDto dto, int individualA, int individualB) {
        double score = 0.0;
        for (int i = 0; i < dto.getNumberOfProperty(); i++) {
            double requirement = Double.parseDouble(dto.getIndividualRequirements()[individualA][i]);
            score += requirement * dto.getIndividualWeights()[individualA][i] * dto.getIndividualProperties()[individualB][i];
        }
        return score;
    }

    private double calculateCustomPreference(StableMatchingProblemDto dto, int individualA, int individualB) {
        double score = 0.0;
        for (int i = 0; i < dto.getNumberOfProperty(); i++) {
            String requirementStr = dto.getIndividualRequirements()[individualA][i];
            double requirement = parseRequirement(requirementStr);
            score += requirement * dto.getIndividualWeights()[individualA][i];
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

    private StableMatchingProblemDto createSampleDto() {
        StableMatchingProblemDto dto = new StableMatchingProblemDto();
        dto.setNumberOfProperty(3);
        dto.setIndividualRequirements(new String[][]{
                {"1", "2", "3"},
                {"4", "5", "6"}
        });
        dto.setIndividualWeights(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });
        dto.setIndividualProperties(new double[][]{
                {10.0, 11.0, 12.0},
                {13.0, 14.0, 15.0}
        });
        return dto;
    }

    private StableMatchingProblemDto createCustomSampleDto() {
        StableMatchingProblemDto dto = new StableMatchingProblemDto();
        dto.setNumberOfProperty(3);
        dto.setIndividualRequirements(new String[][]{
                {"1--", "2:3", "3++"},
                {"4", "5", "6"}
        });
        dto.setIndividualWeights(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });
        dto.setIndividualProperties(new double[][]{
                {10.0, 11.0, 12.0},
                {13.0, 14.0, 15.0}
        });
        return dto;
    }

    @Test
    public void testPreferenceListWrapper() {
        StableMatchingProblemDto dto = createSampleDto();
        List<PreferenceList> lists = new ArrayList<>();
        lists.add(createDefaultPreferenceList(dto, 0));
        lists.add(createDefaultPreferenceList(dto, 1));
        PreferenceListWrapper wrapper = new PreferenceListWrapper(lists);

        Set<Integer> setOfPreferNode = new TreeSet<>();
        setOfPreferNode.add(1);
        int weakestNode = wrapper.getLeastScoreNode(1, 0, 2, setOfPreferNode, 1);

        assertEquals(1, weakestNode);

        boolean isPreferred = wrapper.isPreferredOver(1, 0, 0);
        assertEquals(true, isPreferred);

        Matches matches = createMatches();
        MatchingData matchingData = StableMatchingProblemMapper.toOTO(dto).getMatchingData();

        double[] satisfactions = wrapper.getMatchesSatisfactions(matches, matchingData);
        assertEquals(10.0 + 22.0 + 36.0, satisfactions[0]);
        assertEquals(40.0 + 70.0 + 90.0, satisfactions[1]);

        int lastChoice = wrapper.getLastChoiceOf(1, 0);
        assertEquals(1, lastChoice);
    }

    private Matches createMatches() {
        Matches matches = new Matches(2);
        matches.addMatch(0, 1);
        matches.addMatch(1, 0);
        return matches;
    }

    @ParameterizedTest
    @CsvSource({
            "10.0, 22.0, 36.0, 40.0, 70.0, 90.0", // Default scores
            "10.0, 23.0, 36.0, 40.0, 70.0, 90.0"  // Custom scores
    })
    public void testPreferenceList(double score0to1, double score1to0, double customScore0to1, double customScore1to0) {
        // Test Default Preferences
        StableMatchingProblemDto defaultDto = createSampleDto();
        PreferenceList defaultList0 = createDefaultPreferenceList(defaultDto, 0);
        PreferenceList defaultList1 = createDefaultPreferenceList(defaultDto, 1);

        assertEquals(score0to1, defaultList0.getScore(1));
        assertEquals(score1to0, defaultList1.getScore(0));

        // Test Custom Preferences
        StableMatchingProblemDto customDto = createCustomSampleDto();
        PreferenceList customList0 = createCustomPreferenceList(customDto, 0);
        PreferenceList customList1 = createCustomPreferenceList(customDto, 1);

        assertEquals(customScore0to1, customList0.getScore(1));
        assertEquals(customScore1to0, customList1.getScore(0));
    }

    @ParameterizedTest
    @CsvSource({
            "1, true, 10.0, 22.0, 36.0, 40.0, 70.0, 90.0, 1" // weakestNode, isPreferred, satisfactions0, satisfactions1, lastChoice
    })
    public void testPreferenceListWrapper(int weakestNode, boolean isPreferred, double satisfactions0, double satisfactions1, int lastChoice) {
        StableMatchingProblemDto dto = createSampleDto();
        List<PreferenceList> lists = new ArrayList<>();
        lists.add(createDefaultPreferenceList(dto, 0));
        lists.add(createDefaultPreferenceList(dto, 1));
        PreferenceListWrapper wrapper = new PreferenceListWrapper(lists);

        Set<Integer> setOfPreferNode = new TreeSet<>();
        setOfPreferNode.add(1);
        int actualWeakestNode = wrapper.getLeastScoreNode(1, 0, 2, setOfPreferNode, 1);
        assertEquals(weakestNode, actualWeakestNode);

        boolean actualIsPreferred = wrapper.isPreferredOver(1, 0, 0);
        assertEquals(isPreferred, actualIsPreferred);

        Matches matches = createMatches();
        MatchingData matchingData = StableMatchingProblemMapper.toOTO(dto).getMatchingData();

        double[] actualSatisfactions = wrapper.getMatchesSatisfactions(matches, matchingData);
        assertEquals(satisfactions0, actualSatisfactions[0]);
        assertEquals(satisfactions1, actualSatisfactions[1]);

        int actualLastChoice = wrapper.getLastChoiceOf(1, 0);
        assertEquals(lastChoice, actualLastChoice);
    }
}