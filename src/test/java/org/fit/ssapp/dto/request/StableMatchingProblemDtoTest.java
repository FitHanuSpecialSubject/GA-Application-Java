package org.fit.ssapp.dto.request;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.fit.ssapp.service.GameTheoryService;
import org.fit.ssapp.service.PsoCompatSmtService;
import org.fit.ssapp.service.StableMatchingOtmService;
import org.fit.ssapp.service.StableMatchingService;
import org.fit.ssapp.service.TripletMatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

/**
 * Test class for StableMatchingProblemDto.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class StableMatchingProblemDtoTest {
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
            "10*(P1*W1) + 5*(P1*W2)",
            "sqrt(P1*W1) + 2*(P3*W3) + e"
    });
    dto.setFitnessFunction("default");
    dto.setExcludedPairs(new int[][]{
            {1, 2},
            {2, 3}
    });
    dto.setPopulationSize(100);
    dto.setGeneration(50);
    dto.setMaxTime(3600);
    dto.setAlgorithm("Genetic Algorithm");
    dto.setDistributedCores("4");

    _mock
        .perform(post("/api/stable-matching-solver")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(dto)))
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
        .andExpect(status().isBadRequest());
  }

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc _mock;
}
