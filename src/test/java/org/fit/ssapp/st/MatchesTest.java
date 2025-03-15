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
            "2,3,4,3",
            "3,4,5,4",
            "4,5,6,5",
            "5,6,7,6",
            "6,7,8,7"
    })
    void testSelectNewMatch(int targetIndividual, int newIndividual, int leastPreferred, int expected) {
        Matches matches = new Matches(10);
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
            "2,3",
            "3,4",
            "4,5",
            "5,6",
            "6,7"
    })
    void testMatchBothWays(int node1, int node2) {
        Matches matches = new Matches(10);
        matches.addMatchBi(node1, node2);

        // Em nghĩ assert riêng biệt là em kiểm tra cả 2 chiều
        assertTrue(matches.getSetOf(node1).contains(node2), "Node1 should be matched with Node2");
        assertTrue(matches.getSetOf(node2).contains(node1), "Node2 should be matched with Node1");
    }

    // 3. Check Full
    @ParameterizedTest
    @CsvSource({
            "0,2,1,false",
            "1,2,2,true",
            "2,3,3,false",
            "3,4,4,true",
            "4,5,5,true",
            "5,6,6,true",
            "6,7,7,true"
    })
    void testCheckFull(int targetNode, int targetNodeCapacity, int matchesToAdd, boolean expected) {
        Matches matches = new Matches(10);
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
            "0,5,3,4,5,3",
            "0,5,4,5,6,4",
            "0,5,5,6,7,5",
            "0,5,6,7,8,6",
            "0,5,7,8,9,7"
    })
    void testGetLeastNode(int set, int newNode, int node1, int node2, int node3, int expected) {
        // Tạo một đối tượng TwoSetPreferenceList với padding = 0
        TwoSetPreferenceList preferenceList = new TwoSetPreferenceList(10, 0);

        // Thêm các score vào preferenceList
        preferenceList.add(1.0); // Node 0
        preferenceList.add(0.8); // Node 1
        preferenceList.add(0.5); // Node 2
        preferenceList.add(0.3); // Node 3
        preferenceList.add(0.2); // Node 4
        preferenceList.add(0.1); // Node 5
        preferenceList.add(0.05); // Node 6
        preferenceList.add(0.01); // Node 7
        preferenceList.add(0.005); // Node 8
        preferenceList.add(0.001); // Node 9

        // Tạo danh sách các node hiện tại
        Set<Integer> currentNodes = new HashSet<>(Arrays.asList(node1, node2, node3));

        // Gọi phương thức getLeastNode và kiểm tra kết quả
        int result = preferenceList.getLeastNode(set, newNode, currentNodes);
        assertEquals(expected, result, "The least preferred node should be " + expected);
    }
}
