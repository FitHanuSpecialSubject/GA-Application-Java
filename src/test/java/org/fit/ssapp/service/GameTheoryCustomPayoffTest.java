package org.fit.ssapp.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests focused on custom payoff functions in Game Theory.
 * 
 * Note: For payoff functions, the 'p' prefix is used for the current player's properties (e.g., p1, p2),
 * and the 'P' prefix with player index is used for other players (e.g., P1p2 - player 1's property 2).
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryCustomPayoffTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @ParameterizedTest
  @CsvSource({
      "NSGAII,(p1+p2+p3)/3-(p4+p5)/2", // Non-relative payoff function
      "NSGAIII,abs(p1) / 100",
      "eMOEA,ceil(100 / p3)",
      "PESA2,log(4) - p1",
      "VEGA,sqrt(p1) + sqrt(4)",
      "OMOPSO,12 - 41 * p2 + p1",
      "SMPSO,p2 + 21 / 13"
  })
  void exp4j(String algorithm, String function) throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();

    dto.setDefaultPayoffFunction(function);
    dto.setAlgorithm(algorithm);

    MvcResult result = this.mockMvc
        .perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(request().asyncStarted())
        .andReturn();

    final String response = this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();

    final JsonNode jsonNode = objectMapper.readTree(response);
    assertTrue(jsonNode.has("data"));
    final JsonNode data = jsonNode.get("data");
    assertTrue(data.has("players"));
    assertTrue(data.has("fitnessValue"));
  }

  @ParameterizedTest
  @CsvSource({
      "NSGAII,(P1p1+P2p2)/(p3+1)", // Relative payoff function
      "NSGAIII,P2p1*P1p2",
      "eMOEA,max(P1p1,P2p1)",
      "PESA2,min(p1,P1p2)",
      "VEGA,(P1p1+P2p1)/2-p1",
      "OMOPSO,P1p1+P2p2-P1p3",
      "SMPSO,P1p1*p2/P2p3"
  })
  void customFunction(String algorithm, String function) throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();

    dto.setDefaultPayoffFunction(function);
    dto.setAlgorithm(algorithm);

    MvcResult result = this.mockMvc
        .perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(request().asyncStarted())
        .andReturn();

    final String response = this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();

    final JsonNode jsonNode = objectMapper.readTree(response);
    assertTrue(jsonNode.has("data"));
    final JsonNode data = jsonNode.get("data");
    assertTrue(data.has("players"));
    assertTrue(data.has("fitnessValue"));
  }

  @ParameterizedTest
  @CsvSource({
      "NSGAII,sin(p1) * cos(p2) + tan(p3/10)",
      "eMOEA,pow(p1,2) + pow(p2,3) - sqrt(p3)",
      "PESA2,max(p1,p2,p3) / min(p4,p5,1)",
      "VEGA,exp(p1/10) - ln(p2+1)",
      "OMOPSO,floor(p1) + ceil(p2) + round(p3)"
  })
  void complexPayoffFunction(String algorithm, String function) throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();

    dto.setDefaultPayoffFunction(function);
    dto.setAlgorithm(algorithm);

    MvcResult result = this.mockMvc
        .perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(request().asyncStarted())
        .andReturn();

    final String response = this.mockMvc.perform(asyncDispatch(result))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();

    final JsonNode jsonNode = objectMapper.readTree(response);
    assertTrue(jsonNode.has("data"));
    final JsonNode data = jsonNode.get("data");
    assertTrue(data.has("players"));
    assertTrue(data.has("insights"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "(p1 + p2 + ) / 3 - (p4 + p5",
      "p1 + p2 * / p3",
      "(p1 + p2 +) * p3",
      "p1 + p2 + @@",
      "p1 + P9p9"  // Invalid player index
  })
  void invalidFunction(String function) throws Exception {
    GameTheoryProblemDto invalidDto = setUpTestCase();
    invalidDto.setDefaultPayoffFunction(function);

    mockMvc
        .perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void InvalidDto() throws Exception {
    String invalidJson = "{" +
        "\"defaultPayoffFunction\": \"(p1+p2)/2\"," +
        "\"maxTime\": \"sixty\"," +
        "\"generation\": \"hundred\"" +
        "}";

    this.mockMvc
        .perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  private GameTheoryProblemDto setUpTestCase() {
    GameTheoryProblemDto dto = new GameTheoryProblemDto();
    dto.setNormalPlayers(getNormalPlayers());
    dto.setFitnessFunction("default");
    dto.setDefaultPayoffFunction("default");
    dto.setMaximizing(true);
    dto.setDistributedCores("all");
    dto.setMaxTime(5000);
    dto.setGeneration(100);
    dto.setPopulationSize(1000);
    return dto;
  }

  private double[][][] getNormalPlayers() {
    double[] props = {1.0d, 2.0d, 4.0d, 3.0d};
    double[][] strats = {props, props, props};
    return new double[][][] { strats, strats, strats};
  }
}