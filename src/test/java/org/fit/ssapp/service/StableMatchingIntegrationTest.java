package org.fit.ssapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.source.tree.Tree;
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
    assertThat(jsonNode.get("data").has("fitnessValue")).isTrue();

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
    assertThat(jsonNode.get("data").has("fitnessValue")).isTrue();

    assertNoExcludedPairs(jsonNode.get("data").get("matches").get("matches"), dto.getExcludedPairs());
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

    StableMatchingProblemDto dto  = new StableMatchingProblemDto();

        dto.setProblemName("Test Base Case");
        dto.setNumberOfSets(2);
        dto.setNumberOfProperty(2);
        dto.setIndividualSetIndices(new int[]{0, 1, 1});
        dto.setIndividualCapacities(new int[]{1, 1, 1});
        dto.setIndividualRequirements(new String[][]{{"2:3", "1--"}, {"2++", "1:2" }, {"2++", "1--"}});
        dto.setIndividualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0 }, {3.0, 4.0}});
        dto.setIndividualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0 }, {7.0, 8.0}});
        dto.setEvaluateFunctions(new String[]{"default", "default"} );
        dto.setFitnessFunction("default");
        dto.setExcludedPairs( null);
        dto.setPopulationSize(100);
        dto.setGeneration(50);
        dto.setNumberOfIndividuals(3);
        dto.setMaxTime(5000);
        dto.setAlgorithm(algorithm);
        dto.setDistributedCores("all");
        
        return dto;
  }

  private StableMatchingProblemDto createExcludePairDto(String algorithm) {
    StableMatchingProblemDto dto = createBaseCaseDto(algorithm);
    dto.setExcludedPairs(new int[][]{{0, 2}, {1, 2}});
    return dto;
  }

  private void assertNoDuplication(JsonNode matches) {
    for (JsonNode match : matches) {
      int[] indices = getIndices(match);
      Set<Integer> set = new TreeSet<>();
      for (int index : indices) {
          set.add(index);
      }
      assertThat(set.size()).isEqualTo(indices.length).withFailMessage("Match have duplicated indices");
    }
  }

  private void assertNoExcludedPairs(JsonNode matches, int[][] excludedPairs) {
    if (excludedPairs == null) return;
    Map<Integer, int[]> matchesSplitted = new TreeMap<>();
    int i = 0;
    for (JsonNode list: matches) {
      int[] indices = getIndices(list);
      matchesSplitted.put(i, indices);
      i++;
    }
    for (int[] pair : excludedPairs) {
      int left = pair[0];
      int right = pair[1];

      assertThat(left)
          .isNotIn(matchesSplitted.get(right))
          .withFailMessage("Excluded pair matched");
      assertThat(right)
          .isNotIn(matchesSplitted.get(left))
          .withFailMessage("Excluded pair matched");
    }
  }

  private void assertCapacityValid(JsonNode data, StableMatchingProblemDto dto) {
    int[] capacities = dto.getIndividualCapacities();
    HashMap<Integer, Integer> count = new HashMap<>(dto.getNumberOfIndividuals(), 1.0f);
    for (JsonNode match : data.get("matches").get("matches")) {
      int[] indices = getIndices(match);
      for (int index : indices) {
        if (count.containsKey(index)) {
          count.put(index, count.get(index) + 1);
        } else {
          count.put(index, 1);
        }
      }
    }

    count.forEach((index, value) ->
        assertThat(value)
            .isLessThanOrEqualTo(capacities[index])
            .withFailMessage("Capacity exceeded")
    );
  }

  private void assertLeftOversValid(JsonNode data) {
    int[] leftOvers = getIndices(data.get("matches").get("leftOvers"));
    JsonNode matches = data.get("matches").get("matches");
    for (JsonNode match : matches) {
      int[] indices = getIndices(match);
      Arrays.stream(leftOvers).forEach(index ->
          assertThat(index).isNotIn(indices).withFailMessage("Leftover is matched")
      );
    }
  }

  private int[] getIndices(JsonNode match) {
    String[] strs =  match.toString()
        .replaceAll("\\[", "")
        .replaceAll("]", "")
        .split(",");

    return Arrays.stream(strs)
        .filter(str -> ! str.isEmpty())
        .mapToInt(Integer::parseInt)
        .toArray();
  }
}
