package org.fit.ssapp.st;

import org.fit.ssapp.ss.smt.Matches;

import org.fit.ssapp.ss.smt.preference.impl.list.TripletPreferenceList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MatchesTest {
    private Matches matches;
    private TripletPreferenceList tripletPreferenceList;

    @BeforeEach
    void setUp() {
        int size = 11;
        int padding = 1;
        matches = new Matches(10);
        tripletPreferenceList = new TripletPreferenceList(size, padding);

        double[] scores = {0.0, 3.1, 1.5, 4.0, 2.2, 5.8, 6.3, 7.7, 8.5, 9.1, 10.0};
        int[] positions = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        tripletPreferenceList.addArray(scores, positions);
    }

    @Test
    void testSelectNewMatch() {
        matches.addMatch(0, 1);
        matches.addMatch(0, 2);
        matches.addMatch(0, 3);
        matches.addMatch(0, 4);
        matches.addMatch(0, 5);
        matches.addMatch(0, 6);
        matches.addMatch(0, 7);

        Set<Integer> matchedSet = matches.getSetOf(0);

        assertTrue(matchedSet.contains(1));
        assertTrue(matchedSet.contains(2));
        assertTrue(matchedSet.contains(3));
        assertTrue(matchedSet.contains(4));
        assertTrue(matchedSet.contains(5));
        assertTrue(matchedSet.contains(6));
        assertTrue(matchedSet.contains(7));
        assertFalse(matchedSet.contains(8));
    }

    @Test
    void testMatchBothWays() {
        matches.addMatchBi(1, 2);
        matches.addMatchBi(3, 4);
        matches.addMatchBi(5, 6);
        matches.addMatchBi(7, 8);

        assertTrue(matches.isMatched(1, 2));
        assertTrue(matches.isMatched(2, 1));
        assertTrue(matches.isMatched(3, 4));
        assertTrue(matches.isMatched(5, 6));
        assertTrue(matches.isMatched(7, 8));

        assertTrue(matches.getSetOf(1).contains(2));
        assertTrue(matches.getSetOf(2).contains(1));
        assertTrue(matches.getSetOf(3).contains(4));
        assertTrue(matches.getSetOf(4).contains(3));
        assertTrue(matches.getSetOf(5).contains(6));
        assertTrue(matches.getSetOf(6).contains(5));
    }

    @Test
    void testCheckFull() {
        matches.addMatch(3, 0);
        matches.addMatch(3, 1);
        matches.addMatch(3, 2);
        matches.addMatch(4, 5);
        matches.addMatch(4, 6);
        matches.addMatch(4, 7);

        assertTrue(matches.isFull(3, 3));
        assertFalse(matches.isFull(3, 4));
        assertTrue(matches.isFull(4, 3));
    }

    @Test
    void testGetLeastNode() {
        Set<Integer> currentNodes = Set.of(2, 3, 5);

        int result = tripletPreferenceList.getLeastNode(0, 4, currentNodes);

        assertEquals(2, result);
    }

    @Test
    void testGetLeastNodeWhenCurrentNodesIsEmpty() {
        Set<Integer> currentNodes = Set.of();

        int result = tripletPreferenceList.getLeastNode(0, 4, currentNodes);

        assertEquals(4, result); // Khi không có node nào, kết quả là chính newNode
    }
}
