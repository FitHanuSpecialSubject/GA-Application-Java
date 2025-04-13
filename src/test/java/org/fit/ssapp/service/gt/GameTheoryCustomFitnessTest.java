package org.fit.ssapp.service.gt;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
 * Tests focused on custom fitness functions and algorithm validations in Game Theory.
 * <p>
 * Note: For payoff functions, the 'p' prefix is used (e.g., p1, p2).
 * For fitness functions, the 'u' prefix is used (e.g., u1, u2).
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryCustomFitnessTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @ParameterizedTest
  @CsvSource({
      "NSGAII,(u2)^10 + 12",
      "NSGAIII,abs(u1) / 100",
      "eMOEA,ceil(100 / u3)",
      "PESA2,log(4) - u1",
      "VEGA,sqrt(u1) + sqrt(4)",
      "OMOPSO,12 - 41  * u2 + u1",
      "SMPSO,u2 + 21 / 13"
  })
  void exp4j(String algorithm, String function) throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();

    dto.setFitnessFunction(function);
    dto.setAlgorithm(algorithm);
    dto.setDefaultPayoffFunction("DEFAULT");

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
      "NSGAII,SUM",
      "NSGAIII,AVERAGE",
      "eMOEA,MIN",
      "PESA2,MAX",
      "VEGA,PRODUCT",
      "OMOPSO,MEDIAN",
      "SMPSO,RANGE"
  })
  void customFunction(String algorithm, String function) throws Exception {
    GameTheoryProblemDto dto = setUpTestCase();

    dto.setFitnessFunction(function);
    dto.setAlgorithm(algorithm);
    dto.setDefaultPayoffFunction("DEFAULT");

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
      "(u1 + u2 + ) / 3 - (u4 + u5",
      "u1 + u2 * / u3",
      "INVALID",
      "code qua chien"
  })
  void invalidFunction(String function) throws Exception {
    GameTheoryProblemDto invalidDto = setUpTestCase();
    invalidDto.setFitnessFunction(function);

    this.mockMvc
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
        "\"fitnessFunction\": \"DEFAULT\"," +
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
    dto.setFitnessFunction("DEFAULT");
    dto.setDefaultPayoffFunction("DEFAULT");
    dto.setMaximizing(true);
    dto.setDistributedCores("all");
    dto.setMaxTime(5000);
    dto.setGeneration(100);
    dto.setPopulationSize(1000);
    return dto;
  }

  private List<NormalPlayer> getNormalPlayers() {
    final List<Double> stratProps = new ArrayList<Double>(4);
    stratProps.add(1.0d);
    stratProps.add(2.0d);
    stratProps.add(4.0d);
    stratProps.add(3.0d);
    final double payoff = 10d;

    final Strategy strat = new Strategy();
    strat.setPayoff(payoff);
    strat.setProperties(stratProps);

    final List<Strategy> strats = new ArrayList<Strategy>(3);
    strats.add(strat);
    strats.add(strat);
    strats.add(strat);

    final NormalPlayer player = new NormalPlayer();
    player.setStrategies(strats);
    player.setPayoffFunction("SUM");

    final List<NormalPlayer> players = new ArrayList<NormalPlayer>(3);
    players.add(player);
    players.add(player);
    players.add(player);
    return players;
  }
}
