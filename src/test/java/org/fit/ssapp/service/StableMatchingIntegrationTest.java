package org.fit.ssapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class StableMatchingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("stableMatchingAlgorithms")
    void stableMatching_BaseCase(String algorithm) throws Exception {
        // Setup test data
        StableMatchingProblemDto dto = createBaseCaseDto(algorithm);

        // Perform request
        String response = mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify response structure
        JsonNode jsonNode = objectMapper.readTree(response);
        assertThat(jsonNode.has("data")).isTrue();
        assertThat(jsonNode.get("data").has("matches")).isTrue();
        assertThat(jsonNode.get("data").has("fitness")).isTrue();

        // Kiểm tra điều kiện Duplication
        assertNoDuplication(jsonNode.get("data").get("matches"));

        // Kiểm tra Left-Overs
        assertLeftOversValid(jsonNode.get("data"));

        // Kiểm tra Capacity
        assertCapacityValid(jsonNode.get("data"), dto);
    }

    @ParameterizedTest
    @MethodSource("stableMatchingAlgorithms")
    void stableMatching_ExcludePair(String algorithm) throws Exception {
        // Setup test data
        StableMatchingProblemDto dto = createExcludePairDto(algorithm);

        // Perform request
        String response = mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify response structure
        JsonNode jsonNode = objectMapper.readTree(response);
        assertThat(jsonNode.has("data")).isTrue();
        assertThat(jsonNode.get("data").has("matches")).isTrue();
        assertThat(jsonNode.get("data").has("fitness")).isTrue();

        // Kiểm tra điều kiện Exclude Pair
        assertNoExcludedPairs(jsonNode.get("data").get("matches"), dto.getExcludedPairs());
    }

    // Invalid Cases
    @Test
    void stableMatching_Invalid_NoRequestBody() throws Exception {
        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stableMatching_Invalid_SyntaxError() throws Exception {
        StableMatchingProblemDto dto = createBaseCaseDto("GaleShapley");
        dto.setEvaluateFunctions(new String[]{"invalid_function()"}); // Sai cú pháp

        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stableMatching_Invalid_WrongDataType() throws Exception {
        String invalidJson = "{ \"problemName\": 123, \"numberOfSets\": \"abc\" }"; // Sai kiểu dữ liệu

        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // Helper Methods
    private static String[] stableMatchingAlgorithms() {
        return StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
    }

    private StableMatchingProblemDto createBaseCaseDto(String algorithm) {
        return new StableMatchingProblemDto(
                "Test Base Case",
                3,
                2,
                new int[]{0, 1, 2}, // individualSetIndices
                new int[]{1, 1, 1}, // individualCapacities
                new String[][]{{"a", "b"}, {"c", "d"}}, // individualRequirements
                new double[][]{{1.0, 2.0}, {3.0, 4.0}}, // individualWeights
                new double[][]{{5.0, 6.0}, {7.0, 8.0}}, // individualProperties
                new String[]{"sqrt(x)"}, // evaluateFunctions
                "x^2 + 2x", // fitnessFunction
                null, // excludedPairs
                100, // populationSize
                50, // generation
                10, // numberOfIndividuals
                30, // maxTime
                algorithm,
                "AUTO"
        );
    }

    private StableMatchingProblemDto createExcludePairDto(String algorithm) {
        StableMatchingProblemDto dto = createBaseCaseDto(algorithm);
        dto.setExcludedPairs(new int[][]{{0, 1}, {2, 3}});
        return dto;
    }

    private void assertNoDuplication(JsonNode matches) {
        Set<String> seenPairs = new HashSet<>();
        for (JsonNode match : matches) {
            String pair = match.toString();
            assertThat(seenPairs.contains(pair)).isFalse();
            seenPairs.add(pair);
        }
    }

    private void assertNoExcludedPairs(JsonNode matches, int[][] excludedPairs) {
        if (excludedPairs == null) return;
        for (int[] pair : excludedPairs) {
            assertThat(matches.toString().contains(Arrays.toString(pair))).isFalse();
        }
    }

    private void assertCapacityValid(JsonNode data, StableMatchingProblemDto dto) {
        Map<Integer, Integer> matchCount = new HashMap<>();
        for (JsonNode match : data.get("matches")) {
            int individual = match.get(0).asInt();
            matchCount.put(individual, matchCount.getOrDefault(individual, 0) + 1);
        }
        for (int i = 0; i < dto.getIndividualCapacities().length; i++) {
            assertThat(matchCount.getOrDefault(i, 0)).isLessThanOrEqualTo(dto.getIndividualCapacities()[i]);
        }
    }

    private void assertLeftOversValid(JsonNode data) {
        JsonNode leftOvers = data.get("leftOvers");
        JsonNode matches = data.get("matches");
        Set<Integer> matchedIndividuals = new HashSet<>();
        for (JsonNode match : matches) {
            matchedIndividuals.add(match.get(0).asInt());
            matchedIndividuals.add(match.get(1).asInt());
        }
        for (JsonNode leftOver : leftOvers) {
            assertThat(matchedIndividuals.contains(leftOver.asInt())).isFalse();
        }
        assertThat(leftOvers.size() + matches.size()).isEqualTo(data.get("totalIndividuals").asInt());
    }
}

