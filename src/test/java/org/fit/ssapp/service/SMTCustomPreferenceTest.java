package org.fit.ssapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SMTCustomPreferenceTest {

  StableMatchingProblemDto sampleDTO;
  SampleDataGenerator sampleData;
  MatchingData matchingData;
  TwoSetFitnessEvaluator evaluator;

  @BeforeEach
  public void setUp() {
    int testNumberOfIndividuals1 = 5;
    int testNumberOfIndividuals2 = 1;  //or any positive number
    int testNumberOfProperties = 3;

    sampleData = new SampleDataGenerator(
            MatchingProblemType.MTM,
            testNumberOfIndividuals1, testNumberOfIndividuals2,
            testNumberOfProperties
    );
    matchingData = sampleData.generateProblem().getMatchingData();
    evaluator = new TwoSetFitnessEvaluator(matchingData);

    StableMatchingProblemDto dto = new StableMatchingProblemDto();
    dto.setProblemName("Stable Matching Problem");
    dto.setNumberOfSets(2);
    dto.setNumberOfProperty(3);
    dto.setNumberOfIndividuals(3);
    dto.setIndividualSetIndices(new int[]{1, 1, 0});
    dto.setIndividualCapacities(new int[]{1, 2, 1});
    dto.setEvaluateFunctions(new String[]{
            "default",
            "default"
    });
    dto.setFitnessFunction("default");
    dto.setPopulationSize(500);
    dto.setGeneration(50);
    dto.setMaxTime(3600);
    dto.setAlgorithm("NSGAII");
    dto.setDistributedCores("4");

    sampleDTO = dto;
    // Clear excluded pairs
    sampleDTO.setExcludedPairs(new int[0][0]);
  }


  @ParameterizedTest
  @CsvSource({
          "NSGAII, M1 + M2, '1,1.1,1--;1++,1.1,1.1;1,1,2', '1,2,3;4,5,6;7,8,9', '1,2,3;4,5,6;7,8,9'",
          "NSGAII, SIGMA{S1} + SIGMA{S2}, '1--,2:3,3++;1--,2:3,3++;1--,2:3,3++','4.0,5.0,6.0;4.0,5.0,6.0;4.0,5.0,6.0','1.0,2.0,3.0;1.0,2.0,3.0;1.0,2.0,3.0'",
          "eMOEA, S1 + S2, '4,5,6;4,5,6;4,5,6','7.0,8.0,9.0;7.0,8.0,9.0;7.0,8.0,9.0','4.0,5.0,6.0;4.0,5.0,6.0;4.0,5.0,6.0'",
          "PESA2, SIGMA{S1} - M2, '1:3,5:10,100:200;1:3,5:10,100:200;1:3,5:10,100:200','100,100,150.0;100,100,150.0;100,100,150.0','4.0,5.0,6.0;4.0,5.0,6.0;4.0,5.0,6.0'",
          "VEGA, SIGMA{S1}, '4:5,5:7,1:6;4:5,5:7,1:6;4:5,5:7,1:6','7.0,8.0,9.0;7.0,8.0,9.0;7.0,8.0,9.0','4.0,5.0,6.0;4.0,5.0,6.0;4.0,5.0,6.0'",
          "IBEA, SIGMA{S1} + SIGMA{S2}, '1.5,2.5,3.5;1.5,2.5,3.5;1.5,2.5,3.5','11,20,30;11,20,30;11,20,30','1.0,2.0,3.0;1.0,2.0,3.0;1.0,2.0,3.0'"

  })
  void testCustomPreference(
          String algorithm,
          String function,
          String requirementsStr,
          String weightsStr,
          String propertiesStr
  ) throws Exception {

    String[][] requirements = parseCsvTo2DArray(requirementsStr);
    double[][] weights = parseCsvTo2DDoubleArray(weightsStr);
    double[][] properties = parseCsvTo2DDoubleArray(propertiesStr);

    StableMatchingProblemDto dto = sampleDTO;

    dto.setFitnessFunction(function);
    dto.setAlgorithm(algorithm);
    dto.setIndividualRequirements(requirements);
    dto.setIndividualWeights(weights);
    dto.setIndividualProperties(properties);


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
    JsonNode jsonNode = objectMapper.readTree(response);
    assertThat(jsonNode.has("data")).isTrue();
    assertThat(jsonNode.get("data").has("matches")).isTrue();
    assertThat(jsonNode.get("data").has("fitnessValue")).isTrue();

    assertNoDuplication(jsonNode.get("data").get("matches").get("matches"));
    assertLeftOversValid(jsonNode.get("data"));
    assertCapacityValid(jsonNode.get("data"), dto);
  }

  private String[][] parseCsvTo2DArray(String csv) {
    return Arrays.stream(csv.split(";"))
            .map(row -> row.split(","))
            .toArray(String[][]::new);
  }

  private double[][] parseCsvTo2DDoubleArray(String csv) {
    return Arrays.stream(csv.split(";"))
            .map(row -> Arrays.stream(row.split(","))
                    .mapToDouble(Double::parseDouble)
                    .toArray())
            .toArray(double[][]::new);
  }

  private void assertCapacityValid(JsonNode data, StableMatchingProblemDto dto) {
    int[] capacities = dto.getIndividualCapacities();
    HashMap<Integer, Integer> count = new HashMap<>(dto.getNumberOfIndividuals(), 1.0f);
    for (JsonNode match : data.get("matches").get("matches")) {
      int[] indices = getIndices(match);
      for (int index : indices) {
        if (count.containsKey(index)) {
          count.put(index, count.get(index) + 1);
        } else {
          count.put(index, 1);
        }
      }
    }

    count.forEach((index, value) ->
            assertThat(value)
                    .isLessThanOrEqualTo(capacities[index])
                    .withFailMessage("Capacity exceeded")
    );
  }

  private void assertNoDuplication(JsonNode matches) {
    for (JsonNode match : matches) {
      int[] indices = getIndices(match);
      Set<Integer> set = new TreeSet<>();
      for (int index : indices) {
        set.add(index);
      }
      assertThat( set.size()).isEqualTo(indices.length).withFailMessage("Match have duplicated indices");
    }
  }

  private void assertLeftOversValid(JsonNode data) {
    int[] leftOvers = getIndices(data.get("matches").get("leftOvers"));
    JsonNode matches = data.get("matches").get("matches");
    for (JsonNode match : matches) {
      int[] indices = getIndices(match);
      Arrays.stream(leftOvers).forEach(index ->
              assertThat(index).isNotIn(indices).withFailMessage("Leftover is matched")
      );
    }
  }

  private int[] getIndices(JsonNode match) {
    String[] strs =  match.toString()
            .replaceAll("\\[", "")
            .replaceAll("]", "")
            .split(",");

    return Arrays.stream(strs)
            .filter(str -> ! str.isEmpty())
            .mapToInt(Integer::parseInt)
            .toArray();
  }


  private static String[] stableMatchingAlgorithms() {
    return StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
  }

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc _mock;

}
