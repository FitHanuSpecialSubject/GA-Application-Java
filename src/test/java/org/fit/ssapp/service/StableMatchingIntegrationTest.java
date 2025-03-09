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

    return StableMatchingProblemDto.builder()
        .problemName("Test Base Case")
        .numberOfSets(2)
        .numberOfProperty(2)
        .individualSetIndices(new int[]{0, 1, 1})
        .individualCapacities(new int[]{1, 1, 2})
        .individualRequirements(new String[][]{{"1", "1++"}, {"2--", "1:2" }, {"2--", "1:2"}})
        .individualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0 }, {3.0, 4.0}})
        .individualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0 }, {7.0, 8.0}})
        .evaluateFunctions(new String[]{"default", "default"} )
        .fitnessFunction("default")
        .excludedPairs( null)
        .populationSize(100)
        .generation(50)
        .numberOfIndividuals(3)
        .maxTime(5000)
        .algorithm(algorithm)
        .distributedCores("all")
        .build();
  }

  private StableMatchingProblemDto createExcludePairDto(String algorithm) {
    StableMatchingProblemDto dto = createBaseCaseDto(algorithm);
    dto.setExcludedPairs(new int[][]{{0, 1}, {1, 2}});
    return dto;
  }

  private void assertNoDuplication(JsonNode matches) {
    for (JsonNode match : matches) {
      String[] indices = getIndices(match);
      Set<String> set = new TreeSet<>(Arrays.asList(indices));
      assertThat(set.size()).isEqualTo(indices.length).withFailMessage("Match have duplicated indices");
    }
  }

  private void assertNoExcludedPairs(JsonNode matches, int[][] excludedPairs) {
    if (excludedPairs == null) return;
    Map<Integer, int[]> matchesSplitted = new TreeMap<>();
    int i = 0;
    for (JsonNode list: matches) {
      int[] indices = Arrays
          .stream(getIndices(list))
          .filter(x -> ! x.isEmpty())
          .mapToInt(Integer::parseInt).toArray();
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
    HashMap<String, Integer> count = new HashMap<>(dto.getNumberOfIndividuals(), 1.0f);
    for (JsonNode match : data.get("matches").get("matches")) {
      String[] indices = getIndices(match);
      for (String index : indices) {
        if (index.isEmpty()) {
          continue;
        }

        if (count.containsKey(index)) {
          count.put(index, count.get(index) + 1);
        } else {
          count.put(index, 1);
        }
      }
    }

    count.forEach((index, value) ->
        assertThat(value)
            .isLessThanOrEqualTo(capacities[Integer.parseInt(index)])
            .withFailMessage("Capacity exceeded")
    );
  }

  private void assertLeftOversValid(JsonNode data) {
    String[] leftOvers = getIndices(data.get("matches").get("leftOvers"));
    JsonNode matches = data.get("matches").get("matches");
    for (JsonNode match : matches) {
      String[] indices = getIndices(match);
      Arrays.stream(leftOvers).forEach(index ->
          assertThat(index).isNotIn(indices).withFailMessage("Leftover is matched")
      );
    }
  }

  private String[] getIndices(JsonNode match) {
    return match.toString()
        .replaceAll("\\[", "")
        .replaceAll("]", "")
        .split(",");
  }
}
