package org.fit.ssapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.fit.ssapp.constants.GameTheoryConst;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.controller.HomeController;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.service.GameTheoryService;
import org.fit.ssapp.service.PsoCompatSmtService;
import org.fit.ssapp.service.StableMatchingOtmService;
import org.fit.ssapp.service.StableMatchingService;
import org.fit.ssapp.service.TripletMatchingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

@SpringBootTest
@AutoConfigureMockMvc
class SmokeTest {
  @Test
  void healthCheck() throws Exception {
      this._mock
          .perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(content().contentType("text/html;charset=UTF-8"));
  }

  @ParameterizedTest
  @MethodSource("stableMatchingAlgorithms")
  void stableMatching(String algoritm) throws Exception {
    StableMatchingProblemDto dto = new StableMatchingProblemDto();
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
    dto.setExcludedPairs(new int[][]{
            {1, 2},
            {2, 3}
    });
    dto.setPopulationSize(500);
    dto.setGeneration(50);
    dto.setMaxTime(3600);
    dto.setAlgorithm(algoritm);
    dto.setDistributedCores("4");

    _mock
        .perform(post("/api/stable-matching-solver")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(dto)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @MethodSource("gameTheoryAlgorithms")
  void gameTheory(String algoritm) throws Exception {
    StableMatchingProblemDto dto = new StableMatchingProblemDto();
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
    dto.setExcludedPairs(new int[][]{
            {1, 2},
            {2, 3}
    });
    dto.setPopulationSize(500);
    dto.setGeneration(50);
    dto.setMaxTime(3600);
    dto.setAlgorithm(algoritm);
    dto.setDistributedCores("4");

    _mock
        .perform(post("/api/game-theory-solver")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(dto)))
        .andDo(print())
        .andExpect(status().isOk());
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

  @MockBean
  private GameTheoryService gameTheoryService;

  @MockBean
  private StableMatchingService stableMatchingSolver;

  @MockBean
  private StableMatchingOtmService otmProblemSolver;

  @MockBean
  private TripletMatchingService tripletMatchingSolver;

  @MockBean
  private PsoCompatSmtService psoCompatSmtService;
}
