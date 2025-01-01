//package com.example.SS2_Backend.ss.smt.preference.impl.provider;
//
//import com.example.SS2_Backend.model.stableMatching.PreferenceList;
//import com.example.SS2_Backend.model.stableMatching.PropertyRequirement;
//import com.example.SS2_Backend.model.stableMatching.Requirement.Requirement;
//import com.example.SS2_Backend.ss.smt.preference.PreferenceBuilder;
//import com.example.SS2_Backend.util.PreferenceProviderUtils;
//import lombok.Data;
//import lombok.experimental.FieldDefaults;
//import net.objecthunter.exp4j.Expression;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
//@Data
// //TODO: implement PreferenceBuilder
//public class NewProvider implements PreferenceBuilder {
//
//    String[][] individualRequirements;
//    double[][] individualWeights;
//    double[][] individualProperties;
//    int[] individualSetIndices;
//    PreferenceProviderUtils preferenceProviderUtils;
//
//    public NewProvider(String[][] individualRequirements,
//                       double[][] individualWeights,
//                       double[][] individualProperties,
//                       int numberOfIndividuals,
//                       int[] individualSetIndices,
//                       PreferenceProviderUtils preferenceProviderUtils) {
//        setNumberOfIndividuals(numberOfIndividuals);
//        setSizeOf1(0);
//        setSizeOf2(numberOfIndividuals - getSizeOf1());
//        this.preferenceProviderUtils = preferenceProviderUtils;
//        this.individualRequirements = individualRequirements;
//        this.individualWeights = individualWeights;
//        this.individualProperties = individualProperties;
//        this.individualSetIndices = individualSetIndices;
//    }
//
//    @Override
//    protected Map<String, Double> getVariableValues(Map<String, Set<Integer>> variables,
//                                                    int idx1,
//                                                    int idx2) {
//        Map<String, Double> variablesValues = new HashMap<>();
//        for (Map.Entry<String, Set<Integer>> entry : variables.entrySet()) {
//            String key = entry.getKey();
//            Set<Integer> values = entry.getValue();
//            switch (key) {
//                case "P":
//                    for (Integer value : values) {
//                        double val = individualProperties[idx2][value - 1];
//                        variablesValues.put(key + value, val);
//                    }
//                    break;
//                case "W":
//                    for (Integer value : values) {
//                        double val = individualWeights[idx1][value - 1];
//                        variablesValues.put(key + value, val);
//                    }
//                    break;
//                case "R":
//                    for (Integer value : values) {
//                        double val = PropertyRequirement
//                                .setRequirement(MatchingHelperFunctions.decodeInputRequirement(
//                                        individualRequirements[idx1][value - 1]))
//                                .getValueForFunction();
//                        variablesValues.put(key + value, val);
//                    }
//                    break;
//                default:
//                    double val = 0d;
//                    variablesValues.put(key, val);
//            }
//        }
//        return variablesValues;
//    }
//
//    @Override
//    protected PreferenceList getPreferenceListByFunction(int index) {
//        int set = individualSetIndices[index];
//        PreferenceList a;
//        Expression e;
//        if (set == 0) {
//            a = new PreferenceList(getSizeOf2(), getSizeOf1());
//            if (getExpressionOfSet1() == null) {
//                return getPreferenceListByDefault(index);
//            }
//            e = getExpressionOfSet1();
//            for (int i = getSizeOf1(); i < getNumberOfIndividuals(); i++) {
//                e.setVariables(getVariableValuesForSet1(index, i));
//                double totalScore = e.evaluate();
//                a.add(totalScore);
//            }
//        } else {
//            a = new PreferenceList(getSizeOf1(), 0);
//            if (getExpressionOfSet2() == null) {
//                return getPreferenceListByDefault(index);
//            }
//            e = getExpressionOfSet2();
//            for (int i = 0; i < getSizeOf1(); i++) {
//                e.setVariables(getVariableValuesForSet2(index, i));
//                double totalScore = e.evaluate();
//                a.add(totalScore);
//            }
//        }
//        a.sort();
//        return a;
//    }
//
//    @Override
//    protected PreferenceList getPreferenceListByDefault(int index) {
//        int set = individualSetIndices[index];
//        int numberOfProperties = individualProperties.length;
//        PreferenceList a;
//        if (set == 0) {
//            a = new PreferenceList(getSizeOf2(), getSizeOf1());
//            for (int i = getSizeOf1(); i < getNumberOfIndividuals(); i++) {
//                double totalScore = 0;
//                for (int j = 0; j < numberOfProperties; j++) {
//                    double PropertyValue = individualProperties[i][j];
//                    Requirement requirement = PropertyRequirement.setRequirement(
//                            MatchingHelperFunctions.decodeInputRequirement(individualRequirements[index][j]));
//                    double PropertyWeight = individualWeights[i][j];
//                    totalScore +=
//                            preferenceProviderUtils.getDefaultScaling(requirement, PropertyValue) *
//                                    PropertyWeight;
//                }
//                a.add(totalScore);
//            }
//        } else {
//            a = new PreferenceList(getSizeOf1(), 0);
//            for (int i = 0; i < getSizeOf1(); i++) {
//                double totalScore = 0;
//                for (int j = 0; j < numberOfProperties; j++) {
//                    double PropertyValue = individualProperties[i][j];
//                    Requirement requirement = PropertyRequirement.setRequirement(
//                            MatchingHelperFunctions.decodeInputRequirement(individualRequirements[index][j]));
//                    double PropertyWeight = individualWeights[i][j];
//                    totalScore +=
//                            preferenceProviderUtils.getDefaultScaling(requirement, PropertyValue) *
//                                    PropertyWeight;
//                }
//                // Add
//                a.add(totalScore);
//            }
//        }
//        a.sort();
//        return a;
//    }
//
//}
