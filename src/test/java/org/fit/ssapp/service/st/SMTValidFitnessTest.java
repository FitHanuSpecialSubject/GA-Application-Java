package org.fit.ssapp.service.st;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

@SpringBootTest
@AutoConfigureMockMvc
public class SMTValidFitnessTest {

  @ParameterizedTest
  @MethodSource("malformedFunctions")
  void invalidSyntax(StableMatchingProblemDto dto) throws Exception {
    _mock.perform(post("/api/stable-matching-solver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @MethodSource("invalidSigmaFunctions")
  void invalidSigma(StableMatchingProblemDto dto) throws Exception {

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

    // Verify response structure
    final JsonNode jsonNode = objectMapper.readTree(response);
    assertTrue(jsonNode.has("data"));
    final JsonNode data = jsonNode.get("data");
    assertTrue(data.has("matches"));
    assertTrue(data.has("fitnessValue"));
    assertTrue(data.has("setSatisfactions"));
  }


  @ParameterizedTest
  @MethodSource("validSigmaFunctions")
  void validSigma(StableMatchingProblemDto dto) throws Exception {

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

    // Verify response structure
    final JsonNode jsonNode = objectMapper.readTree(response);
    assertTrue(jsonNode.has("data"));
    final JsonNode data = jsonNode.get("data");
    assertTrue(data.has("matches"));
    assertTrue(data.has("fitnessValue"));
    assertTrue(data.has("setSatisfactions"));
  }


  private static Stream<Arguments> validFunctions() {
    return Stream.of(
            Arguments.of(createDto("default")),
            Arguments.of(createDto("S1 + S2")),
            Arguments.of(createDto("( M1 + M2 )- M3 ")),
            Arguments.of(createDto("M1 - M2")),
            Arguments.of(createDto("M1 / 2"))
    );
  }

  private static Stream<Arguments> validSigmaFunctions() {
    return Stream.of(
            Arguments.of(createDto("SIGMA{S1} + SIGMA{S2}")),
            Arguments.of(createDto("SIGMA{S1} + 3")),
            Arguments.of(createDto("( SIGMA{S2} + M1 )"))
    );
  }

  private static Stream<Arguments> invalidSigmaFunctions() {
    return Stream.of(
            Arguments.of(createDto("SIGMA{S1} + INVALID")),
            Arguments.of(createDto("SIGMA{S1} * / SIGMA{S2}")),
            Arguments.of(createDto("SIGMA{S1} + INVALID_VARIABLE")),
            Arguments.of(createDto("SIGMA{S10} + M1")),
            Arguments.of(createDto("SIGMA{S1+} + SIGMA{1}")),
            Arguments.of(createDto("SIGMA{S1} / 0 "))
    );
  }

  private static Stream<Arguments> malformedFunctions() {
    return Stream.of(
            Arguments.of(createDto("defaulttt")),
            Arguments.of(createDto("S3 + S4")),
            Arguments.of(createDto("(M1 + M2 - M3")),
            Arguments.of(createDto("M1 -/ M2")),
            Arguments.of(createDto("abc - abc")),
            Arguments.of(createDto("M1 / 0"))
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

  private static StableMatchingProblemDto createDto(String fitnessFunction) {
    return StableMatchingProblemDto.builder()
            .problemName("Test Base Case")
            .numberOfSets(2)
            .numberOfProperty(2)
            .individualSetIndices(new int[]{0, 1, 1})
            .individualCapacities(new int[]{1, 1, 1})
            .individualRequirements(new String[][] {{ "1", "1.1" }, { "1++", "1.1"}, { "1", "1" } })
            .individualWeights(new double[][]{{1.0, 2.0}, {3.0, 4.0}, {3.0, 4.0}})
            .individualProperties(new double[][]{{5.0, 6.0}, {7.0, 8.0}, {7.0, 8.0}})
            .evaluateFunctions(new String[]{"default", "default"})
            .fitnessFunction(fitnessFunction)
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
