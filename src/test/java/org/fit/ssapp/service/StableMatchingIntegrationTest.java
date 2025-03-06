package org.fit.ssapp.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.fit.ssapp.constants.StableMatchingConst;
import org.fit.ssapp.dto.request.StableMatchingProblemDto;
import org.fit.ssapp.service.StableMatchingService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StableMatchingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    // **Test Case 1: Base Case - Default Fitness & Evaluate Function, No Exclude Pair**
    @ParameterizedTest
    @MethodSource("stableMatchingAlgorithms")
    void testBaseCase(String algorithm) throws Exception {
        StableMatchingProblemDto dto = createStableMatchingDto(algorithm, false);

        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // **Test Case 4: Exclude Pair - Base Case with Exclude Pair**
    @ParameterizedTest
    @MethodSource("stableMatchingAlgorithms")
    void testExcludePair(String algorithm) throws Exception {
        StableMatchingProblemDto dto = createStableMatchingDto(algorithm, true);

        mockMvc.perform(post("/api/stable-matching-solver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // **Helper method to create Stable Matching DTO**
    private StableMatchingProblemDto createStableMatchingDto(String algorithm, boolean includeExcludedPairs) {
        StableMatchingProblemDto dto = new StableMatchingProblemDto();
        dto.setProblemName("Stable Matching Test");
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
        dto.setEvaluateFunctions(new String[]{"default", "default"});
        dto.setFitnessFunction("default");

        // Nếu includeExcludedPairs = true, thêm cặp bị loại trừ
        if (includeExcludedPairs) {
            dto.setExcludedPairs(new int[][]{
                    {1, 2},
                    {2, 3}
            });
        }

        dto.setPopulationSize(500);
        dto.setGeneration(50);
        dto.setMaxTime(3600);
        dto.setAlgorithm(algorithm);
        dto.setDistributedCores("4");

        return dto;
    }

    private static String[] stableMatchingAlgorithms() {
        return StableMatchingConst.ALLOWED_INSIGHT_ALGORITHMS;
    }
}
