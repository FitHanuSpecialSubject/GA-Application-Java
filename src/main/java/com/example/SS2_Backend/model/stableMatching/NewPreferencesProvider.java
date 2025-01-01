package com.example.SS2_Backend.model.stableMatching;

import com.example.SS2_Backend.model.stableMatching.Requirement.Requirement;
import com.example.SS2_Backend.model.stableMatching.Requirement.RequirementDecoder;
import lombok.Getter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;

public class NewPreferencesProvider {
    private final String[][] individualRequirements;
    private final double[][] individualWeights;
    private final double[][] individualProperties;
    private final int[] individualSetIndices;
    private final int numberOfIndividuals;
    private final int sizeOf1;
    private final int sizeOf2;
    public int numberOfIndividualForSet0;
    @Getter
    private Expression expressionOfSet1;
    @Getter
    private Expression expressionOfSet2;
    private Map<String, Set<Integer>> variablesOfSet1;
    private Map<String, Set<Integer>> variablesOfSet2;

    public NewPreferencesProvider(
            String[][] individualRequirements,
            double[][] individualWeights,
            double[][] individualProperties,
            int numberOfIndividuals,
            int[] individualSetIndices
    ) {
        this.individualRequirements = individualRequirements;
        this.individualWeights = individualWeights;
        this.individualProperties = individualProperties;
        this.individualSetIndices = individualSetIndices;
        this.numberOfIndividuals = numberOfIndividuals;

        this.sizeOf1 = 0;
        this.sizeOf2 = numberOfIndividuals - sizeOf1;
    }


    public void setEvaluateFunctionForSet1(String EvaluateFunction1) {
        if (expressionOfSet1 != null) return;
        this.variablesOfSet1 = filterVariable(EvaluateFunction1);
        this.expressionOfSet1 = new ExpressionBuilder(EvaluateFunction1)
                .variables(convertMapToSet(variablesOfSet1))
                .build();
    }



    public void setEvaluateFunctionForSet2(String EvaluateFunction2) {
        if (expressionOfSet2 != null) return;
        this.variablesOfSet2 = filterVariable(EvaluateFunction2);
        this.expressionOfSet2 = new ExpressionBuilder(EvaluateFunction2)
                .variables(convertMapToSet(variablesOfSet2))
                .build();
    }

    public Set<String> convertMapToSet(Map<String, Set<Integer>> varMap) {
        Set<String> resultSet = new HashSet<>();
        for (Map.Entry<String, Set<Integer>> entry : varMap.entrySet()) {
            String variable = entry.getKey();
            for (Integer value : entry.getValue()) {
                resultSet.add(variable + value.toString());
            }
        }
        return resultSet;
    }

    public Map<String, Set<Integer>> filterVariable(String evaluateFunction) {
        Map<String, Set<Integer>> variables = new HashMap<>();
        for (int c = 0; c < evaluateFunction.length(); c++) {
            char ch = evaluateFunction.charAt(c);
            switch (ch) {
                case 'P':
                case 'W':
                case 'R':
                    String prefix = String.valueOf(ch);
                    Optional<Integer> nextIdx = getNextIndexToken(evaluateFunction, c);
                    if (nextIdx.isPresent()) {
                        int idx = nextIdx.get();
                        variables.compute(prefix, (key, value) -> {
                            if (value == null) {
                                Set<Integer> set = new HashSet<>();
                                set.add(idx);
                                return set;
                            } else {
                                value.add(idx);
                                return value;
                            }
                        });
                    } else {
                        throw new IllegalArgumentException("Invalid expression after: " + prefix);
                    }
            }
        }
        return variables;
    }

    public Optional<Integer> getNextIndexToken(String evaluateFunction, int currentIndex) {
        int nextIndex = currentIndex + 1;
        while (nextIndex < evaluateFunction.length() &&
                Character.isDigit(evaluateFunction.charAt(nextIndex))) {
            nextIndex++;
        }
        if (nextIndex == currentIndex + 1) {
            return Optional.empty();
        }
        String subString = evaluateFunction.substring(currentIndex + 1, nextIndex);
        int idx = Integer.parseInt(subString);
        return Optional.of(idx);
    }

    public Map<String, Double> getVariableValuesForSet1(int indexOfEvaluator,
                                                        int indexOfBeEvaluated) {
        return getVariableValues(this.variablesOfSet1, indexOfEvaluator, indexOfBeEvaluated);
    }

    public Map<String, Double> getVariableValuesForSet2(int indexOfEvaluator,
                                                        int indexOfBeEvaluated) {
        return getVariableValues(this.variablesOfSet2, indexOfEvaluator, indexOfBeEvaluated);
    }

    private Map<String, Double> getVariableValues(Map<String, Set<Integer>> variables,
                                                  int idx1,
                                                  int idx2) {
        Map<String, Double> variablesValues = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : variables.entrySet()) {
            String key = entry.getKey();
            Set<Integer> values = entry.getValue();
            switch (key) {
                case "P":
                    for (Integer value : values) {
                        double val = individualProperties[idx2][value - 1];
                        variablesValues.put(key + value, val);
                    }
                    break;
                case "W":
                    for (Integer value : values) {
                        double val = individualWeights[idx1][value - 1];
                        variablesValues.put(key + value, val);
                    }
                    break;
                case "R":
                    for (Integer value : values) {
                        double val = PropertyRequirement.setRequirement(
                                RequirementDecoder.decodeInputRequirement(individualRequirements[idx1][value - 1])
                        ).getValueForFunction();
                        variablesValues.put(key + value, val);
                    }
                    break;
                default:
                    double val = 0d;
                    variablesValues.put(key, val);
            }
        }
        return variablesValues;
    }

    public PreferenceList getPreferenceListByFunction(int index) {
        int set = individualSetIndices[index];
        PreferenceList a;
        Expression e;
        if (set == 0) {
            a = new PreferenceList(this.sizeOf2, this.sizeOf1);
            if (this.expressionOfSet1 == null) {
                return this.getPreferenceListByDefault(index);
            }
            e = this.expressionOfSet1;
            for (int i = this.sizeOf1; i < numberOfIndividuals; i++) {
                e.setVariables(this.getVariableValuesForSet1(index, i));
                double totalScore = e.evaluate();
                a.add(totalScore);
            }
        } else {
            a = new PreferenceList(this.sizeOf1, 0);
            if (this.expressionOfSet2 == null) {
                return this.getPreferenceListByDefault(index);
            }
            e = this.expressionOfSet2;
            for (int i = 0; i < sizeOf1; i++) {
                e.setVariables(this.getVariableValuesForSet2(index, i));
                double totalScore = e.evaluate();
                a.add(totalScore);
            }
        }
        a.sort();
        return a;
    }

    public PreferenceList getPreferenceListByDefault(int index) {
        int set = individualSetIndices[index];
        int numberOfProperties = individualProperties.length;
        PreferenceList a;
        if (set == 0) {
            a = new PreferenceList(this.sizeOf2, this.sizeOf1);
            for (int i = sizeOf1; i < numberOfIndividuals; i++) {
                double totalScore = 0;
                for (int j = 0; j < numberOfProperties; j++) {
                    double PropertyValue = individualProperties[i][j];
                    Requirement requirement = PropertyRequirement.setRequirement(
                            RequirementDecoder.decodeInputRequirement(individualRequirements[index][j])
                    );
                    double PropertyWeight = individualWeights[i][j];
                    totalScore += getDefaultScaling(requirement, PropertyValue) * PropertyWeight;
                }
                a.add(totalScore);
            }
        } else {
            a = new PreferenceList(this.sizeOf1, 0);
            for (int i = 0; i < sizeOf1; i++) {
                double totalScore = 0;
                for (int j = 0; j < numberOfProperties; j++) {
                    double PropertyValue = individualProperties[i][j];
                    Requirement requirement = PropertyRequirement.setRequirement(
                            RequirementDecoder.decodeInputRequirement(individualRequirements[index][j])
                    );
                    double PropertyWeight = individualWeights[i][j];
                    totalScore += getDefaultScaling(requirement, PropertyValue) * PropertyWeight;
                }
                // Add
                a.add(totalScore);
            }
        }
        a.sort();
        return a;
    }

    public static double getDefaultScaling(Requirement requirement, double propertyValue) {
        int type = requirement.getType();
        // Case: Scale
        if (type == 0) {
            int targetValue = requirement.getTargetValue();
            if (propertyValue < 0 || propertyValue > 10) {
                return 0.0;
            } else {
                double Distance = Math.abs(propertyValue - targetValue);
                if (Distance > 7) return 0;
                if (Distance > 5) return 1;
                return (10 - Distance) / 10 + 1;
            }
            //Case: 1 Bound
        } else if (type == 1) {
            double Bound = requirement.getBound();
            String expression = requirement.getExpression();
            if (Objects.equals(expression, "++")) {
                if (propertyValue < Bound) {
                    return 0.0;
                } else {
                    if (Bound == 0) return 2.0;
                    double distance = Math.abs(propertyValue - Bound);
                    return (Bound + distance) / Bound;
                }
            } else {
                if (propertyValue > Bound) {
                    return 0.0;
                } else {
                    if (Bound == 0) return 2.0;
                    double distance = Math.abs(propertyValue - Bound);
                    return (Bound + distance) / Bound;
                }
            }
            //Case: 2 Bounds
        } else {
            double lowerBound = requirement.getLowerBound();
            double upperBound = requirement.getUpperBound();
            if (propertyValue < lowerBound || propertyValue > upperBound ||
                    lowerBound == upperBound) {
                return 0.0;
            } else {
                double diff = Math.abs(upperBound - lowerBound) / 2;
                double distance = Math.abs(((lowerBound + upperBound) / 2) - propertyValue);
                return (diff - distance) / diff + 1;
            }
        }
    }

}
