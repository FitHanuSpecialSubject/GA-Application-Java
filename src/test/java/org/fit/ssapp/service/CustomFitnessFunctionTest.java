package org.fit.ssapp.service;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomFitnessFunctionTest {
    StableMatchingProblemDto sampleDTO;
    SampleDataGenerator sampleData;

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
    void validDTO() {
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

        try {
            _mock
                    .perform(post("/api/stable-matching-solver")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andDo(print())
                    .andExpect(status().isOk());
        } catch (Exception e) {
            Assert.isTrue(false, e.getMessage());
        }
    }

    @Test
    void invalidDTO() {
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

        try {
            _mock
                    .perform(post("/api/stable-matching-solver")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            Assert.isTrue(false, e.getMessage());
        }
    }

    @Test
    public void testFitnessCalculationWithDefaultFitnessFunction() throws Exception {
        // Set the fitness function to "default" (exp4j)
        sampleDTO.setFitnessFunction("default");
        // Set the evaluate functions to "default" (exp4j)
        sampleDTO.setEvaluateFunctions(new String[]{"default", "default"});

        _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testFitnessCalculationWithSumFitnessFunction() throws Exception {
        // Set the fitness function to "SUM"
        sampleDTO.setFitnessFunction("SUM");
        // Set the evaluate functions to "SUM"
        sampleDTO.setEvaluateFunctions(new String[]{"SUM", "SUM"});

        _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @ValueSource(strings = {"default", "SUM"})
    public void testFitnessCalculationWithVariousDefaultFunctions(String functionType) throws Exception {
        // Set the fitness function to the provided type
        sampleDTO.setFitnessFunction(functionType);
        // Set the evaluate functions to the provided type
        sampleDTO.setEvaluateFunctions(new String[]{functionType, functionType});

        _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testFitnessCalculationWithCustomFitnessFunction1() throws Exception {
        // Custom fitness function using exp4j
        String customFitnessFunction = "u1 + u2 + u3 + u4 + u5";
        sampleDTO.setFitnessFunction(customFitnessFunction);
        // Set the evaluate functions to the default "default" (exp4j)
        sampleDTO.setEvaluateFunctions(new String[]{"default", "default"});

        _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testFitnessCalculationWithCustomFitnessFunction2() throws Exception {
        // Custom fitness function using exp4j
        String customFitnessFunction = "max(u1, u2) + min(u3, u4) + u5";
        sampleDTO.setFitnessFunction(customFitnessFunction);
        // Set the evaluate functions to the default "default" (exp4j)
        sampleDTO.setEvaluateFunctions(new String[]{"default", "default"});

        _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testFitnessCalculationWithCustomFitnessFunction3() throws Exception {
        // Custom fitness function using exp4j
        String customFitnessFunction = "sqrt(u1*u2) + log(u3) + u4 + u5";
        sampleDTO.setFitnessFunction(customFitnessFunction);
        // Set the evaluate functions to the default "default" (exp4j)
        sampleDTO.setEvaluateFunctions(new String[]{"default", "default"});

        _mock.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testFitnessCalculation() throws Exception {


        double[] satisfactions = {1.0, 2.0, 3.0, 4.0, 5.0};
        String fitnessFunction = "SIGMA{S1}";


        MatchingData matchingData = sampleData.generateProblem().getMatchingData();
        // Create the evaluator
        TwoSetFitnessEvaluator evaluator = new TwoSetFitnessEvaluator(matchingData);
        // Perform the fitness function evaluation
        double result = evaluator.withFitnessFunctionEvaluation(satisfactions, fitnessFunction);
        // Verify the result
        double expected = 15.0;
        assertEquals(expected, result, 0.001);
    }

    @ParameterizedTest
    @MethodSource("stableMatchingAlgorithms")
    void stableMatching(String algoritm) throws Exception {}

    private static String[] stableMatchingAlgorithms() {
        return StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
    }
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc _mock;

}