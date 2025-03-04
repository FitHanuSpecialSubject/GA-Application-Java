package org.fit.ssapp.service;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.util.StringExpressionEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomFitnessFunctionTest {
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
    public void testFitnessCalculationWithDifferentFunction() {
        double[] satisfactions = {2.0, 3.0, 4.0, 5.0};
        String fitnessFunction = "u1 + u2 + u3 + u4"; // Updated fitness function
        double result = StringExpressionEvaluator.evaluateFitnessValue(satisfactions, fitnessFunction).doubleValue();
        double expected = 14.0;
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void testFitnessCalculationWithEmptySatisfactions() {
        double[] satisfactions = {};
        String fitnessFunction = "SUM"; // Updated fitness function for empty satisfactions
        // Perform the fitness function evaluation using StringExpressionEvaluator
        double result = StringExpressionEvaluator.evaluateFitnessValue(satisfactions, fitnessFunction).doubleValue();
        // Verify the result
        double expected = 0.0;
        assertEquals(expected, result, 0.001);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc _mock;

}