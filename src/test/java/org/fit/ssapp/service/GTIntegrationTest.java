package org.fit.ssapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.constants.GameTheoryConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GTIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Test case for base case.
   *
   * @param algorithm allowed algorithm for gt system
   *
   */
  @ParameterizedTest
  @MethodSource("gameTheoryAlgorithms")
  void baseCaseTest(String algorithm) throws Exception{

    GameTheoryProblemDto dto = setUpBaseCase(algorithm);

    MvcResult result = performPostRequest(dto);

    String response = getAsyncResponse(result);

    JsonNode jsonNode = objectMapper.readTree(response);
    assertThat(jsonNode.has("data")).isTrue();
    JsonNode dataNode = jsonNode.get("data");

    assertThat(dataNode.has("fitnessValue")).isTrue();
    assertThat(dataNode.has("players")).isTrue();
    JsonNode playersNode = dataNode.get("players");
    assertThat(playersNode.isArray()).isTrue();
    assertThat(playersNode.size()).isEqualTo(3);

    for (JsonNode playerNode : playersNode) {
      assertThat(playerNode.has("strategyName")).isTrue();
      assertThat(playerNode.get("strategyName").isTextual()).isTrue();
    }

  }

  /**
   * Test case for base case with conflict strategy.
   *
   * @param algorithm allowed algorithm for gt system
   *
   */
  @ParameterizedTest
  @MethodSource("gameTheoryAlgorithms")
  void baseCaseTestWithConflict(String algorithm) throws Exception{

    GameTheoryProblemDto dto = setUpBaseCase(algorithm);

    int[][] conflicts = {
        { 0, 1, 0, 1 },
        { 1, 2, 1, 0 } };

    dto.setConflictSet(conflicts);

    MvcResult result = performPostRequest(dto);

    String response = getAsyncResponse(result);

    JsonNode jsonNode = objectMapper.readTree(response);
    assertThat(jsonNode.has("data")).isTrue();
    JsonNode dataNode = jsonNode.get("data");

    assertThat(dataNode.has("players")).isTrue();
    JsonNode playersNode = dataNode.get("players");
    assertThat(playersNode.isArray()).isTrue();

    validateConflict(playersNode, conflicts);
  }

  /**
   * Test case for base case with empty request .
   *
   */
  @Test
  void testEmptyRequestBody() throws Exception{

    MvcResult result = mockMvc
            .perform(post("/api/game-theory-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")) // Empty JSON body
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

    String response = getAsyncResponse(result);

  }

  /**
   * Test case for base case with invalid fitness function .
   *
   */
  @Test
  void testInvalidFitnessFunc() throws Exception{

    GameTheoryProblemDto dto = setUpBaseCase("NSGAII");
    dto.setFitnessFunction("Wrong fitness function");

    MvcResult result = performPostRequest(dto);

    String response = getAsyncResponse(result);

    JsonNode jsonNode = objectMapper.readTree(response);
    assertThat(jsonNode.has("data")).isTrue();
    JsonNode dataNode = jsonNode.get("data");

    assertThat(jsonNode.has("message")).isTrue();
    String errorMessage = jsonNode.get("message").asText();
    assertThat(errorMessage).contains("Unknown function or variable");
  }

  private MvcResult performPostRequest(GameTheoryProblemDto dto) throws Exception {
    return mockMvc
            .perform(post("/api/game-theory-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(request().asyncStarted())
            .andReturn();
  }

  private String getAsyncResponse(MvcResult result) throws Exception {
    return mockMvc.perform(asyncDispatch(result))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
  }

  /**
   * Set up input data for base case .
   *
   * @return GameTheoryProblemDto
   */
  private GameTheoryProblemDto setUpBaseCase(String algorithm){
    GameTheoryProblemDto dto = new GameTheoryProblemDto();
    dto.setConflictSet(new int[][]{});
    dto.setFitnessFunction("DEFAULT");
    dto.setDefaultPayoffFunction("DEFAULT");
    dto.setNormalPlayers(getNormalPlayers());
    dto.setAlgorithm(algorithm);
    dto.setMaximizing(false);
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

  /**
   * Set up conflict between players .
   *
   * @return Conflict
   */
  private Conflict createConflict(int leftPlayer, int rightPlayer,
                                  int leftStrategy, int rightStrategy) {
    Conflict conflict = new Conflict();
    conflict.setLeftPlayer(leftPlayer);
    conflict.setRightPlayer(rightPlayer);
    conflict.setLeftPlayerStrategy(leftStrategy);
    conflict.setRightPlayerStrategy(rightStrategy);
    return conflict;
  }

  void validateConflict(JsonNode playersNode, int[][] conflicts){
    for (int[] conflict : conflicts) {
      int left = conflict[0];
      int right = conflict[1];
      int leftStrat = conflict[2];
      int rightStrat = conflict[3];
      JsonNode player1Node = playersNode.get(left);
      JsonNode player2Node = playersNode.get(right);

      assertThat(
          player1Node.get("strategyName").asText().equals("Strategy " + leftStrat)
              && player2Node.get("strategyName").asText().equals("Strategy " + rightStrat)
      ).isFalse();
    }


  }

  /**
   * algorithms allowing
   */
  private static String[] gameTheoryAlgorithms() {
    return GameTheoryConst.ALLOWED_INSIGHT_ALGORITHMS;
  }

}