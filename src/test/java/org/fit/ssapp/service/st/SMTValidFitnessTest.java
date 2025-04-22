package org.fit.ssapp.service.st;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SMTValidFitnessTest {
  StableMatchingProblemDto sampleDTO;
  SampleDataGenerator sampleData;
  MatchingData matchingData;
  TwoSetFitnessEvaluator evaluator;

  private StableMatchingProblemDto setUp() {
    StableMatchingProblemDto dto = new StableMatchingProblemDto();
    dto.setProblemName("Stable Matching Problem");
    dto.setNumberOfSets(2);
    dto.setNumberOfProperty(3);
    dto.setNumberOfIndividuals(3);
    dto.setIndividualSetIndices(new int[] { 1, 1, 0 });
    dto.setIndividualCapacities(new int[] { 1, 2, 1 });
    dto.setIndividualRequirements(new String[][] {
            { "1", "1.1", "1++" },
            { "1++", "1.1", "1.1" },
            { "1", "1", "2" }
    });
    dto.setIndividualWeights(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 }
    });
    dto.setIndividualProperties(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 }
    });
    dto.setEvaluateFunctions(new String[] {
            "default",
            "default"
    });
    dto.setFitnessFunction("default");
    dto.setPopulationSize(100);
    dto.setGeneration(50);
    dto.setMaxTime(3600);
    dto.setAlgorithm("NSGAII");
    dto.setDistributedCores("4");

    // Clear excluded pairs
    dto.setExcludedPairs(new int[0][0]);

    return dto;
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "SIGMA{S1} + INVALID",
          "SIGMA{S1} * / SIGMA{S2}",
          "SIGMA{S1} + INVALID_VARIABLE",
          "INVALID_FUNCTION",
          "SIGMA{S10} + M1",
          "SIGMA{S1+} + SIGMA{1}",
          "SIGMA{S2} * - / SIGMA{S2}",
          "SIGMA{S1} / 0 ",
          "M10 + SIGMA{2}",
          ""
  })
  void invalidSyntax(String function) throws Exception {
    StableMatchingProblemDto dto = setUp();
    dto.setFitnessFunction(function);

    _mock.perform(post("/api/stable-matching-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc _mock;
}
