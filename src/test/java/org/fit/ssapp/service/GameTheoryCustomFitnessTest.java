package org.fit.ssapp.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fit.ssapp.dto.request.GameTheoryProblemDto;
import org.fit.ssapp.ss.gt.NormalPlayer;
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

import java.util.Arrays;
import java.util.List;

/**
 * Tests focused on custom fitness functions and algorithm validations in Game Theory.
 * 
 * Note: For payoff functions, the 'p' prefix is used (e.g., p1, p2).
 * For fitness functions, the 'u' prefix is used (e.g., u1, u2).
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GameTheoryCustomFitnessTest extends BaseGameTheoryTest {

    @Autowired
    private MockMvc mockMvc;

    private GameTheoryProblemDto baseDto;

    @BeforeEach
    void setUp() {
        baseDto = setUpTestCase();
    }

    @Test
    void testCustomFitnessFunction() throws Exception {
        GameTheoryProblemDto testDto = setUpTestCase();
        testDto.setFitnessFunction("(u1+u2)^2/(u3+1)");

        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(u1 + u2 + ) / 3 - (u4 + u5",
        "u1 + u2 * / u3",
        "(u1 + u2 +) * u3",
        "u1 + u2 + invalid",
        "u1 + u9" // Invalid payoff index
    })
    void testInvalidFitnessFunctionSyntax(String invalidFunction) throws Exception {
        GameTheoryProblemDto invalidDto = setUpTestCase();
        invalidDto.setFitnessFunction(invalidFunction);

        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andDo(print())
            .andExpect(status().isBadRequest()) 
            .andReturn();
    }

    @Test
    void testMaximizingTrueCase() throws Exception {
        GameTheoryProblemDto testDto = setUpTestCase();
        testDto.setMaximizing(true);

        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    void testEmptyRequestBody() throws Exception {
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidDataTypes() throws Exception {
        String invalidJson = "{" +
            "\"fitnessFunction\": \"DEFAULT\"," +
            "\"defaultPayoffFunction\": \"(p1+p2+p3)/3-(p4+p5)/2\"," + 
            "\"algorithm\": \"NSGAII\"," +
            "\"maxTime\": \"sixty\"," +
            "\"generation\": \"hundred\"," +
            "\"populationSize\": \"thousand\"," +
            "\"isMaximizing\": \"yes\"" +
            "}";

        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    void testMissingRequiredFields() throws Exception {
        String incompleteJson = "{" +
            "\"fitnessFunction\": \"DEFAULT\"" +
            "}";

        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @ParameterizedTest
    @MethodSource("gameTheoryAlgorithms")
    void testMultipleAlgorithms(String algorithm) throws Exception {
        GameTheoryProblemDto testDto = setUpTestCase();
        testDto.setAlgorithm(algorithm);
        
        this.mockMvc
            .perform(post("/api/game-theory-solver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    }

    private static String[] gameTheoryAlgorithms() {
        return org.fit.ssapp.constants.GameTheoryConst.ALLOWED_INSIGHT_ALGORITHMS;
    }

    private GameTheoryProblemDto setUpTestCase() {
        GameTheoryProblemDto dto = new GameTheoryProblemDto();
        dto.setFitnessFunction("DEFAULT");
        dto.setDefaultPayoffFunction("(p1+p2+p3)/3-(p4+p5)/2"); 

        List<NormalPlayer> players = Arrays.asList(
            createNormalPlayer("Player 1", new double[][]{{10.0, 5.0, 8.0, 4.0, 2.0}, {3.0, 7.0, 6.0, 1.0, 5.0}}),
            createNormalPlayer("Player 2", new double[][]{{6.0, 2.0, 9.0, 3.0, 7.0}, {4.0, 8.0, 5.0, 2.0, 6.0}})
        );

        dto.setNormalPlayers(players);
        dto.setAlgorithm("NSGAII");
        dto.setMaximizing(false);
        dto.setDistributedCores("all");
        dto.setMaxTime(5000);
        dto.setGeneration(100);
        dto.setPopulationSize(1000);
        return dto;
    }
} 
