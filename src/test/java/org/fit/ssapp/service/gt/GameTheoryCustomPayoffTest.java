package org.fit.ssapp.service.gt;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.containsString;

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
      "NSGAII,(p1+p2+p3)/3-(p4+p5)/2",
      "eMOEA,ceil(100/p3)",
      "PESA2,log(4)-p1",
      "VEGA,sqrt(p1)+sqrt(4)",
      "OMOPSO,12-41*p2+p1",
      "SMPSO,p2+21/13"
  })
  void testNonRelativePayoffFunctions(String algorithm, String function) throws Exception {
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
      "NSGAII,(P1p1+P2p2)/(p3+1)",
      "NSGAIII,P2p1*P1p2",
      "eMOEA,P1p1-P2p2",
      "PESA2,P1p1/P2p2",
      "VEGA,(P1p1+P2p1)/2-p1",
      "OMOPSO,P1p1+P2p2-P1p3",
      "SMPSO,P1p1*p2/P2p3"
  })
  void testRelativePayoffFunctions(String algorithm, String function) throws Exception {
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
      "NSGAII,p1 * p2 + p3 / 10",
      "eMOEA,p1 * p1 + p2 * p2 * p2 - p3",
      "PESA2,P1p1 / 2 + P2p2 * P2p2",
      "VEGA,p1 / 10 + p2",
      "OMOPSO,p1 + p2 + p3"
  })
  void testComplexPayoffFunctions(String algorithm, String function) throws Exception {
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
  @ValueSource(strings = {
      "(p1 + p2 + ) / 3 - (p4 + p5",
      "p1 + p2 * / p3",
      "(p1 + p2 +) * p3",
      "p1 + p2 + @@"
  })
  void testInvalidPayoffFunctions(String function) throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();
    dto.setDefaultPayoffFunction(function);
    dto.setAlgorithm("NSGAII");

    this.mockMvc
        .perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void testInvalidDto() throws Exception {
    String invalidJson = "{" +
        "\"defaultPayoffFunction\": \"(p1+p2)/2\"," +
        "\"maxTime\": \"sixty\"," +
        "\"generation\": \"hundred\"" +
        "}";

    this.mockMvc
        .perform(post("/api/game-theory-solver")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  private GameTheoryProblemDto setUpTestCase() {
    List<NormalPlayer> players = getNormalPlayers();
    GameTheoryProblemDto dto = new GameTheoryProblemDto();
    dto.setSpecialPlayer(null);
    dto.setNormalPlayers(players);
    dto.setFitnessFunction("default");
    dto.setDefaultPayoffFunction("default");
    dto.setMaximizing(true);
    dto.setDistributedCores("all");
    dto.setMaxTime(5000);
    dto.setGeneration(100);
    dto.setPopulationSize(100);
    return dto;
  }

  private List<NormalPlayer> getNormalPlayers() {
    List<NormalPlayer> players = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      NormalPlayer player = new NormalPlayer();
      player.setName("Player " + (i + 1));

      List<Strategy> strategies = new ArrayList<>();
      for (int j = 0; j < 3; j++) {
        Strategy strategy = new Strategy();
        strategy.setName("Strategy " + (j + 1));
        strategy.setPayoff(10.0d);

        List<Double> properties = new ArrayList<>();
        properties.add(10.0d); // p1
        properties.add(5.0d);  // p2
        properties.add(8.0d);  // p3
        properties.add(4.0d);  // p4
        properties.add(2.0d);  // p5

        strategy.setProperties(properties);
        strategies.add(strategy);
      }

      player.setStrategies(strategies);
      player.setPayoffFunction("default");
      players.add(player);
    }

    return players;
  }
}

