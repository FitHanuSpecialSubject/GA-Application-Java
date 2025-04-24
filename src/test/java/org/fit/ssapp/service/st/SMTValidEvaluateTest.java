package org.fit.ssapp.service.st;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class SMTValidEvaluateTest {

  @ParameterizedTest
  @MethodSource("validInvalidFunctions")
  void invalidSyntax(StableMatchingProblemDto dto) throws Exception {

    _mock.perform(post("/api/stable-matching-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @MethodSource("validFunctions")
  void validSyntax(StableMatchingProblemDto dto) throws Exception {

    MvcResult result = this._mock
            .perform(post("/api/stable-matching-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(request().asyncStarted())
            .andReturn();

    final String response = this._mock.perform(asyncDispatch(result))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();
  }

  private static Stream<Arguments> validFunctions() {
    return Stream.of(
            Arguments.of(createDto("default")),
            Arguments.of(createDto(("abs(R1 - R2) + 1")),
            Arguments.of(createDto("12^2 + log(R1) * P1 + log2(W2) + P3")),
            Arguments.of(createDto("ceil(R2) + (15 / 2) * W3")),
            Arguments.of(createDto("((P1 * W1) + R2 * W2) / (W1 + R1)"))
            )
    );
  }

  private static Stream<Arguments> validInvalidFunctions() {
    return Stream.of(
            Arguments.of(createDto("defaulttt")),
            Arguments.of(createDto("R1 -* W2")),
            Arguments.of(createDto("W2 / 0,4")),
            Arguments.of(createDto("(M1 - @2")),
            Arguments.of(createDto("m1 + P2")),
            Arguments.of(createDto("P5 + Wi5")),
            Arguments.of(createDto("ceil(R5) + (15 / 2) * W3")),
            Arguments.of(createDto("Floor(R2) + 15^2"))
    );
  }

  private static StableMatchingProblemDto createDto(String evaluateFunction) {
    return StableMatchingProblemDto.builder()
            .problemName("Test Base Case")
            .numberOfSets(2)
            .numberOfProperty(2)
            .individualSetIndices(new int[]{0, 1, 1})
            .individualCapacities(new int[]{1, 1, 1})
            .individualRequirements(new String[][] {{ "1", "1.1" }, { "1++", "1.1"}, { "1", "1" } })
            .individualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0}, {3.0, 4.0}})
            .individualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0}, {7.0, 8.0}})
            .evaluateFunctions(new String[]{evaluateFunction, evaluateFunction})
            .fitnessFunction("default")
            .excludedPairs(null)
            .populationSize(100)
            .generation(50)
            .numberOfIndividuals(3)
            .maxTime(5000)
            .algorithm("NSGAII")
            .distributedCores("all")
            .build();
  }

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc _mock;

}