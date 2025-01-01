package com.example.SS2_Backend.ss.smt.implement;

import com.example.SS2_Backend.constants.MatchingConst;
import com.example.SS2_Backend.ss.smt.preference.PreferenceList;
import com.example.SS2_Backend.ss.smt.MatchingData;
import com.example.SS2_Backend.ss.smt.MatchingProblem;
import com.example.SS2_Backend.ss.smt.evaluator.FitnessEvaluator;
import com.example.SS2_Backend.ss.smt.Matches;
import com.example.SS2_Backend.ss.smt.preference.PreferenceListWrapper;
import com.example.SS2_Backend.ss.smt.preference.impl.list.TripletPreferenceList;
import com.example.SS2_Backend.util.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.Permutation;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TripletOTOProblem implements MatchingProblem {

    /** problem name */
    final String problemName;

    /** problem size (number of individuals in matching problem */
    final int problemSize;

    /** number of set in matching problem */
    final int setNum;

    /** Matching data */
    final MatchingData matchingData;

    /** preference list  */
    final PreferenceListWrapper preferenceLists;

    /** problem fitness function */
    final String fitnessFunction;

    /** fitness evaluator */
    final FitnessEvaluator fitnessEvaluator;

    /** will not be used */
    final int UNUSED_VAL = MatchingConst.UNUSED_VALUE;



    /**
     * generate new solution
     * @return Solution contains Variable(s)
     */
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(1, 1);
        Permutation permutationVar = new Permutation(problemSize);
        solution.setVariable(0, permutationVar);
        return solution;
    }

    /**
     * evaluate function for matching problem
     * @param solution Solution contains Variable(s)
     */
    @Override
    public void evaluate(Solution solution) {
        Matches result = this.stableMatching(solution.getVariable(0));
        // Check Exclude Pairs
        int[][] excludedPairs = this.matchingData.getExcludedPairs();
        if (Objects.nonNull(excludedPairs)) {
            for (int[] excludedPair : excludedPairs) {
                if (result.getSetOf(excludedPair[0]).contains(excludedPair[1])) {
                    solution.setObjective(0, Double.MAX_VALUE);
                    return;
                }
            }
        }
        double[] satisfactions = this.preferenceLists.getMatchesSatisfactions(result, matchingData);
        double fitnessScore;
        if (this.hasFitnessFunc()) {
            fitnessScore = fitnessEvaluator
                    .withFitnessFunctionEvaluation(satisfactions, this.fitnessFunction);
        } else {
            fitnessScore = fitnessEvaluator.defaultFitnessEvaluation(satisfactions);
        }
        solution.setAttribute(MatchingConst.MATCHES_KEY, result);
        solution.setObjective(0, -fitnessScore);
    }


    /**
     * check exists fitness function
     * @return true if exists
     */
    public boolean hasFitnessFunc() {
        return StringUtils.isEmptyOrNull(this.fitnessFunction);
    }


    public double[] getMatchesSatisfactions(Matches matches) {
        return this.preferenceLists.getMatchesSatisfactions(matches, matchingData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matches stableMatching(Variable var){
        Matches matches = new Matches(matchingData.getSize());
        Set<Integer> matchedNode = new HashSet<>();
        Permutation castVar = (Permutation) var;
        int[] decodeVar = castVar.toArray();
        Queue<Integer> unMatchedNode = new LinkedList<>();

        for (int val : decodeVar) {
            unMatchedNode.add(val);
        }

        while(!unMatchedNode.isEmpty()){
            int newNode = unMatchedNode.poll();

            if(matchedNode.contains(newNode)) continue;

            int currentSet = matchingData.getSetNoOf(newNode);
            int[] otherSets = getOtherSets(currentSet);

            TripletPreferenceList nodePreference = (TripletPreferenceList) preferenceLists.get(newNode);
            List<Integer> matchedGroup = new ArrayList<>();   // chứa những cá thể ghép với nhau trong lượt ghép
            matchedGroup.add(newNode);

            for (int targetSet : otherSets) {    // ghép với từng set khác
                int preferNodeOfTargetSet = matchWithTargetSet(newNode, targetSet, nodePreference, matches, unMatchedNode);

                // if can not match with any node in target set, add to leftovers
                if(preferNodeOfTargetSet == -1){
//                    matches.addLeftOver(preferNodeOfTargetSet);
                    break;
                }
                matchedGroup.add(preferNodeOfTargetSet);
            }

            matches.addMatchForGroup(matchedGroup);
            matchedNode.addAll(matchedGroup);

        }

        // case when a newNode match with only 1 in 2 set
        for (int i = 0; i < matches.getSize(); i++) {
            Set<Integer> currentSet = matches.getSetOf(i);
            if (currentSet.size() == 1) {
                int element = currentSet.iterator().next();
                matches.removeMatchBi(i, element);
            }
        }

        return matches;

    }


    private int matchWithTargetSet(int newNode, int targetSet,
                                   TripletPreferenceList nodePreferences,
                                   Matches matches,
                                   Queue<Integer> unmatchedNodes) {
        // -1 is not find yet
        int result = -1;

//        int[] preferPartForTargetSet = nodePreferences.getPreferenceForSpecificSet(
//                matchingData.getSetNoOf(newNode), targetSet, matchingData.getSetNums());

        int sizeOfTargetSet = matchingData.getSetNums().get(targetSet);

        int currentNewNodeSet = matchingData.getSetNoOf(newNode) ;
        int padding = calculatePadding(targetSet, currentNewNodeSet);
        int calPosition = calculatePosition(targetSet,currentNewNodeSet ) ;
        nodePreferences.setPadding(padding);

        for (int i = 0 ; i < sizeOfTargetSet; i++) {     // ghép với 1 cá thể trong preferList

            int preferNode = nodePreferences.getPositionByRank(UNUSED_VAL, calPosition + i );

            if (!matches.isFull(preferNode, matchingData.getCapacityOf(preferNode))) {
                result = preferNode;    // ghép thành công với preferNode thì dừng vòng lặp
                break;   //
            }else {
                if(breakPreviousMatch(newNode, preferNode, matches, unmatchedNodes)){
                    result = preferNode;    // ghép thành công với preferNode thì dừng vòng lặp
                    break;
                }
            }
        }

        return result ;

    }

    private boolean breakPreviousMatch(int newNode, int preferNode,
                                       Matches matches, Queue<Integer> unmatchedNodes){
        Integer[] individualMatches = matches.getSetOf(preferNode).toArray(new Integer[0]);
        // các cá thể mà preferNode đã ghép với trước đó
        for (int currentNode : individualMatches) {    // ví dụ 1 ghép với 4, 4 đã có [2,8] ghép với --> 1 so với 2
            if (matchingData.getSetNoOf(currentNode) == matchingData.getSetNoOf(newNode)) { // so sánh 2 cá thể cùng set
                if (preferenceLists.isPreferredOver(newNode, currentNode, preferNode)) {
                    Collection<Integer> allMatched = matches.getMatchesAndTarget(preferNode);

                    for (int matched : allMatched) {
                        matches.disMatch(matched, allMatched);    // hủy ghép cặp cũ nếu chọn cá thể mới
                        if(matched != preferNode) unmatchedNodes.add(matched);
                    }

                    return true ;
                }
            }
        }
        return false;
    }


    private int[] getOtherSets(int currentSet) {
        return IntStream.range(0, setNum)
                .filter(set -> set != currentSet)
                .toArray();
    }

    private int calculatePadding(int targetSet, int currentNewNodeSet){
        Map<Integer, Integer> setNums = matchingData.getSetNums();
        if(currentNewNodeSet == setNum -1) return 0 ;
        if(currentNewNodeSet == 0 ) return setNums.get(currentNewNodeSet) ;

        // if smaller than newNode set, return 0 to get all name of previous set before current's
        if(targetSet < currentNewNodeSet) return 0 ;

        int paddingSize = 0 ;
//        if(targetSet > currentNewNodeSet){
//            for(int i = 0 ; i < currentNewNodeSet ; i++){
//                paddingSize += setNums.get(i);
//            }
//        }
        paddingSize += setNums.get(targetSet);
        return paddingSize;
    }

    private int calculatePosition(int targetSet, int currentNewNodeSet){
        Map<Integer, Integer> setNums = matchingData.getSetNums();

        // if smaller than newNode set, return 0 to get all name of previous set before current's
        int paddingSize = 0 ;

            for(int i = 0 ; i < targetSet; i++){
                if(i != currentNewNodeSet) {
                    paddingSize += setNums.get(i);
                }
            }

        return paddingSize;
    }




    @Override
    public String getMatchingTypeName() {
        return "One To One To One";
    }

    /**
     * MOEA Framework Problem implements
     */

    @Override
    public String getName() {
        return this.problemName;
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return 1;
    }

    @Override
    public int getNumberOfVariables() {
        return 1;
    }

    @Override
    public void close() {
    }
}
