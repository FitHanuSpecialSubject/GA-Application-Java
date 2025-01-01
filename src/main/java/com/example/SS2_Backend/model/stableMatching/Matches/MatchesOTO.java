package com.example.SS2_Backend.model.stableMatching.Matches;

import lombok.Data;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Structure for result of StableMatchingOTOProblem algorithm only.
 * This minified unnecessary attribute, but need to be converted to Matches as result.
 */
@Data
public class MatchesOTO implements Serializable {
    private static final long serialVersionUID = 2L;
    private final List<Integer> matches;
    private final Set<Integer> leftOver;

    public MatchesOTO(int[] matches, Set<Integer> leftOver) {
        this.matches = Arrays.stream(matches).boxed().collect(Collectors.toList());
        this.leftOver = leftOver;
    }
    public Matches toMatches() {
        Matches obj = new Matches(matches.size());
        for (int match : matches) {
            if (match == -1) continue;
            obj.addMatch(match, matches.get(match));
            obj.addMatch(matches.get(match), match);
        }
        for(int individual: leftOver) obj.addLeftOver(individual);
        return obj;
    }

    public boolean isEmpty() {
        return matches.isEmpty();
    }
}