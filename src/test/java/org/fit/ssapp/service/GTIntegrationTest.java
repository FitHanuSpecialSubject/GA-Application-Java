package org.fit.ssapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.dto.response.Response;
import org.fit.ssapp.ss.gt.Conflict;
import org.fit.ssapp.ss.gt.NormalPlayer;
import org.fit.ssapp.ss.gt.Strategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  @Test
  void baseCaseTest() throws Exception{

    GameTheoryProblemDto dto = setUpBaseCase();

    this.mockMvc
            .perform(post("/api/game-theory-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
  }

  @Test
  void baseCaseTestWithConflict() throws Exception{

    GameTheoryProblemDto dto = setUpBaseCase();

    List<Conflict> conflicts = Arrays.asList(
            createConflict(2,3,0,1),
            createConflict(1,2,2,2)

    );
    dto.setConflictSet(conflicts);

    this.mockMvc
            .perform(post("/api/game-theory-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
  }


  private GameTheoryProblemDto setUpBaseCase(){
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

    dto.setAlgorithm("NSGAII");
    dto.setMaximizing(false);
    dto.setDistributedCores("all");
    dto.setMaxTime(5000);
    dto.setGeneration(100);
    dto.setPopulationSize(1000);
    return dto;
  }
  private Strategy createStrategy(double... properties) {
    Strategy strategy = new Strategy();
    for (double prop : properties) {
      strategy.addProperty(prop);
    }
    return strategy;
  }

  private Conflict createConflict(int leftPlayer, int rightPlayer,
                                  int leftStrategy, int rightStrategy) {
    Conflict conflict = new Conflict();
    conflict.setLeftPlayer(leftPlayer);
    conflict.setRightPlayer(rightPlayer);
    conflict.setLeftPlayerStrategy(leftStrategy);
    conflict.setRightPlayerStrategy(rightStrategy);
    return conflict;
  }

}