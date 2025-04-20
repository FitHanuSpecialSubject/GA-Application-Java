package org.fit.ssapp.service.gt;

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

    List<Conflict> conflicts = Arrays.asList(
            createConflict(1,2,0,1),
            createConflict(2,3,1,0)
    );
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
   * Test case for base case with invalid fitness function .
   *
   */
  @Test
  void testInvalidFitnessFunc() throws Exception{
    GameTheoryProblemDto dto = setUpBaseCase("NSGAII");
    dto.setFitnessFunction("Wrong fitness function");

    mockMvc
            .perform(post("/api/game-theory-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andReturn();
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
    dto.setConflictSet(new ArrayList<>(0));
    dto.setFitnessFunction("DEFAULT");
    dto.setDefaultPayoffFunction("DEFAULT");

    List<NormalPlayer> players = new ArrayList<>();
    NormalPlayer p1 = new NormalPlayer();

    List<Strategy> strategies1 = Arrays.asList(
            createStrategy(1.0, 2.0, 5.0),
            createStrategy(3.0, 4.0, 2.0),
            createStrategy(7.0, 8.0, 4.0)
    );
    p1.setStrategies(strategies1);

    NormalPlayer p2 = new NormalPlayer();

    List<Strategy> strategies2 = Arrays.asList(
            createStrategy(5.0, 6.0, 3.0),
            createStrategy(7.0, 8.0, 4.0),
            createStrategy(1.0, 2.0, 5.0)

    );
    p2.setStrategies(strategies2);

    NormalPlayer p3 = new NormalPlayer();

    List<Strategy> strategies3 = Arrays.asList(
            createStrategy(4.0, 5.0, 3.0),
            createStrategy(1.0, 6.0, 4.0),
            createStrategy(7.0, 8.0, 4.0)
    );
    p3.setStrategies(strategies3);

    players.add(p1);
    players.add(p2);
    players.add(p3);

    dto.setNormalPlayers(players);

    dto.setAlgorithm(algorithm);
    dto.setMaximizing(false);
    dto.setDistributedCores("all");
    dto.setMaxTime(5000);
    dto.setGeneration(50);
    dto.setPopulationSize(100);
    return dto;
  }

  /**
   * Set up strategy for player .
   *
   * @return Strategy
   */
  private Strategy createStrategy(double... properties) {
    Strategy strategy = new Strategy();
    for (double prop : properties) {
      strategy.addProperty(prop);
    }
    return strategy;
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

  void validateConflict(JsonNode playersNode, List<Conflict> conflicts){
    for (Conflict conflict : conflicts) {
      JsonNode player1Node = playersNode.get(conflict.getLeftPlayer());
      JsonNode player2Node = playersNode.get(conflict.getRightPlayer());

      assertThat(
          player1Node.get("strategyName").asText().equals("Strategy " + conflict.getLeftPlayerStrategy())
              && player2Node.get("strategyName").asText().equals("Strategy " + conflict.getRightPlayerStrategy())
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