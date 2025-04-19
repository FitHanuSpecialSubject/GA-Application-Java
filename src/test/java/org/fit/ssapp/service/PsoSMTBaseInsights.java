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
public class PsoSMTBaseInsights {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void stableMatching_BaseCase() throws Exception {
    StableMatchingProblemDto dto = createBaseCaseDto(StableMatchingConst.PSO_ALLOWED[0]);

    MvcResult result = mockMvc.perform(post("/api/smt-pso-compat-insight/abc-1234444")
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
    assertThat(jsonNode.get("data").has("runtimes")).isTrue();
    assertThat(jsonNode.get("data").has("fitnessValues")).isTrue();
  }

  @Test
  void stableMatching_ExcludePair() throws Exception {
    StableMatchingProblemDto dto = createExcludePairDto("SMPSO");

    MvcResult result = mockMvc.perform(post("/api/smt-pso-compat-insight/abc-1234444")
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
    assertThat(jsonNode.get("data").has("runtimes")).isTrue();
    assertThat(jsonNode.get("data").has("fitnessValues")).isTrue();
  }

  private StableMatchingProblemDto createBaseCaseDto(String algorithm) {
    return StableMatchingProblemDto.builder()
        .problemName("Test Base Case")
        .numberOfSets(2)
        .numberOfProperty(2)
        .individualSetIndices(new int[]{
            0, 0, 0,
            0, 0, 0,
            0, 0, 0,
            1, 1, 1,
            1, 1, 1,
            1, 1, 1,
        })
        .individualCapacities(new int[]{
            10, 10, 10,
            10, 10, 10,
            10, 10, 10,
            10, 10, 10,
            10, 10, 10,
            10, 10, 10,
        })
        .individualRequirements(new String[][]{
            {"2", "1++"}, {"6:8", "1++" }, {"2++", "7++"},
            {"2", "1++"}, {"6:8", "1++" }, {"2++", "7++"},
            {"2", "1++"}, {"6:8", "1++" }, {"2++", "7++"},
            {"2", "1++"}, {"6:8", "1++" }, {"2++", "7++"},
            {"2", "1++"}, {"6:8", "1++" }, {"2++", "7++"},
            {"2", "1++"}, {"6:8", "1++" }, {"2++", "7++"},
        })
        .individualWeights(new double[][]{
            {1.0, 2.0}, {3.0, 8.0 }, {3.0, 4.0},
            {1.0, 2.0}, {3.0, 4.0 }, {3.0, 4.0},
            {1.0, 2.0}, {3.0, 4.0 }, {3.0, 4.0},
            {1.0, 2.0}, {3.0, 12.0 }, {3.0, 4.0},
            {1.0, 2.0}, {3.0, 100.0 }, {3.0, 4.0},
            {1.0, 2.0}, {3.0, 4.0 }, {3.0, 4.0},
        })
        .individualProperties(new double[][]{
            {5.0, 6.0}, {7.0, 8.0 }, {7.0, 8.0},
            {5.0, 6.0}, {7.0, 8.0 }, {21.0, 8.0},
            {5.0, 6.0}, {7.0, 20.0 }, {7.0, 8.0},
            {5.0, 6.0}, {7.0, 8.0 }, {7.0, 8.0},
            {5.0, 6.0}, {7.0, 8.0 }, {7.0, 8.0},
            {5.0, 6.0}, {7.0, 8.0 }, {12.0, 8.0},
        })
        .evaluateFunctions(new String[]{"default", "default"} )
        .fitnessFunction("default")
        .excludedPairs( null)
        .populationSize(100)
        .generation(50)
        .numberOfIndividuals(18)
        .maxTime(5000)
        .algorithm(algorithm)
        .distributedCores("all")
        .build();

  }

  private StableMatchingProblemDto createExcludePairDto(String algorithm) {
    StableMatchingProblemDto dto = createBaseCaseDto(algorithm);
    dto.setExcludedPairs(new int[][]{{0, 2}, {1, 2}});
    return dto;
  }
}