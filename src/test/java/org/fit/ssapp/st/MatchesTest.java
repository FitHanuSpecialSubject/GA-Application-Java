package org.fit.ssapp.st;

import static org.junit.jupiter.api.Assertions.*;

import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.*;

class MatchesTest {

    // 1. Select New Match
    @ParameterizedTest
    @CsvSource({
            "0,1,2,1",
            "1,2,3,2",
            "2,3,4,3"
    })
    void testSelectNewMatch(int targetIndividual, int newIndividual, int leastPreferred, int expected) {
        Matches matches = new Matches(5);
        matches.addMatch(targetIndividual, leastPreferred);
        matches.addMatch(targetIndividual, newIndividual);

        Set<Integer> currentMatches = matches.getSetOf(targetIndividual);
        currentMatches.add(newIndividual);

        Integer result = Collections.min(currentMatches);
        assertEquals(expected, result, "The least preferred individual should be " + expected);
    }

    // 2. Match Both Ways
    @ParameterizedTest
    @CsvSource({
            "0,1",
            "1,2",
            "2,3"
    })
    void testMatchBothWays(int node1, int node2) {
        Matches matches = new Matches(5);
        matches.addMatchBi(node1, node2);

        assertTrue(matches.isMatched(node1, node2), "Node1 and Node2 should be matched both ways");
    }

    // 3. Check Full
    @ParameterizedTest
    @CsvSource({
            "0,2,1,false",
            "1,2,2,true",
            "2,3,3,false"
    })
    void testCheckFull(int targetNode, int targetNodeCapacity, int matchesToAdd, boolean expected) {
        Matches matches = new Matches(5);
        for (int i = 0; i < matchesToAdd; i++) {
            matches.addMatch(targetNode, i + 10);  // Adding unique nodes to avoid duplication
        }

        assertEquals(expected, matches.isFull(targetNode, targetNodeCapacity), "The node should be full: " + expected);
    }

    // Unit Test cho getLeastNode
    @ParameterizedTest
    @CsvSource({
            "0,5,1,2,3,1",
            "0,5,2,3,4,2",
            "0,5,3,4,5,3"
    })
    void testGetLeastNode(int set, int newNode, int node1, int node2, int node3, int expected) {
        // Tạo một đối tượng TwoSetPreferenceList với padding = 0
        TwoSetPreferenceList preferenceList = new TwoSetPreferenceList(6, 0);

        // Thêm các score vào preferenceList
        preferenceList.add(1.0); // Node 0
        preferenceList.add(0.8); // Node 1
        preferenceList.add(0.5); // Node 2
        preferenceList.add(0.3); // Node 3
        preferenceList.add(0.2); // Node 4
        preferenceList.add(0.1); // Node 5

        // Tạo danh sách các node hiện tại
        Set<Integer> currentNodes = new HashSet<>(Arrays.asList(node1, node2, node3));

        // Gọi phương thức getLeastNode và kiểm tra kết quả
        int result = preferenceList.getLeastNode(set, newNode, currentNodes);
        assertEquals(expected, result, "The least preferred node should be " + expected);
    }
}
