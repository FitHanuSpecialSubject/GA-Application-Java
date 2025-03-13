package org.fit.ssapp.st;



import org.fit.ssapp.ss.smt.Matches;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MatchesTest {
    private Matches matches;

    @BeforeEach
    void setUp() {
        matches = new Matches(5);
    }

    @Test
    void testSelectNewMatch() {
        // Giả lập danh sách match
        matches.addMatch(0, 1);
        matches.addMatch(0, 2);
        matches.addMatch(0, 3);

        Set<Integer> matchedSet = matches.getSetOf(0);

        // Kiểm tra danh sách có chứa các node đã add
        assertTrue(matchedSet.contains(1));
        assertTrue(matchedSet.contains(2));
        assertTrue(matchedSet.contains(3));
        assertFalse(matchedSet.contains(4)); // Không có node 4
    }

    @Test
    void testMatchBothWays() {
        // Thêm match hai chiều
        matches.addMatchBi(1, 2);

        // Kiểm tra cả hai phía
        assertTrue(matches.isMatched(1, 2));
        assertTrue(matches.isMatched(2, 1));

        // Đảm bảo cả hai phía đều có trong danh sách match
        assertTrue(matches.getSetOf(1).contains(2));
        assertTrue(matches.getSetOf(2).contains(1));
    }

    @Test
    void testCheckFull() {
        // Giả lập match tối đa 2 node cho node 3
        matches.addMatch(3, 0);
        matches.addMatch(3, 1);

        assertTrue(matches.isFull(3, 2));  // Đã đủ số lượng
        assertFalse(matches.isFull(3, 3)); // Chưa đạt giới hạn 3
    }
}


