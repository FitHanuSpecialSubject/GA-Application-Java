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
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

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
        StableMatchingProblemDto dto = createBaseCaseDto(algorithm);

        MvcResult result = mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
                .andReturn();

        String response = mockMvc.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        assertThat(jsonNode.has("data")).isTrue();
        assertThat(jsonNode.get("data").has("matches")).isTrue();
        assertThat(jsonNode.get("data").has("fitness")).isTrue();

        assertNoDuplication(jsonNode.get("data").get("matches").get("matches"));
        assertLeftOversValid(jsonNode.get("data"));
        assertCapacityValid(jsonNode.get("data"), dto);
    }

    @ParameterizedTest
    @MethodSource("stableMatchingAlgorithms")
    void stableMatching_ExcludePair(String algorithm) throws Exception {
        StableMatchingProblemDto dto = createExcludePairDto(algorithm);

        MvcResult result = mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
                .andReturn();

        String response = mockMvc.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        assertThat(jsonNode.has("data")).isTrue();
        assertThat(jsonNode.get("data").has("matches")).isTrue();
        assertThat(jsonNode.get("data").has("fitness")).isTrue();

        assertNoExcludedPairs(jsonNode.get("data").get("matches"), dto.getExcludedPairs());
    }

    @Test
    void stableMatching_Invalid_NoRequestBody() throws Exception {
        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stableMatching_Invalid_SyntaxError() throws Exception {
        StableMatchingProblemDto dto = createBaseCaseDto("GaleShapley");
        dto.setEvaluateFunctions(new String[]{"invalid_function()"});

        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stableMatching_Invalid_WrongDataType() throws Exception {
        String invalidJson = "{ \"problemName\": 123, \"numberOfSets\": \"abc\" }";

        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    private static String[] stableMatchingAlgorithms() {
        return StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
    }

    private StableMatchingProblemDto createBaseCaseDto(String algorithm) {
        return new StableMatchingProblemDto("Test Base Case",
                2, 2,
                new int[]{0, 1},
                new int[]{1, 1},
                new String[][]{{"1", "1++"}, {"2--", "1:2"}},
                new double[][]{{1.0, 2.0}, {3.0, 4.0}},
                new double[][]{{5.0, 6.0}, {7.0, 8.0}},
                new String[]{"default"},
                "default",
                null,
                100,
                50,
                2,
                30,
                algorithm,
                "all");
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
    }
}
