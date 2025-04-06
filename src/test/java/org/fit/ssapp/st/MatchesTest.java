package org.fit.ssapp.st;

import static org.junit.jupiter.api.Assertions.*;

import org.fit.ssapp.ss.smt.Matches;
import org.fit.ssapp.ss.smt.preference.impl.list.TwoSetPreferenceList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

class MatchesTest {

    // Match Both Ways
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

    // Check Full
    // @ParameterizedTest
    @CsvSource({
            "0,2,1,false",
            "1,2,2,true",
            "2,4,3,false",
            "3,4,4,true",
            "4,5,5,true",
            "5,6,6,true",
            "6,7,7,true"
    })
    void isFull(int targetNode, int targetNodeCapacity, int matchesToAdd, boolean expected) {
        Matches matches = new Matches(10);
        for (int i = 0; i < matchesToAdd; i++) {
            matches.addMatch(targetNode, i + 10);  // Adding unique nodes to avoid duplication
        }

        assertEquals(expected, matches.isFull(targetNode, targetNodeCapacity), "The node should be full: " + expected);
    }

    // Unit Test cho getLeastNode
    // @ParameterizedTest
    @MethodSource("provideTestDataForGetLeastNode")
    void getLeastNode(int set, int newNode, Set<Integer> currentNodes, int expected) {
        // Tạo một đối tượng TwoSetPreferenceList với padding = 0
        TwoSetPreferenceList preferenceList = new TwoSetPreferenceList(10);

        // Thêm các score vào preferenceList
        preferenceList.add(0,1.0); // Node 0
        preferenceList.add(1,0.8); // Node 1
        preferenceList.add(2,0.5); // Node 2
        preferenceList.add(3,0.3); // Node 3
        preferenceList.add(4,0.2); // Node 4
        preferenceList.add(5,0.1); // Node 5
        preferenceList.add(6,0.05); // Node 6
        preferenceList.add(7,0.01); // Node 7
        preferenceList.add(8,0.005); // Node 8
        preferenceList.add(9,0.001); // Node 9

        // Gọi phương thức getLeastNode và kiểm tra kết quả
        int result = preferenceList.getLeastNode(set, newNode, currentNodes);
        assertEquals(expected, result, "The least preferred node should be " + expected);
    }

    // Phương thức cung cấp dữ liệu test cho testGetLeastNode
    private static Stream<Arguments> provideTestDataForGetLeastNode() {
        return Stream.of(
                Arguments.of(0, 5, new HashSet<>(Arrays.asList(1, 2, 3)), 5),
                Arguments.of(0, 5, new HashSet<>(Arrays.asList(2, 3, 4)), 5),
                Arguments.of(0, 9, new HashSet<>(Arrays.asList(3, 4, 5)), 9),
                Arguments.of(0, 5, new HashSet<>(Arrays.asList(4, 5, 6)), 6),
                Arguments.of(0, 5, new HashSet<>(Arrays.asList(5, 6, 7)), 7),
                Arguments.of(0, 5, new HashSet<>(Arrays.asList(6, 7, 8)), 8),
                Arguments.of(0, 5, new HashSet<>(Arrays.asList(7, 8, 9)), 9)
        );
    }
}
