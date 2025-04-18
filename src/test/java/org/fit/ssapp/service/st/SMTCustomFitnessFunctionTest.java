package org.fit.ssapp.service.st;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.ss.smt.MatchingData;
import org.fit.ssapp.ss.smt.evaluator.impl.TwoSetFitnessEvaluator;
import org.fit.ssapp.util.SampleDataGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class SMTCustomFitnessFunctionTest {
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
        dto.setPopulationSize(500);
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
            "code qua chien"
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

    @ParameterizedTest
    @CsvSource({
            "NSGAII, SIGMA{S1} + SIGMA{S2}",
            "NSGAIII, M1 + M2",
            "eMOEA, S1 + S2",
            "PESA2, SIGMA{S1} - M2",
            "VEGA, SIGMA{S1}",
    })
    void customFunction(String algorithm, String function) throws Exception {
        StableMatchingProblemDto dto = setUp();

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
        assertTrue(data.has("matches"));
        assertTrue(data.has("fitnessValue"));
        assertTrue(data.has("setSatisfactions"));
    }

    private static String[] stableMatchingAlgorithms() {
        return new String[] { "NSGAII", "NSGAIII", "eMOEA", "PESA2", "VEGA" };
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc _mock;
}
