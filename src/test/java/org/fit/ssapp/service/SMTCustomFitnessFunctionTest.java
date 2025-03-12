package org.fit.ssapp.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.util.MatchingProblemType;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class SMTCustomFitnessFunctionTest {
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
        dto.setAlgorithm("NSGAII");
        dto.setDistributedCores("4");

        sampleDTO = dto;
        // Clear excluded pairs
        sampleDTO.setExcludedPairs(new int[0][0]);
    }

    @Test
    void validDTO() throws Exception {
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
        dto.setAlgorithm("Genetic Algorithm");
        dto.setDistributedCores("4");


        _mock
                .perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void invalidDTO() throws Exception {
        StableMatchingProblemDto invalidDto = new StableMatchingProblemDto();
        invalidDto.setProblemName("");
        invalidDto.setNumberOfSets(1); // Less than 2 sets
        invalidDto.setNumberOfProperty(2); // Less than 3 properties
        invalidDto.setNumberOfIndividuals(2); // Less than 3 individuals
        invalidDto.setIndividualSetIndices(new int[]{1, 0});
        invalidDto.setIndividualCapacities(new int[]{1, 2});
        invalidDto.setIndividualRequirements(new String[][]{
                {"1", "1.1"},
                {"1++", "1.1"}
        });
        invalidDto.setIndividualWeights(new double[][]{
                {1.0, 2.0},
                {4.0, 5.0}
        });
        invalidDto.setIndividualProperties(new double[][]{
                {1.0, 2.0},
                {4.0, 5.0}
        });
        invalidDto.setEvaluateFunctions(new String[]{
                "10*(P1*W1) + 5*(P1*W2) + (P6*W6) + (P7*W7)"
        });
        invalidDto.setFitnessFunction(""); // Empty fitness function
        invalidDto.setExcludedPairs(new int[][]{
                {1, 2},
                {2, 3}
        });
        invalidDto.setPopulationSize(500);
        invalidDto.setGeneration(50);
        invalidDto.setMaxTime(3600);
        invalidDto.setAlgorithm("Genetic Algorithm");
        invalidDto.setDistributedCores("4");

        _mock
                .perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testFitnessCalculation1() throws Exception {
        String customFitnessFunction = "SIGMA{S1}";
        sampleDTO.setFitnessFunction(customFitnessFunction);
        sampleDTO.setEvaluateFunctions(new String[]{"default", "default"});

        MvcResult result = _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(request().asyncStarted())
                .andReturn();

        final String response = _mock.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final JsonNode jsonNode = objectMapper.readTree(response);
        assertTrue(jsonNode.has("data"));
        final JsonNode data = jsonNode.get("data");
        assertTrue(data.has("matching"));
        assertTrue(data.has("fitnessValue"));
    }

    @Test
    public void testFitnessCalculation() throws Exception {
        String fitnessFunction = "SIGMA{S1}";
        sampleDTO.setFitnessFunction(fitnessFunction);

        MvcResult result = _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(request().asyncStarted())
                .andReturn();

        final String response = _mock.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final JsonNode jsonNode = objectMapper.readTree(response);
        assertTrue(jsonNode.has("data"));
        final JsonNode data = jsonNode.get("data");
        assertTrue(data.has("matching"));
        assertTrue(data.has("fitnessValue"));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "SIGMA{S1} + INVALID",
            "SIGMA{S1} * / SIGMA{S2}",
            "SIGMA{S1} + INVALID_VARIABLE",
            "INVALID_FUNCTION",
            "code qua chien"
    })
    void invalidFitnessFunction(String function) throws Exception {
        sampleDTO.setFitnessFunction(function);

        _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @CsvSource({
            "NSGAII,SIGMA{S1}",
            "NSGAIII,SIGMA{S1}",
            "eMOEA,SIGMA{S1}",
            "PESA2,SIGMA{S1}",
            "VEGA,SIGMA{S1}",
            "IBEA, SIGMA{S1}"
    })
    void exp4j(String algorithm, String function) throws Exception {
        StableMatchingProblemDto dto = sampleDTO;

        dto.setFitnessFunction(function);
        dto.setAlgorithm(algorithm);

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

        final JsonNode jsonNode = objectMapper.readTree(response);
        assertTrue(jsonNode.has("data"));
        final JsonNode data = jsonNode.get("data");
        assertTrue(data.has("matching"));
        assertTrue(data.has("fitnessValue"));
    }


    @ParameterizedTest
    @CsvSource({
            "NSGAII,SIGMA{1}",
            "NSGAIII,SIGMA{1}",
            "eMOEA,SIGMA{1}",
            "PESA2,SIGMA{1}",
            "VEGA,SIGMA{1}",
            "IBEA,SIGMA{1}"
    })
    void geneticAlgo(String algorithm, String function) throws Exception {
        StableMatchingProblemDto dto = sampleDTO;

        dto.setFitnessFunction(function);
        dto.setAlgorithm(algorithm);

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

        final JsonNode jsonNode = objectMapper.readTree(response);
        assertTrue(jsonNode.has("data"));
        final JsonNode data = jsonNode.get("data");
        assertTrue(data.has("matching"));
        assertTrue(data.has("fitnessValue"));
    }

    @ParameterizedTest
    @CsvSource({
            "NSGAII, SIGMA{S1} + SIGMA{S2}",
            "NSGAIII, M1 + M2",
            "eMOEA, S1 + S2",
            "PESA2, SIGMA{S1 * 2} - M3",
            "VEGA, SIGMA{S1}",
            "IBEA, SIGMA{S1} + SIGMA{S2}"
    })
    void customFunction(String algorithm, String function) throws Exception {
        StableMatchingProblemDto dto = sampleDTO;

        dto.setFitnessFunction(function);
        dto.setAlgorithm(algorithm);

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
        assertTrue(data.has("matching"));
        assertTrue(data.has("fitnessValue"));
    }

    private static String[] stableMatchingAlgorithms() {
        return StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
    }

    /*
   * @param algorithm
   * @throws Exception
   */
    @ParameterizedTest
    @MethodSource("stableMatchingAlgorithms")
    void stableMatching(final String algorithm) throws Exception {
        final StableMatchingProblemDto dto = sampleDTO;

        // Perform request
        final MvcResult result = _mock
                .perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc _mock;
}
