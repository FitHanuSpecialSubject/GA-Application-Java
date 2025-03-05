package org.fit.ssapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StableMatchingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Test Base Case - Không có exclude pair */
    @Test
    public void testBaseCase() throws Exception {
        StableMatchingProblemDto dto = createTestCase(false);

        MvcResult result = mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        List<List<Integer>> matches = (List<List<Integer>>) response.get("matches");

        assertAll(
                () -> assertNoDuplication(matches),
                () -> assertValidCapacity(matches, dto.getIndividualCapacities()),
                () -> assertValidLeftOvers(response, dto.getNumberOfIndividuals())
        );
    }

    /** Test Exclude Pair - Kiểm tra cặp bị loại trừ */
    @Test
    public void testExcludePair() throws Exception {
        StableMatchingProblemDto dto = createTestCase(true);

        MvcResult result = mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        List<List<Integer>> matches = (List<List<Integer>>) response.get("matches");

        assertNoExcludedPairs(matches, dto.getExcludedPairs());
    }

    /** Kiểm tra không có cá nhân nào bị ghép trùng */
    private void assertNoDuplication(List<List<Integer>> matches) {
        Set<Integer> uniqueIndividuals = matches.stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        int totalMatches = matches.stream().mapToInt(List::size).sum();
        assertThat(uniqueIndividuals).hasSize(totalMatches);
    }

    /** Kiểm tra không ai có số match vượt quá capacity */
    private void assertValidCapacity(List<List<Integer>> matches, int[] capacities) {
        int[] matchCount = new int[capacities.length];

        matches.forEach(pair -> pair.forEach(person -> matchCount[person]++));

        for (int i = 0; i < capacities.length; i++) {
            assertThat(matchCount[i]).isLessThanOrEqualTo(capacities[i]);
        }
    }

    /** Kiểm tra danh sách left-over hợp lệ */
    private void assertValidLeftOvers(Map<String, Object> response, int totalIndividuals) {
        List<Integer> leftOvers = (List<Integer>) response.get("leftOvers");
        List<List<Integer>> matches = (List<List<Integer>>) response.get("matches");

        Set<Integer> matchedIndividuals = matches.stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        assertThat(leftOvers).doesNotContainAnyElementsOf(matchedIndividuals);
        assertThat(leftOvers.size() + matchedIndividuals.size()).isEqualTo(totalIndividuals);
    }

    /** Kiểm tra không có cặp bị loại trừ trong kết quả */
    private void assertNoExcludedPairs(List<List<Integer>> matches, int[][] excludePairs) {
        for (int[] pair : excludePairs) {
            assertThat(matches).doesNotContain(List.of(pair[0], pair[1]));
        }
    }

    /** Helper - Tạo DTO cho test case */
    private StableMatchingProblemDto createTestCase(boolean withExcludePairs) {
        StableMatchingProblemDto dto = new StableMatchingProblemDto();
        dto.setProblemName("Stable Matching Problem with 3 Sets");
        dto.setNumberOfSets(3);
        dto.setNumberOfProperty(3);
        dto.setNumberOfIndividuals(6);
        dto.setIndividualSetIndices(new int[]{0, 0, 1, 1, 2, 2});
        dto.setIndividualCapacities(new int[]{1, 2, 1, 1, 2, 1});
        dto.setEvaluateFunctions(new String[]{"default"});
        dto.setFitnessFunction("default");

        if (withExcludePairs) {
            dto.setExcludedPairs(new int[][]{{1, 3}, {2, 5}, {4, 6}});
        } else {
            dto.setExcludedPairs(new int[][]{});
        }
        return dto;
    }

}
