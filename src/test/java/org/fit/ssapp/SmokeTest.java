package org.fit.ssapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.fit.ssapp.constants.GameTheoryConst;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.service.GameTheoryService;
import org.fit.ssapp.service.PsoCompatSmtService;
import org.fit.ssapp.service.StableMatchingOtmService;
import org.fit.ssapp.service.StableMatchingService;
import org.fit.ssapp.service.TripletMatchingService;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * A simple test to verify system surface level health. Success of this test
 * should not be taken seriously
 */
@SpringBootTest
@AutoConfigureMockMvc
class SmokeTest {
  /**
   * Basic check to see if the server run
   * @throws Exception
   */
  @Test
  void healthCheck() throws Exception {
    this._mock
      .perform(get("/"))
      .andExpect(status().isOk())
      .andExpect(content().contentType("text/html;charset=UTF-8"));
  }

  /**
   * Basic test with various GA algorithms allowed for this domain. The test also check the response
   * structure
   * {@link org.fit.ssapp.constants.StableMatchingConst}
   * @param algorithm
   * @throws Exception
   */
//  @ParameterizedTest
  @MethodSource("stableMatchingAlgorithms")
  void stableMatching(final String algorithm) throws Exception {
    final StableMatchingProblemDto dto =
        getStableMatchingProblemDto(algorithm);

    // Perform request
    final MvcResult result = _mock
      .perform(post("/api/stable-matching-solver")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
//      .andExpect(request().asyncStarted())
      .andReturn();

    final String response = _mock.perform(asyncDispatch(result))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andReturn()
      .getResponse()
      .getContentAsString();

    // Verify response structure
    final JsonNode jsonNode = objectMapper.readTree(response);
    assertThat(jsonNode.has("data")).isTrue();
    final JsonNode data = jsonNode.get("data");
    assertThat(data.has("matches")).isTrue();
    assertThat(data.has("fitnessValue")).isTrue();
    assertThat(data.has("setSatisfactions")).isTrue();
  }

  private static StableMatchingProblemDto getStableMatchingProblemDto(String algorithm)
  {
    final StableMatchingProblemDto dto = new StableMatchingProblemDto();
    dto.setProblemName("Stable Matching Problem");
    dto.setNumberOfSets(2);
    dto.setNumberOfProperty(3);
    dto.setNumberOfIndividuals(3);
    dto.setIndividualSetIndices(new int[]{1, 1, 0});
    dto.setIndividualCapacities(new int[]{1, 2, 1});
    dto.setIndividualRequirements(new String[][]{
      {"1", "1.1", "1--"},
      {"1++", "1.1", "1.1"},
      {"1", "1", "2"}
    });
    dto.setIndividualWeights(new double[][]{
      {1.0, 2.0, 3.0},
      {4.0, 5.0, 6.0},
      {7.0, 8.0, 9.0}
    });
    dto.setIndividualProperties(new double[][]{
      {1.0, 2.0, 3.0},
      {4.0, 5.0, 6.0},
      {7.0, 8.0, 9.0}
    });
    dto.setEvaluateFunctions(new String[]{
      "default",
      "default"
    });
    dto.setFitnessFunction("default");
    dto.setPopulationSize(500);
    dto.setGeneration(50);
    dto.setMaxTime(3600);
    dto.setAlgorithm(algorithm);
    dto.setDistributedCores("all");
    dto.setRunCountPerAlgorithm(StableMatchingConst.DEFAULT_RUN_COUNT_PER_ALGO);
    return dto;
  }

  /**
   * Basic test with various GA algorithms allowed for this domain. The test also check the response
   * structure
   * {@link org.fit.ssapp.constants.GameTheoryConst}
   * @param algorithm
   * @throws Exception
   */
  // @ParameterizedTest
  @MethodSource("gameTheoryAlgorithms")
  void gameTheory(final String algorithm) throws Exception {
    final GameTheoryProblemDto dto = getGameTheoryProblemDto(algorithm);

    final MvcResult result = _mock
      .perform(post("/api/game-theory-solver")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
      .andExpect(request().asyncStarted())
      .andReturn();

    final String response = _mock
      .perform(asyncDispatch(result))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andReturn()
      .getResponse()
      .getContentAsString();

    // Verify response structure
    final JsonNode jsonNode = objectMapper.readTree(response);
    assertThat(jsonNode.has("data")).isTrue();
    final JsonNode data = jsonNode.get("data");
    assertThat(data.has("players")).isTrue();
    assertThat(data.has("fitnessValue")).isTrue();
  }

  private static GameTheoryProblemDto getGameTheoryProblemDto(String algoritm)
  {
    final List<NormalPlayer> players = getNormalPlayers();

    final GameTheoryProblemDto dto = new GameTheoryProblemDto();
    dto.setSpecialPlayer(null);
    dto.setNormalPlayers(players);
    dto.setConflictSet(new ArrayList<>());
    dto.setFitnessFunction("default");
    dto.setDefaultPayoffFunction("default");
    dto.setAlgorithm(algoritm);
    dto.setMaximizing(true);
    dto.setDistributedCores("all");
    dto.setMaxTime(5000);
    dto.setGeneration(10);
    dto.setPopulationSize(100);
    return dto;
  }

  private static List<NormalPlayer> getNormalPlayers()
  {
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
    player.setPayoffFunction("default");

    final List<NormalPlayer> players = new ArrayList<NormalPlayer>(3);
    players.add(player);
    players.add(player);
    players.add(player);
    return players;
  }

  /**
   * Run with empty payload
   * @throws Exception
   */
  // @Test
  void gameTheoryInvalid() throws Exception {
    _mock.perform(post("/api/game-theory-solver")
          .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  /**
   * Run with empty payload
   * @throws Exception
   */
  // @Test
  void stableMatchingInvalid() throws Exception {
    _mock
      .perform(post("/api/stable-matching-solver")
          .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }
  private static String[] stableMatchingAlgorithms() {
    return StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
  }

  private static String[] gameTheoryAlgorithms() {
    return GameTheoryConst.ALLOWED_INSIGHT_ALGORITHMS;
  }

  @Autowired
  private MockMvc _mock;

  @Autowired
  private ObjectMapper objectMapper;
}
