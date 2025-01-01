package com.example.SS2_Backend.model.stableMatching;

import com.example.SS2_Backend.model.stableMatching.Matches.MatchesOTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.SS2_Backend.util.StringExpressionEvaluator.convertToStringWithoutScientificNotation;

/*
* An extended version of StableMatchingProblem model that designed for one-to-one problem (problem where every individual has capacity = 0).
* */
@Slf4j
@Getter@Setter
public class StableMatchingOTOProblem implements Problem {
    private PreferencesProvider preferencesProvider;
    private IndividualList individuals;
    private static final List<String> VALID_EVALUATE_FUNCTION_KEYWORDS = Arrays.asList("P", "W", "R");
    private String evaluateFunctionForSet1, evaluateFunctionForSet2, fitnessFunction;
    private boolean func1 = false, func2 = false, fitfunc = false;
    private int problemSize, padding;
    private List<PreferenceList> preferences;
    private String problemName;
    public StableMatchingOTOProblem() {
        super();
    }

    public void setPopulation(ArrayList<Individual> individuals, String[] propertiesNames) {
        this.individuals = new IndividualList(individuals, propertiesNames);
        initializeFields();
    }
    private void initializeFields() {
        this.preferencesProvider = new PreferencesProvider(individuals);
        initializePrefProvider();
        problemSize = individuals.getNumberOfIndividual();
        this.preferences = new ArrayList<>();
        this.padding = individuals.getNumberOfIndividualForSet0();
        for (int i = 0; i < problemSize; i++) {
            PreferenceList individualPref = getPreferenceOfIndividual(i);
            this.preferences.add(individualPref);
        }
    }

    public PreferenceList getPreferenceOfIndividual(int index) {
        PreferenceList a;
        if (!func1 && !func2) {
            a = this.preferencesProvider.getPreferenceListByDefault(index);
        } else {
            a = this.preferencesProvider.getPreferenceListByFunction(index);
        }
        return a;
    }

    private void initializePrefProvider() {
        if (this.evaluateFunctionForSet1 != null) {
            this.preferencesProvider.setEvaluateFunctionForSet1(evaluateFunctionForSet1);
        }
        if (this.evaluateFunctionForSet2 != null) {
            this.preferencesProvider.setEvaluateFunctionForSet2(evaluateFunctionForSet2);
        }
    }

    public void setEvaluateFunctionForSet1(String func) {
        if(VALID_EVALUATE_FUNCTION_KEYWORDS.stream().anyMatch(func::contains)) {
            this.func1 = true;
            this.evaluateFunctionForSet1 = func;
        }
    }
    public void setEvaluateFunctionForSet2(String func) {
        if(VALID_EVALUATE_FUNCTION_KEYWORDS.stream().anyMatch(func::contains)) {
            this.func2 = true;
            this.evaluateFunctionForSet2 = func;
        }
    }
    public void setFitnessFunction(String func) {
        if (func.contains("S") || func.contains("SIGMA{") ||
                func.contains("M")) {
            this.fitfunc = true;
            this.fitnessFunction = func;
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
    @Override
    public int getNumberOfVariables() {
        return 1;
    }
    @Override
    public int getNumberOfObjectives() {
        return 1;
    }
    @Override
    public int getNumberOfConstraints() {
        return 0;
    }
    @Override
    public void evaluate(Solution solution) {
        log.info("Evaluating ... ");
        int[] order = ((Permutation) solution.getVariable(0)).toArray();
        MatchesOTO matches = StableMatchingAlgorithm(order);
        double[] satisfaction = getAllSatisfactions(matches);
        solution.setAttribute("matches", matches);
        if (satisfaction.length == 0) {
            solution.setObjective(0, 1);
            log.info("Skip infinite loop");
        } else {
            double sum = Arrays.stream(satisfaction).sum();
            solution.setObjective(0, -sum);
            log.info("Score: {}", convertToStringWithoutScientificNotation(sum));
        }
    }
    public MatchesOTO StableMatchingAlgorithm(int[] order) {
        Queue<Integer> singleQueue = Arrays.stream(order).boxed().collect(Collectors.toCollection(LinkedList::new));
        int[] matches = new int[problemSize];
        for (int i = 0; i < problemSize; i++) matches[i] = -1;
        Set<Integer> matched = new HashSet<>();
        Set<Integer> leftOver = new HashSet<>();
        while(!singleQueue.isEmpty()){
            int a = singleQueue.poll();
            if (matched.contains(a)) continue;
            PreferenceList aPreference = preferences.get(a);
            int prefLen = aPreference.size();
            for (int i = 0; i < prefLen; i++) {
                int b = aPreference.getIndexByPosition(i);
                if (matches[a] == b && matches[b] == a) break;
                if (!matched.contains(b)) {
                    matched.add(a);
                    matched.add(b);
                    matches[a] = b;
                    matches[b] = a;
                    break;
                } else {
                    int bPartner = matches[b];
                    if (bLikeAMore(a, b, bPartner)) {
                        singleQueue.add(bPartner);
                        matched.remove(bPartner);
                        matches[bPartner] = -1;
                        matched.add(a);
                        matches[a] = b;
                        matches[b] = a;
                        break;
                    } else if (i == prefLen - 1) {
                        leftOver.add(a);
                    }
                }
            }
        }
        return new MatchesOTO(matches, leftOver);
    }
    public double[] getAllSatisfactions(MatchesOTO matches) {
        List<Integer> list = matches.getMatches();
        double[] totalSatisfaction = new double[problemSize];
        for (int a = 0; a < problemSize; a++) {
            int b = list.get(a);
            if (b == -1) totalSatisfaction[a] = 0;
            else {
                double aSatis = getPreferenceOfIndividual(a).getScoreByIndex(b);
                double bSatis = getPreferenceOfIndividual(b).getScoreByIndex(a);
                totalSatisfaction[a] = aSatis + bSatis;
            }
        }
        return totalSatisfaction;
    }

    public boolean bLikeAMore(int a, int b, int c) {
        return preferences.get(b).isScoreGreater(a, c);
    }
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(this.getNumberOfVariables(), this.getNumberOfObjectives());
        solution.setVariable(0, new Permutation(problemSize));
        return solution;
    }

    @Override
    public void close(){}
}